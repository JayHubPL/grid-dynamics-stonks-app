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
    private final BigDecimal COMMISSION_MULTIPLIER = new BigDecimal("1.07"); // 7% commission
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String token;

    private final OrderRepository orderRepository;

    public BrokerService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        try {
            token = Files.readString(FINNHUB_TOKEN_PATH);
        } catch (IOException e) {
            throw new MissingFinnhubApiTokenException();
        }
    }

    public void processOrder(Order order) {
        BigDecimal orderValue = evaluateOrderValue(order);
        User owner = order.getOwner();
        switch (order.getType()) {
            case BUY -> {
                if (owner.getBalance().compareTo(orderValue) < 0) {
                    throw new InsufficientBalanceException(orderValue, owner.getBalance());
                }
                orderValue = orderValue.negate();
            }
            case SELL -> {
                long stockCount = orderRepository.findAllByOwnerUuidAndSymbol(owner.getUuid(), order.getSymbol()).stream()
                        .collect(Collectors.summingLong(o -> o.getAmount() * (o.getType() == Order.Type.BUY ? 1 : -1)));
                if (order.getAmount() > stockCount) {
                    throw new InsufficientStockAmountException(order.getAmount(), order.getSymbol(), stockCount);
                }
            }
        }
        owner.setBalance(owner.getBalance().add(orderValue));
    }

    private BigDecimal evaluateOrderValue(Order order) {
        BigDecimal stockPrice = getStockPrice(order.getSymbol());
        BigDecimal orderValue = applyCommission(stockPrice.multiply(BigDecimal.valueOf(order.getAmount())));
        return orderValue;
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

    private BigDecimal applyCommission(BigDecimal stockPrice) {
        return stockPrice.multiply(COMMISSION_MULTIPLIER);
    }
    
}
