package com.scr.project.sam

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.reactive.server.WebTestClient

@TestConfiguration
class TestConfig {

    @Bean
    fun webTestClient(): WebTestClient {
        return WebTestClient.bindToServer()
            .baseUrl("http://localhost:8080")
            .build()
    }
}