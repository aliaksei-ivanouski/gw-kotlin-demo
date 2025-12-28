package com.aivanouski.gw_kotlin_demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.autoconfigure.LifecycleMvcEndpointAutoConfiguration

@SpringBootApplication(exclude = [LifecycleMvcEndpointAutoConfiguration::class])
class GwKotlinDemoApplication

fun main(args: Array<String>) {
	runApplication<GwKotlinDemoApplication>(*args)
}
