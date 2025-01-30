package com.scr.project.sam.domains.actor.service

import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.repository.ActorRepository
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
import java.time.LocalDate
import java.util.Locale

class ActorServiceTest {

    private val actorWithoutId = Actor("surname", "name", Locale("", "FR"), LocalDate.of(1980, 1, 1), LocalDate.of(1990, 1, 1))
    private val actor = actorWithoutId.copy(id = ObjectId.get())

    private val actorRepository = mockk<ActorRepository>()
    private val actorService = ActorService(actorRepository)

    @BeforeEach
    internal fun setUp() {
        clearMocks(actorRepository)
        every { actorRepository.insert(actorWithoutId) } answers { actorWithoutId.copy(id = ObjectId.get()).toMono() }
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
        verify(exactly = 1) { actorRepository.insert(actorWithoutId) }
        confirmVerified(actorRepository)
    }

    @Test
    fun `findById should succeed when actor exists`() {
        every { actorRepository.findById(actor.id!!.toHexString()) } answers { actor.toMono() }
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
        verify(exactly = 1) { actorRepository.findById(actor.id!!.toHexString()) }
        confirmVerified(actorRepository)
    }
}