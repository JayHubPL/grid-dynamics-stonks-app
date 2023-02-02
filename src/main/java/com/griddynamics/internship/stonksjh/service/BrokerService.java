package com.griddynamics.internship.stonksjh.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.griddynamics.internship.stonksjh.exception.broker.InsufficientBalanceException;
import com.griddynamics.internship.stonksjh.exception.broker.InsufficientStockAmountException;
import com.griddynamics.internship.stonksjh.exception.broker.MissingFinnhubApiTokenException;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.model.User;
import com.griddynamics.internship.stonksjh.repository.OrderRepository;

@Service
public class BrokerService {

    private final String FINNHUB_TOKEN_HEADER = "X-Finnhub-Token";
    private final String FINNHUB_API_QUOTE_ENDPOINT = "https://finnhub.io/api/v1/quote?symbol=%s";
    private final Path FINNHUB_TOKEN_PATH = Path.of("token.txt");
    private final BigDecimal COMMISSION_PERCENT = new BigDecimal("0.07"); // 7% commission
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final String token;

    private Map<Order.Symbol, BigDecimal> stockPrices;

    private final OrderRepository orderRepository;

    public BrokerService(OrderRepository orderRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
        try {
            token = Files.readString(FINNHUB_TOKEN_PATH);
        } catch (IOException e) {
            throw new MissingFinnhubApiTokenException();
        }
        initializeDaemonThread(1);
    }

    public void checkIfOrderCanBePlaced(Order order) {
        User owner = order.getOwner();
        switch (order.getType()) {
            case BUY -> {
                BigDecimal actualBalance = owner.getBalance().subtract(getPendingExpenditures(owner));
                BigDecimal orderValue = evaluateOrderValue(order);
                if (actualBalance.compareTo(orderValue) < 0) {
                    throw new InsufficientBalanceException(orderValue, actualBalance);
                }
            }
            case SELL -> {
                long stockCount = orderRepository.findAllByOwnerUuidAndSymbol(owner.getUuid(), order.getSymbol()).stream()
                        .mapToLong(o -> (long)o.getAmount() * (o.getType() == Order.Type.BUY ? 1 : -1))
                        .sum();
                if (order.getAmount() > stockCount) {
                    throw new InsufficientStockAmountException(order.getAmount(), order.getSymbol(), stockCount);
                }
            }
        }
    }

    private void initializeDaemonThread(long period) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "brokerDaemon");
                thread.setDaemon(true);
                return thread;
            }
        });
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                stockPrices = getAllStockPrices();
                orderRepository.findAllByStatus(Order.Status.PENDING)
                        .forEach(order -> processOrder(order));
            }
        }, 0, period, TimeUnit.MINUTES);
    }

    private BigDecimal getPendingExpenditures(User user) {
        BigDecimal pendingExpenditures = orderRepository.findAllByOwnerUuid(user.getUuid()).stream()
                .filter(order -> order.getStatus() == Order.Status.PENDING)
                .filter(order -> order.getType() == Order.Type.BUY)
                .reduce(
                    BigDecimal.ZERO,
                    (acc, order) -> acc.add(order.getBid().multiply(BigDecimal.valueOf(order.getAmount()))),
                    BigDecimal::add
                );
        return applyCommission(pendingExpenditures);
    }

    private void processOrder(Order order) {
        User owner = order.getOwner();
        BigDecimal orderValue = evaluateOrderValue(order);
        switch (order.getType()) {
            case BUY -> {
                BigDecimal actualBalance = owner.getBalance().subtract(getPendingExpenditures(owner));
                if (applyCommission(order.getBid()).compareTo(stockPrices.get(order.getSymbol())) < 0 || actualBalance.compareTo(orderValue) < 0) {
                    return; // can't be processed yet
                }
                owner.setBalance(owner.getBalance().subtract(orderValue));
            }
            case SELL -> {
                if (order.getBid().compareTo(stockPrices.get(order.getSymbol())) < 0) {
                    return; // can't be processed yet
                }
                owner.setBalance(owner.getBalance().add(orderValue.multiply(BigDecimal.ONE.subtract(COMMISSION_PERCENT))));
            }
        }
        order.setStatus(Order.Status.COMPLETE);
        orderRepository.save(order);
    }

    private BigDecimal evaluateOrderValue(Order order) {
        return applyCommission(order.getBid().multiply(BigDecimal.valueOf(order.getAmount())));
    }

    private BigDecimal getStockPrice(Order.Symbol symbol) {
        try {
            URI uri = URI.create(String.format(FINNHUB_API_QUOTE_ENDPOINT, symbol.toString()));
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(FINNHUB_TOKEN_HEADER, token)
                    .build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new RuntimeException("Got response with the status code " + HttpStatus.valueOf(response.statusCode()));
            }
            BigDecimal price = new BigDecimal(objectMapper.readValue(response.body(), ObjectNode.class).get("c").asText());
            return price;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to send a request");
        }
    }

    private EnumMap<Order.Symbol, BigDecimal> getAllStockPrices() {
        return Arrays.stream(Order.Symbol.values())
                .collect(Collectors.toMap(
                    Function.identity(),
                    symbol -> {
                        int tries = 0;
                        int maxTries = 3;
                        while (true) {
                            try {
                                return getStockPrice(symbol);
                            } catch (Exception e) {
                                if (++tries == maxTries) {
                                    throw e;
                                }
                            }
                        }
                    },
                    (o1, o2) -> {
                        throw new IllegalArgumentException("Key collision"); // this should technically never happen
                    }, 
                    () -> new EnumMap<>(Order.Symbol.class)
                ));
    }

    private BigDecimal applyCommission(BigDecimal stockPrice) {
        return stockPrice.multiply(BigDecimal.ONE.add(COMMISSION_PERCENT));
    }
    
}
