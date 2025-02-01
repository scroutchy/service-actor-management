package com.scr.project.sam.entrypoint.integration.resource

import com.scr.project.sam.AbstractIntegrationTest
import com.scr.project.sam.domains.actor.dao.ActorDao
import com.scr.project.sam.domains.actor.error.ActorExceptionHandler.ErrorResponse
import com.scr.project.sam.entrypoint.mapper.toApiDto
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.resource.ApiConstants.ACTOR_PATH
import com.scr.project.sam.entrypoint.resource.ApiConstants.ID_PATH
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun setUp() {
        actorDao.initTestData()
    }

    @Test
    fun `create should succeed and create an actor`() {
        val actorRequest = ActorApiDto(
            "surname",
            "name",
            "FR",
            LocalDate.of(1980, 1, 1),
            LocalDate.of(1990, 1, 1)
        )
        val initialCount = actorDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(ACTOR_PATH)
            .bodyValue(actorRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(ActorApiDto::class.java)
            .consumeWith {
                val body = it.responseBody
                assertThat(body).isNotNull
                with(body!!) {
                    assertThat(id).isNotNull
                    assertThat(surname).isEqualTo(actorRequest.surname)
                    assertThat(name).isEqualTo(actorRequest.name)
                    assertThat(nationalityCode).isEqualTo(actorRequest.nationalityCode)
                    assertThat(birthDate).isEqualTo(actorRequest.birthDate)
                    assertThat(deathDate).isEqualTo(actorRequest.deathDate)
                    assertThat(isAlive).isEqualTo(actorRequest.deathDate == null)
                    assertThat(nationality).isNotNull
                }
                assertThat(actorDao.count()).isEqualTo(initialCount + 1)
                val actor = actorDao.findById(ObjectId(body.id!!))
                with(actor!!) {
                    assertThat(id).isEqualTo(ObjectId(body.id))
                    assertThat(surname).isEqualTo(body.surname)
                    assertThat(name).isEqualTo(body.name)
                    assertThat(nationality).isEqualTo(Locale("", body.nationalityCode))
                    assertThat(birthDate).isEqualTo(body.birthDate)
                    assertThat(deathDate).isEqualTo(body.deathDate)
                }
            }
    }

    @Test
    fun `find should succeed and returns an actor response when id exists`() {
        val actorResponse = actorDao.findAny()!!.toApiDto()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$ACTOR_PATH$ID_PATH", actorResponse.id)
            .exchange()
            .expectStatus().isOk
            .expectBody(ActorApiDto::class.java)
            .consumeWith {
                val body = it.responseBody
                assertThat(body).isNotNull
                with(body!!) {
                    assertThat(id).isEqualTo(actorResponse.id)
                    assertThat(surname).isEqualTo(actorResponse.surname)
                    assertThat(name).isEqualTo(actorResponse.name)
                    assertThat(nationality).isEqualTo(actorResponse.nationality)
                    assertThat(birthDate).isEqualTo(actorResponse.birthDate)
                    assertThat(deathDate).isEqualTo(actorResponse.deathDate)
                    assertThat(isAlive).isEqualTo(actorResponse.isAlive)
                }
            }
    }

    @Test
    fun `find should return 404 when id does not exist`() {
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$ACTOR_PATH$ID_PATH", ObjectId.get().toHexString())
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorResponse::class.java)
    }
}