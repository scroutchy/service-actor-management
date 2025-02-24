package com.scr.project.sam.domains.actor.service

import com.scr.project.sam.domains.actor.error.ActorErrors.OnActorAlreadyDead
import com.scr.project.sam.domains.actor.error.ActorErrors.OnActorNotFound
import com.scr.project.sam.domains.actor.error.ActorErrors.OnInconsistentDeathDate
import com.scr.project.sam.domains.actor.model.business.ActorUpdateRequest
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.repository.ActorRepositoryImpl
import com.scr.project.sam.domains.actor.repository.SimpleActorRepository
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
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.kotlin.test.verifyError
import java.time.LocalDate
import java.util.Locale
import kotlin.random.Random

class ActorServiceTest {

    private val actorWithoutId =
        Actor("surname", "name", Locale.Builder().setRegion("FR").build(), LocalDate.of(1980, 1, 1), LocalDate.of(1990, 1, 1))
    private val actor = actorWithoutId.copy(id = ObjectId.get())
    private val actorToUpdate = actor.copy(deathDate = null)
    private val simpleActorRepository = mockk<SimpleActorRepository>()
    private val actorRepository = mockk<ActorRepositoryImpl>()
    private val actorService = ActorService(simpleActorRepository, actorRepository)

    @BeforeEach
    internal fun setUp() {
        clearMocks(simpleActorRepository, actorRepository)
        every { simpleActorRepository.insert(actorWithoutId) } answers { actorWithoutId.copy(id = ObjectId.get()).toMono() }
    }

