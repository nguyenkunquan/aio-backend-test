package org.example.aioschedulingservice.infrastructure.configs;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${external.data-service.url}")
    private String dataServiceBaseUrl;

    @Value("${external.data-service.connection-timeout}")
    private Duration connectionTimeout;

    @Value("${external.data-service.response-timeout}")
    private Duration responseTimeout;

    @Value("${external.data-service.reconnection-timeout}")
    private Duration reconnectionTimeout;

    @Bean(name = "dataServiceWebClient")
    public WebClient dataServiceWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectionTimeout.toSeconds())
                .responseTimeout(responseTimeout)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(reconnectionTimeout.toSeconds(), TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(reconnectionTimeout.toSeconds(), TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(dataServiceBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
