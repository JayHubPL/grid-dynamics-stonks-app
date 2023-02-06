package com.griddynamics.internship.stonksjh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.griddynamics.internship.stonksjh.exception.trading.InsufficientBalanceException;
import com.griddynamics.internship.stonksjh.exception.trading.InsufficientStockAmountException;
import com.griddynamics.internship.stonksjh.model.Order;
import com.griddynamics.internship.stonksjh.model.User;
import com.griddynamics.internship.stonksjh.properties.FinnhubProperties;
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

@Service
@Slf4j
public class BrokerService {

    private static final String ENDPOINT_SUFFIX = "?symbol=%s";
    private static final BigDecimal COMMISSION_MULTIPLIER = new BigDecimal("1.07"); // 7% commission
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final FinnhubProperties finnhubProperties;

    public BrokerService(FinnhubProperties finnhubProperties) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.finnhubProperties = finnhubProperties;
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
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new RuntimeException("Got response with the status code " + HttpStatus.valueOf(response.statusCode()));
            }

            return new BigDecimal(objectMapper.readValue(response.body(), ObjectNode.class).get("c").asText());
        } catch (JsonProcessingException e) {
            val msg = "Failed to parse response from finnhub.io";
            log.error(msg);
            throw new RuntimeException(msg, e);
        } catch (IOException | InterruptedException e) {
            val msg = "Failed to send a request to finnhub.io";
            log.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    private BigDecimal applyCommission(BigDecimal stockPrice) {
        return stockPrice.multiply(COMMISSION_MULTIPLIER);
    }

}
