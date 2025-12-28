package com.aivanouski.gw_kotlin_demo.config

import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthHeaderFilter : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: org.springframework.cloud.gateway.filter.GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.value()

        if (request.method == HttpMethod.OPTIONS || path.startsWith("/api/v1/auth/")) {
            return chain.filter(exchange)
        }

        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader.isNullOrBlank()) {
            val response = exchange.response
            response.statusCode = HttpStatus.UNAUTHORIZED
            response.headers.contentType = MediaType.APPLICATION_JSON
            val body = """{"error":"Unauthorized","message":"Authentication required"}"""
            val buffer = response.bufferFactory().wrap(body.toByteArray(Charsets.UTF_8))
            return response.writeWith(Mono.just(buffer))
        }

        return chain.filter(exchange)
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
