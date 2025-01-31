package com.service.controller;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.CloudEvent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONObject;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@RestController
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private DaprClient client;

    private static final String PUBSUB_NAME = "pubsub";

    @PostConstruct
    public void init() {
        client = new DaprClientBuilder().build();
        Order order = new Order();
        order.setOrderId(-1000);
        // Run publish in the background
        CompletableFuture.runAsync(() -> publish(order).subscribe());
    }

    // Package-private setter for testing purposes
    void setClient(DaprClient client) {
        this.client = client;
    }

    // Publish messages
    @PostMapping(path = "/pubsub/orders", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity> publish(@RequestBody(required = true) Order order) {
        return Mono.fromSupplier(() -> {

            // Publish an event/message using Dapr PubSub
            try {
                int i = order.getOrderId();
                if ( i < 0) {
                    while (i < 0) {
                        order.setOrderId(i);
                        i++;
                        client.publishEvent(PUBSUB_NAME, "orders", order).block();
                        logger.info("Publisher published: " + order.getOrderId());
                        Thread.sleep(100);
                    }
                } else {
                    client.publishEvent(PUBSUB_NAME, "orders", order).block();
                    logger.info("Publisher published: " + order.getOrderId());
                }

                return ResponseEntity.ok("SUCCESS");
            } catch (Exception e) {
                logger.error("Error occurred while publishing order: " + order.getOrderId());
                throw new RuntimeException(e);
            }
        });
    }
}

@Getter
@Setter
@ToString
class Order {
    private int orderId;
}