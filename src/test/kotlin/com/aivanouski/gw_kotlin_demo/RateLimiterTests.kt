package com.aivanouski.gw_kotlin_demo

import com.aivanouski.gw_kotlin_demo.support.RedisTestContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.gateway.config.GatewayProperties
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(RateLimiterTests.DownstreamController::class)
class RateLimiterTests {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var gatewayProperties: GatewayProperties

    @BeforeEach
    fun setUpClient() {
        webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @Test
    fun `rate limiter blocks second request when burst is exhausted`() {
        assertTrue(gatewayProperties.routes.isNotEmpty(), "Gateway routes should be loaded for the test profile")

        webTestClient.get()
            .uri("/api/v1/users/me")
            .header("Authorization", "Bearer test-token")
            .exchange()
            .expectStatus().isOk

        webTestClient.get()
            .uri("/api/v1/users/me")
            .header("Authorization", "Bearer test-token")
            .exchange()
            .expectStatus().isEqualTo(429)
    }

    @RestController
    class DownstreamController {
        @GetMapping("/__test/downstream")
        fun downstream(): Map<String, String> = mapOf("status" to "ok")
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun startRedis() {
            RedisTestContainer.start()
        }

        @JvmStatic
        @AfterAll
        fun stopRedis() {
            RedisTestContainer.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun redisProperties(registry: DynamicPropertyRegistry) {
            RedisTestContainer.registerProperties(registry)
        }
    }
}
