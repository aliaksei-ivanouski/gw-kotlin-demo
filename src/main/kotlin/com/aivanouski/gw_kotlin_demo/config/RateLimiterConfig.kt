package com.aivanouski.gw_kotlin_demo.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
class RateLimiterConfig {

    @Bean
    fun userKeyResolver(): KeyResolver {
        return KeyResolver { exchange: ServerWebExchange ->
            // Resolve key based on user identity (e.g., header, IP, or principal)
            // For now, falling back to IP address if user is not authenticated
            val ip = exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
            Mono.just(ip)
        }
    }
}
