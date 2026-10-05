package com.multi.shop.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.UnknownHostException;

@Component
public class ServiceUnavailableFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(ServiceUnavailableFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
            .onErrorResume(ServiceUnavailableFilter::cannotConnect, exception -> {
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                log.warn("Service {} is not available: {}", route == null ? "?" : route.getId(), exception.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                return exchange.getResponse().setComplete();
            });
    }

    private static boolean cannotConnect(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause())
            if (cause instanceof ConnectException || cause instanceof UnknownHostException) return true;
        return false;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
