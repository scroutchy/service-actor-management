package com.scr.project.sam.entrypoint.integration.resource

import com.scr.project.sam.AbstractIntegrationTest
import com.scr.project.sam.domains.actor.dao.ActorDao
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.resource.ActorResource.Companion.ACTOR_PATH
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate
import java.util.Locale

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
internal class ActorResourceIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val actorDao: ActorDao,
) : AbstractIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `create should succeed and create an actor`() {
        val actorRequest = ActorApiDto("surname", "name", Locale("fr", "FR"), false, LocalDate.of(1980, 1, 1), LocalDate.of(1990, 1, 1))
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(ACTOR_PATH)
            .bodyValue(actorRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(ActorApiDto::class.java)
            .consumeWith {
                val body = it.responseBody
                assertThat(body).isNotNull
                with(body!!) {
                    assertThat(id).isNotNull
                    assertThat(surname).isEqualTo(actorRequest.surname)
                    assertThat(name).isEqualTo(actorRequest.name)
                    assertThat(nationality).isEqualTo(actorRequest.nationality)
                    assertThat(birthDate).isEqualTo(actorRequest.birthDate)
                    assertThat(deathDate).isEqualTo(actorRequest.deathDate)
                    assertThat(isAlive).isEqualTo(actorRequest.isAlive)
                }
            }
    }
}