package com.scr.project.sam.entrypoint.unit.resource

import com.scr.project.sam.domains.actor.error.OnActorNotFound
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.service.ActorService
import com.scr.project.sam.entrypoint.mapper.toEntity
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.resource.ActorResource
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import java.time.LocalDate
import java.util.Locale

class ActorResourceTest {

    private val actorRequest =
        ActorApiDto(
            "surname",
            "name",
            Locale("", "FR"),
            LocalDate.of(1980, 1, 1),
            LocalDate.of(1990, 1, 1),
        )
    private val actorService = mockk<ActorService>()
    private val actorResource = ActorResource(actorService)

    @BeforeEach
    fun setUp() {
        clearMocks(actorService)
        every { actorService.create(any<Actor>()) } answers {
            actorRequest.toEntity().copy(id = ObjectId.get()).toMono()
        }
        every { actorService.findById(any<ObjectId>()) } answers {
            actorRequest.toEntity().copy(id = firstArg()).toMono()
        }
    }

    @Test
    fun `create should succeed and return an actor`() {
        actorResource.create(actorRequest)
            .test()
            .expectSubscription()
            .consumeNextWith {
                with(it.body!!) {
                    assertThat(it).isNotNull()
                    assertThat(id).isNotNull
                    assertThat(surname).isEqualTo(actorRequest.surname)
                    assertThat(name).isEqualTo(actorRequest.name)
                    assertThat(nationality).isEqualTo(actorRequest.nationality)
                    assertThat(birthDate).isEqualTo(actorRequest.birthDate)
                    assertThat(deathDate).isEqualTo(actorRequest.deathDate)
                    assertThat(isAlive).isEqualTo(actorRequest.deathDate == null)
                }
            }
            .verifyComplete()
        verify(exactly = 1) { actorService.create(any<Actor>()) }
        confirmVerified(actorService)
    }

    @Test
    fun `find should succeed and return an actor response when id exits`() {
        val id = ObjectId.get()
        actorResource.find(id)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotNull()
                assertThat(it.id).isEqualTo(id.toHexString())
                assertThat(it.surname).isEqualTo(actorRequest.surname)
                assertThat(it.name).isEqualTo(actorRequest.name)
                assertThat(it.nationality).isEqualTo(actorRequest.nationality)
                assertThat(it.birthDate).isEqualTo(actorRequest.birthDate)
                assertThat(it.deathDate).isEqualTo(actorRequest.deathDate)
                assertThat(it.isAlive).isEqualTo(actorRequest.deathDate == null)
            }
            .verifyComplete()
        verify(exactly = 1) { actorService.findById(id) }
        confirmVerified(actorService)
    }

    @Test
    fun `find should return an exception when actor id is not exist`() {
        val id = ObjectId.get()
        every { actorService.findById(id) } answers { OnActorNotFound(id).toMono() }
        actorResource.find(id)
            .test()
            .expectSubscription()
            .consumeSubscriptionWith {
                verify(exactly = 1) { actorService.findById(id) }
                confirmVerified(actorService)
            }
            .verifyError(OnActorNotFound::class)
    }
}