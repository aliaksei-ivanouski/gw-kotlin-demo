package com.aivanouski.gw_kotlin_demo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(CircuitBreakerTests.BoomController::class)
class CircuitBreakerTests {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUpClient() {
        webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @Test
    fun `circuit breaker returns fallback with service name`() {
        webTestClient.get()
            .uri("/__test/cb/trigger")
            .header("Authorization", "Bearer test-token")
            .exchange()
            .expectStatus().isEqualTo(503)
            .expectBody()
            .jsonPath("$.error").isEqualTo("ServiceUnavailable")
            .jsonPath("$.service").isEqualTo("cb-test-service")
    }

    @RestController
    class BoomController {
        @GetMapping("/__test/boom")
        fun boom(): String {
            throw IllegalStateException("boom")
        }
    }
}
