package com.scr.project.sam.entrypoint.integration.resource

import com.scr.project.sam.AbstractIntegrationTest
import com.scr.project.sam.RewardedKafkaTestConsumer
import com.scr.project.sam.TestKafkaConfig
import com.scr.project.sam.awaitUntil
import com.scr.project.sam.domains.actor.dao.ActorDao
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.resource.ApiConstants.ACTOR_PATH
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate
import java.util.Locale

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["spring.kafka.enabled=false"])
@AutoConfigureWebTestClient
@AutoConfigureRestDocs
@Import(TestKafkaConfig::class)
class ActorResourceNoKafkaIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val actorDao: ActorDao,
    @Autowired private val kafkaRewardedConsumer: RewardedKafkaTestConsumer,
) : AbstractIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        actorDao.initTestData()
        kafkaRewardedConsumer.clearTopic()
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
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .build()
            .post()
            .uri(ACTOR_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(actorRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(ActorApiDto::class.java)
            .consumeWith {
                val body = it.responseBody as ActorApiDto
                assertThat(body).isNotNull
                with(body) {
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
                awaitUntil {
                    val messages = kafkaRewardedConsumer.poll()
                    assertThat(messages).hasSize(0)
                }
            }
    }
}