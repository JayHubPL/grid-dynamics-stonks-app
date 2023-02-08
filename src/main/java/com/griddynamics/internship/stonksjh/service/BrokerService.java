package com.griddynamics.internship.stonksjh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.griddynamics.internship.stonksjh.exception.broker.InsufficientBalanceException;
import com.griddynamics.internship.stonksjh.exception.broker.InsufficientStockAmountException;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.model.User;
import com.griddynamics.internship.stonksjh.properties.FinnhubProperties;
import com.griddynamics.internship.stonksjh.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BrokerService {

    private static final String ENDPOINT_SUFFIX = "?symbol=%s";
    private static final BigDecimal COMMISSION_PERCENT = new BigDecimal("0.07"); // 7% commission
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final FinnhubProperties finnhubProperties;
    private Map<Order.Symbol, BigDecimal> stockPrices;

    public BrokerService(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            OrderRepository orderRepository,
            FinnhubProperties finnhubProperties
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.finnhubProperties = finnhubProperties;
        initializeDaemonThread(1);
    }

    public void checkIfOrderCanBePlaced(Order order) {
        User owner = order.getOwner();
        val stocks = owner.getStocks();
        val stockCount = stocks.get(order.getSymbol());
        switch (order.getType()) {
            case BUY -> {
                BigDecimal actualBalance = owner.getBalance().subtract(getPendingExpenditures(owner));
                BigDecimal orderValue = evaluateOrderValue(order);
                if (actualBalance.compareTo(orderValue) < 0) {
                    throw new InsufficientBalanceException(orderValue, actualBalance);
                }
            }
            case SELL -> {
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
        scheduler.scheduleAtFixedRate(() -> {
            stockPrices = getAllStockPrices();
            orderRepository.findAllByStatus(Order.Status.PENDING)
                    .forEach(this::processOrder);
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
        val stocks = owner.getStocks();
        switch (order.getType()) {
            case BUY -> {
                BigDecimal actualBalance = owner.getBalance().subtract(getPendingExpenditures(owner));
                if (applyCommission(order.getBid()).compareTo(stockPrices.get(order.getSymbol())) < 0 || actualBalance.compareTo(orderValue) < 0) {
                    return; // can't be processed yet
                }
                stocks.merge(order.getSymbol(), order.getAmount(), Integer::sum);
                owner.setBalance(owner.getBalance().subtract(orderValue));
            }
            case SELL -> {
                if (order.getBid().compareTo(stockPrices.get(order.getSymbol())) < 0) {
                    return; // can't be processed yet
                }
                stocks.computeIfPresent(order.getSymbol(), (k, v) -> v - order.getAmount());
                owner.setBalance(owner.getBalance().add(orderValue.multiply(BigDecimal.ONE.subtract(COMMISSION_PERCENT))));
            }
        }
        order.setStatus(Order.Status.COMPLETE);
        orderRepository.save(order);
    }

    private BigDecimal evaluateOrderValue(Order order) {
        return applyCommission(order.getBid().multiply(BigDecimal.valueOf(order.getAmount())));
    }

    private Optional<BigDecimal> getStockPrice(Order.Symbol symbol) {
        val endpoint = finnhubProperties.apiUrl() + ENDPOINT_SUFFIX;
        try {
            URI uri = URI.create(String.format(endpoint, symbol.toString()));
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(finnhubProperties.tokenHeader(), finnhubProperties.apiKey())
                    .build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new RuntimeException("Got response with the status code " + HttpStatus.valueOf(response.statusCode()));
            }

            return Optional.of(
                    new BigDecimal(objectMapper.readValue(response.body(), ObjectNode.class).get("c").asText())
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse response from finnhub.io");
            return Optional.empty();
        } catch (IOException | InterruptedException e) {
            log.error("Failed to send a request to finnhub.io");
            return Optional.empty();
        }
    }

    private Map<Order.Symbol, BigDecimal> getAllStockPrices() {
        return Arrays.stream(Order.Symbol.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        symbol -> getStockPrice(symbol)
                                .orElse(stockPrices.get(symbol)),
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
