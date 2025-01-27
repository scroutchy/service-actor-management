package com.scr.project.sam.entrypoint.unit.resource

import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.service.ActorService
import com.scr.project.sam.entrypoint.mapper.toEntity
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.resource.ActorResource
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import java.time.LocalDate
import java.util.Locale

class ActorResourceTest {

    private val actorRequest = ActorApiDto("surname", "name", Locale("", "FR"), false, LocalDate.of(1980, 1, 1), LocalDate.of(1990, 1, 1))

    private val actorService = mockk<ActorService>()
    private val actorResource = ActorResource(actorService)

    @BeforeEach
    fun setUp() {
        clearMocks(actorService)
        every { actorService.create(any<Actor>()) } answers { actorRequest.toEntity().copy(id = ObjectId.get()).toMono() }
    }

    @Test
    fun `create should succeed and return an actor`() {
        actorResource.create(actorRequest)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotNull()
                assertThat(it.id).isNotNull()
                assertThat(it.surname).isEqualTo(actorRequest.surname)
                assertThat(it.name).isEqualTo(actorRequest.name)
                assertThat(it.nationality).isEqualTo(actorRequest.nationality)
                assertThat(it.birthDate).isEqualTo(actorRequest.birthDate)
                assertThat(it.deathDate).isEqualTo(actorRequest.deathDate)
                assertThat(it.isAlive).isEqualTo(actorRequest.isAlive)
            }
            .verifyComplete()
    }
}