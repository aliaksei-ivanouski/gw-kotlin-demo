package com.aivanouski.gw_kotlin_demo.support

import com.redis.testcontainers.RedisContainer
import org.springframework.test.context.DynamicPropertyRegistry

object RedisTestContainer {
    private val redis = RedisContainer("redis:7.2-alpine")
        .withExposedPorts(6379)

    @Synchronized
    fun start() {
        if (!redis.isRunning) {
            redis.start()
        }
    }

    @Synchronized
    fun stop() {
        if (redis.isRunning) {
            redis.stop()
        }
    }

    fun registerProperties(registry: DynamicPropertyRegistry) {
        start()
        registry.add("spring.data.redis.host") { redis.host }
        registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
    }
}
