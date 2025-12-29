package com.aivanouski.gw_kotlin_demo

import com.aivanouski.gw_kotlin_demo.support.RedisTestContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import reactor.netty.DisposableServer
import reactor.netty.http.server.HttpServer
import java.time.Duration

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HttpClientTimeoutTests {

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
    fun `response timeout returns gateway timeout`() {
        webTestClient.get()
            .uri("/__test/timeout/slow")
            .header("Authorization", "Bearer test-token")
            .exchange()
            .expectStatus().isEqualTo(504)
    }

    companion object {
        private var server: DisposableServer? = null

        @JvmStatic
        @DynamicPropertySource
        fun dynamicProperties(registry: DynamicPropertyRegistry) {
            RedisTestContainer.registerProperties(registry)

            if (server == null) {
                server = HttpServer.create()
                    .port(0)
                    .route { routes ->
                        routes.get("/slow") { _, response ->
                            response.sendString(
                                Mono.delay(Duration.ofMillis(500)).map { "slow" }
                            )
                        }
                    }
                    .bindNow()
            }

            registry.add("test.upstream.url") { "http://localhost:${server!!.port()}" }
        }

        @JvmStatic
        @AfterAll
        fun stopServer() {
            server?.disposeNow()
        }
    }
}
