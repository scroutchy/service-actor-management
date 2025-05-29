package com.scr.project.sam.domains.actor.component

import com.scr.project.sam.AbstractIntegrationTest
import com.scr.project.sam.awaitUntil
import com.scr.project.sam.domains.actor.dao.ActorDao
import com.scr.project.sam.domains.actor.dao.OutboxDao
import com.scr.project.sam.domains.actor.messaging.v1.RewardedMessagingV1
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.repository.ActorRepositoryImpl
import com.scr.project.sam.domains.actor.repository.SimpleActorRepository
import com.scr.project.sam.domains.actor.service.ActorService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.kotlin.test.test
import java.time.LocalDate
import java.util.Locale

@SpringBootTest(properties = ["spring.kafka.enabled=true"])
@ActiveProfiles("kafka")
class ActorServiceComponentTest(
    @Autowired private val simpleActorRepository: SimpleActorRepository,
    @Autowired private val actorRepository: ActorRepositoryImpl,
    @Autowired private val actorMessagingV1: RewardedMessagingV1,
    @Autowired private val actorDao: ActorDao,
    @Autowired private val outboxDao: OutboxDao,
) : AbstractIntegrationTest() {

    private val actorService = ActorService(simpleActorRepository, actorRepository, actorMessagingV1)

    @BeforeEach
    fun setUp() {
        actorDao.initTestData()
        outboxDao.initTestData()
    }

    @AfterEach
    fun clear() {
        outboxDao.deleteAll()
    }

    @Test
    fun `create should succeed and send a message to the outbox`() {
        val actor = Actor("Niney", "Pierre", Locale.Builder().setRegion("FR").build(), LocalDate.of(1989, 1, 1))
        actorService.create(actor)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull
                val id = it.id!!
                val createdActor = actorDao.findById(id)
                assertThat(createdActor).isNotNull
            }.verifyComplete()
        awaitUntil {
            val outboxEvents = outboxDao.findAll()
            assertThat(outboxEvents).hasSize(1)
        }
    }
}