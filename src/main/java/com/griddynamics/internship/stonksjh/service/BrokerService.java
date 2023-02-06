package com.griddynamics.internship.stonksjh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.griddynamics.internship.stonksjh.exception.trading.InsufficientBalanceException;
import com.griddynamics.internship.stonksjh.exception.trading.InsufficientStockAmountException;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.model.User;
import com.griddynamics.internship.stonksjh.properties.FinnhubProperties;
import com.griddynamics.internship.stonksjh.repository.OrderRepository;
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

@Service
public class BrokerService {

    private static final String ENDPOINT_SUFFIX = "?symbol=%s";
    private static final BigDecimal COMMISSION_MULTIPLIER = new BigDecimal("1.07"); // 7% commission
    private final HttpClient HTTP_CLIENT;
    private final ObjectMapper OBJECT_MAPPER;
    private final FinnhubProperties finnhubProperties;
    private final OrderRepository orderRepository;

    public BrokerService(FinnhubProperties finnhubProperties, OrderRepository orderRepository) {
        this.HTTP_CLIENT = HttpClient.newHttpClient();
        this.OBJECT_MAPPER = new ObjectMapper();
        this.finnhubProperties = finnhubProperties;
        this.orderRepository = orderRepository;
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
                val stocks = owner.getStocks();
                stocks.merge(order.getSymbol(), order.getAmount(), Integer::sum);
            }
            case SELL -> {
                long stockCount = orderRepository.findAllByOwnerUuidAndSymbol(owner.getUuid(), order.getSymbol()).stream()
                        .mapToLong(o -> o.getAmount() * (o.getType() == Order.Type.BUY ? 1 : -1))
                        .sum();
                if (order.getAmount() > stockCount) {
                    throw new InsufficientStockAmountException(order.getAmount(), order.getSymbol(), stockCount);
                }
                val stocks = owner.getStocks();
                if (order.getAmount() > stocks.get(order.getSymbol())) {
                    throw new InsufficientStockAmountException(order.getAmount(), order.getSymbol(), stocks.get(order.getSymbol()));
                }
                stocks.computeIfPresent(order.getSymbol(), (k, v) -> v - order.getAmount());
            }
        }
        owner.setBalance(owner.getBalance().add(orderValue));
    }

    private BigDecimal evaluateOrderValue(Order order) {
        BigDecimal stockPrice = getStockPrice(order.getSymbol());
        return applyCommission(stockPrice.multiply(BigDecimal.valueOf(order.getAmount())));
    }

    private BigDecimal getStockPrice(Order.Symbol symbol) {
        val endpoint = finnhubProperties.apiUrl() + ENDPOINT_SUFFIX;
        try {
            URI uri = URI.create(String.format(endpoint, symbol.toString()));
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header(finnhubProperties.tokenHeader(), finnhubProperties.apiKey())
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new RuntimeException("Got response with the status code " + HttpStatus.valueOf(response.statusCode()));
            }
            return new BigDecimal(OBJECT_MAPPER.readValue(response.body(), ObjectNode.class).get("c").asText());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to send a request");
        }
    }

    private BigDecimal applyCommission(BigDecimal stockPrice) {
        return stockPrice.multiply(COMMISSION_MULTIPLIER);
    }

}
