package com.scr.project.sam.domains.actor.component

import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.repository.ActorRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.kotlin.test.test
import java.time.LocalDate
import java.util.*


@SpringBootTest
@Testcontainers
internal class ActorRepositoryTest(@Autowired private val actorRepository: ActorRepository) {

    companion object {

        @Container
        val mongoDBContainer = MongoDBContainer("mongo:5.0.3")

        @JvmStatic
        @DynamicPropertySource
        fun mongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }

        @JvmStatic
        @BeforeAll
        fun startContainer() {
            mongoDBContainer.start()
        }

        @JvmStatic
        @AfterAll
        fun stopContainer() {
            mongoDBContainer.stop()
        }
    }

    @Test
    fun `insert should succeed`() {
        val actor = Actor("surname", "name", Locale("", "FR"), LocalDate.of(1980, 1, 1), LocalDate.of(1990, 1, 1))
        actorRepository.insert(actor)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull
                assertThat(it.name).isEqualTo(actor.name)
                assertThat(it.surname).isEqualTo(actor.surname)
                assertThat(it.nationality).isEqualTo(actor.nationality)
                assertThat(it.birthDate).isEqualTo(actor.birthDate)
                assertThat(it.deathDate).isEqualTo(actor.deathDate)
            }
            .verifyComplete()
    }

}