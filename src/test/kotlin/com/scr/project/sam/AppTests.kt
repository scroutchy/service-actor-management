package com.scr.project.sam

import com.scr.project.sam.entrypoint.resource.ApiConstants.ACTUATOR_PATH
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AppTests(@Autowired private val webTestClient: WebTestClient) : AbstractIntegrationTest() {

	@Test
	fun contextLoads() {
	}


    @Test
    fun `health should succeed`() {
        webTestClient.get()
            .uri("$ACTUATOR_PATH/health")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
    }

}