    @Test
    fun `create should succeed and create an actor`() {
        actorService.create(actorWithoutId)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull()
                assertThat(it.name).isEqualTo(actorWithoutId.name)
                assertThat(it.surname).isEqualTo(actorWithoutId.surname)
                assertThat(it.nationality).isEqualTo(actorWithoutId.nationality)
                assertThat(it.birthDate).isEqualTo(actorWithoutId.birthDate)
                assertThat(it.deathDate).isEqualTo(actorWithoutId.deathDate)
            }.verifyComplete()
        verify(exactly = 1) { simpleActorRepository.insert(actorWithoutId) }
        confirmVerified(simpleActorRepository)
    }

    @Test
    fun `findById should succeed when actor exists`() {
        every { simpleActorRepository.findById(actor.id!!.toHexString()) } answers { actor.toMono() }
        actorService.findById(actor.id!!)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isEqualTo(actor.id)
                assertThat(it.name).isEqualTo(actor.name)
                assertThat(it.surname).isEqualTo(actor.surname)
                assertThat(it.nationality).isEqualTo(actor.nationality)
                assertThat(it.birthDate).isEqualTo(actor.birthDate)
                assertThat(it.deathDate).isEqualTo(actor.deathDate)
            }.verifyComplete()
        verify(exactly = 1) { simpleActorRepository.findById(actor.id!!.toHexString()) }
        confirmVerified(simpleActorRepository)
    }

    @Test
    fun `findById should return exception when actor does not exist`() {
        every { simpleActorRepository.findById(actor.id!!.toHexString()) } answers { Mono.empty() }
        actorService.findById(actor.id!!)
            .test()
            .expectSubscription()
            .consumeSubscriptionWith {
                verify(exactly = 1) { simpleActorRepository.findById(actor.id!!.toHexString()) }
                confirmVerified(simpleActorRepository)
            }.verifyError(OnActorNotFound::class)
    }

    @Test
    fun `update should succeed`() {
        val updateRequest = ActorUpdateRequest(actorToUpdate.id!!, LocalDate.now())
        every { simpleActorRepository.findById(actorToUpdate.id!!.toHexString()) } answers { actorToUpdate.toMono() }
        every { actorRepository.update(updateRequest) } answers { actorToUpdate.copy(deathDate = updateRequest.deathDate).toMono() }
        actorService.update(updateRequest)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isEqualTo(actorToUpdate.id)
                assertThat(it.name).isEqualTo(actorToUpdate.name)
                assertThat(it.surname).isEqualTo(actorToUpdate.surname)
                assertThat(it.nationality).isEqualTo(actorToUpdate.nationality)
                assertThat(it.birthDate).isEqualTo(actorToUpdate.birthDate)
                assertThat(it.deathDate).isEqualTo(updateRequest.deathDate)
            }
            .verifyComplete()
        verify(exactly = 1) { simpleActorRepository.findById(actor.id!!.toHexString()) }
        verify(exactly = 1) { actorRepository.update(updateRequest) }
        confirmVerified(simpleActorRepository, actorRepository)
    }

    @Test
    fun `update should fail and return exception in case the death date is prior to birth date`() {
        val updateRequest = ActorUpdateRequest(actorToUpdate.id!!, actorToUpdate.birthDate.minusDays(1))
        every { simpleActorRepository.findById(actorToUpdate.id!!.toHexString()) } answers { actorToUpdate.toMono() }
        every { actorRepository.update(updateRequest) } answers { actor.copy(deathDate = updateRequest.deathDate).toMono() }
        actorService.update(updateRequest)
            .test()
            .expectSubscription()
            .consumeSubscriptionWith {
                verify(exactly = 1) { simpleActorRepository.findById(actor.id!!.toHexString()) }
                verify(inverse = true) { actorRepository.update(updateRequest) }
                confirmVerified(simpleActorRepository, actorRepository)
            }.verifyError(OnInconsistentDeathDate::class)
    }

    @Test
    fun `update should fail and return exception in case the actor is already dead at update time`() {
        val updateRequest = ActorUpdateRequest(actor.id!!, LocalDate.now())
        every { simpleActorRepository.findById(actorToUpdate.id!!.toHexString()) } answers { actor.toMono() }
        every { actorRepository.update(updateRequest) } answers { actor.copy(deathDate = updateRequest.deathDate).toMono() }
        actorService.update(updateRequest)
            .test()
            .expectSubscription()
            .consumeSubscriptionWith {
                verify(exactly = 1) { simpleActorRepository.findById(actor.id!!.toHexString()) }
                verify(inverse = true) { actorRepository.update(updateRequest) }
                confirmVerified(simpleActorRepository, actorRepository)
            }.verifyError(OnActorAlreadyDead::class)
    }

    @Test
    fun `findAll should list all actors`() {
        val actors = generateListOfActors()
        every { actorRepository.findAll(true, any()) } answers { actors.toFlux() }
        actorService.findAll(true, Pageable.unpaged())
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).hasSize(actors.size)
                assertThat(it).containsAll(actors)
            }
            .verifyComplete()
        verify(exactly = 1) { actorRepository.findAll(true, any()) }
        confirmVerified(actorRepository)
    }

    @Test
    fun `findAll should list living actors only`() {
        val livingActors = generateListOfActors().filter { it.deathDate != null }
        every { actorRepository.findAll(false, any()) } answers { livingActors.toFlux() }
        actorService.findAll(false, Pageable.unpaged())
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).hasSize(livingActors.size)
                assertThat(it).containsAll(livingActors)
            }
            .verifyComplete()
        verify(exactly = 1) { actorRepository.findAll(false, any()) }
        confirmVerified(actorRepository)
    }

    @Test
    fun `findAll should return empty list when nothing to list`() {
        every { actorRepository.findAll(true, any()) } answers { emptyList<Actor>().toFlux() }
        actorService.findAll(true, Pageable.unpaged())
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isEmpty()
            }
            .verifyComplete()
        verify(exactly = 1) { actorRepository.findAll(true, any()) }
        confirmVerified(actorRepository)
    }

    private fun generateListOfActors(): List<Actor> {
        return List(10) {
            actor.copy(id = ObjectId.get(), deathDate = Random.nextBoolean().takeIf { it }.let { actor.deathDate })
        }
    }
}