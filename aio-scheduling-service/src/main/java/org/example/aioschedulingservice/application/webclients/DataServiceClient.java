package org.example.aioschedulingservice.application.webclients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.example.aioschedulingservice.application.exceptions.DataServiceException;
import org.example.aioschedulingservice.application.exceptions.DataServiceNonRetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DataServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(DataServiceClient.class);

    @Qualifier("dataServiceWebClient")
    private final WebClient dataServiceWebClient;

    @CircuitBreaker(name = "dataService", fallbackMethod = "getStaffForGroupFallback")
    @Retry(name = "dataService")
    public Set<String> getStaffForGroup(String staffGroupId) {
        logger.info("Fetching staff for group: {}", staffGroupId);
        return dataServiceWebClient.get()
                .uri("/staff-groups/{id}/members", staffGroupId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> {
                    logger.warn("Staff group {} not found in Data Service.", staffGroupId);
                    return Mono.error(new DataServiceNonRetryableException("Staff group not found: " + staffGroupId));
                })
                .onStatus(status -> status.is4xxClientError() && status != HttpStatus.NOT_FOUND, clientResponse -> {
                    logger.error("Client error from Data Service for group {}: {}", staffGroupId, clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new DataServiceNonRetryableException("Client error from Data Service: " + clientResponse.statusCode() + " - " + body)));
                })
                .onStatus(status -> status.is5xxServerError(),
                        clientResponse -> {
                            logger.error("Server error from DataService for group {}: {}", staffGroupId, clientResponse.statusCode());
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new DataServiceException("Server error from Data Service: " + clientResponse.statusCode() + " - " + body)));
                        })
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
                .block();
    }

    public Set<String> getStaffForGroupFallback(String staffGroupId, Throwable t) {
        logger.error("Fallback for getStaffForGroup (groupId: {}): Circuit breaker opened or retry exhausted. Error: {}", staffGroupId, t.getMessage());
        return Collections.emptySet();
    }

}
