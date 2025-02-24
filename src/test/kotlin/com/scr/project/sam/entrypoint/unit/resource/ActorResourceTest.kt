package com.scr.project.sam.entrypoint.unit.resource

import com.scr.project.sam.domains.actor.error.ActorErrors.OnActorNotFound
import com.scr.project.sam.domains.actor.model.business.ActorUpdateRequest
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.service.ActorService
import com.scr.project.sam.entrypoint.mapper.toEntity
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.model.api.ActorUpdateRequestApiDto
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
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import java.time.LocalDate

class ActorResourceTest {

    private val actorRequest =
        ActorApiDto(
            "surname",
            "name",
            "FR",
            LocalDate.of(1980, 1, 1),
            LocalDate.of(1990, 1, 1),
        )
    private val updateActorRequest = ActorUpdateRequestApiDto(LocalDate.now())
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
                    assertThat(id).isNotNull
                    assertThat(surname).isEqualTo(actorRequest.surname)
                    assertThat(name).isEqualTo(actorRequest.name)
                    assertThat(nationalityCode).isEqualTo(actorRequest.nationalityCode)
                    assertThat(birthDate).isEqualTo(actorRequest.birthDate)
                    assertThat(deathDate).isEqualTo(actorRequest.deathDate)
                    assertThat(isAlive).isEqualTo(actorRequest.deathDate == null)
                    assertThat(nationality).isNotNull
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
                assertThat(it.nationalityCode).isEqualTo(actorRequest.nationalityCode)
                assertThat(it.birthDate).isEqualTo(actorRequest.birthDate)
                assertThat(it.deathDate).isEqualTo(actorRequest.deathDate)
                assertThat(it.isAlive).isEqualTo(actorRequest.deathDate == null)
                assertThat(it.nationality).isNotNull
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

    @Test
    fun `update should succeed and update actor`() {
        val id = ObjectId.get()
        every { actorService.update(ActorUpdateRequest(id, updateActorRequest.deathDate)) } answers {
            actorRequest.toEntity().copy(id = id, deathDate = updateActorRequest.deathDate).toMono()
        }
        actorResource.patch(id, updateActorRequest)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotNull()
                assertThat(it.id).isEqualTo(id.toHexString())
                assertThat(it.surname).isEqualTo(actorRequest.surname)
                assertThat(it.name).isEqualTo(actorRequest.name)
                assertThat(it.nationalityCode).isEqualTo(actorRequest.nationalityCode)
                assertThat(it.birthDate).isEqualTo(actorRequest.birthDate)
                assertThat(it.deathDate).isEqualTo(updateActorRequest.deathDate)
                assertThat(it.isAlive).isFalse()
                assertThat(it.nationality).isNotNull
            }
            .verifyComplete()
        verify(exactly = 1) { actorService.update(ActorUpdateRequest(id, updateActorRequest.deathDate)) }
        confirmVerified(actorService)
    }

    @Test
    fun `update should return an exception when actor id does not exist`() {
        val id = ObjectId.get()
        every { actorService.update(ActorUpdateRequest(id, LocalDate.now())) } answers { OnActorNotFound(id).toMono() }
        actorResource.patch(id, ActorUpdateRequestApiDto(LocalDate.now()))
            .test()
            .expectSubscription()
            .consumeSubscriptionWith {
                verify(exactly = 1) { actorService.update(any()) }
                confirmVerified(actorService)
            }
            .verifyError(OnActorNotFound::class)
    }

    @Test
    fun `list should return only living actors`() {
        val pageable = Pageable.ofSize(10)
        val livingActor = actorRequest.toEntity().copy(id = ObjectId.get(), deathDate = null)
        every { actorService.findAll(any(), any()) } answers { Flux.just(livingActor) }

        actorResource.list(false, pageable)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(1)
                with(it.body!!.first()) {
                    assertThat(it).isNotNull()
                    assertThat(id).isEqualTo(livingActor.id!!.toHexString())
                    assertThat(surname).isEqualTo(livingActor.surname)
                    assertThat(name).isEqualTo(livingActor.name)
                    assertThat(nationalityCode).isNotNull
                    assertThat(birthDate).isEqualTo(livingActor.birthDate)
                    assertThat(deathDate).isNull()
                    assertThat(isAlive).isTrue()
                }
            }
            .verifyComplete()
        verify(exactly = 1) { actorService.findAll(false, pageable) }
        confirmVerified(actorService)
    }

    @Test
    fun `list should return empty list when no actors found`() {
        val pageable = Pageable.ofSize(10)
        every { actorService.findAll(any(), any()) } answers { Flux.empty() }
        actorResource.list(false, pageable)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).isEmpty()
            }
            .verifyComplete()
        verify(exactly = 1) { actorService.findAll(false, pageable) }
        confirmVerified(actorService)
    }
}