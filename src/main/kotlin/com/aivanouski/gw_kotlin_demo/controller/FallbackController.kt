package com.aivanouski.gw_kotlin_demo.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
class FallbackController {
    private val logger = LoggerFactory.getLogger(FallbackController::class.java)

    @RequestMapping("/__fallback")
    fun fallback(exchange: ServerWebExchange): ResponseEntity<Map<String, String>> {
        val serviceName = exchange.request.headers.getFirst("X-Target-Service") ?: "unknown"
        logger.warn("Circuit breaker fallback triggered for service: {}", serviceName)
        val body = mapOf(
            "error" to "ServiceUnavailable",
            "message" to "Downstream service is unavailable",
            "service" to serviceName
        )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body)
    }
}
