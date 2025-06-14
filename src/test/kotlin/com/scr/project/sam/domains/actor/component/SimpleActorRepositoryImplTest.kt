package com.scr.project.sam.domains.actor.component

import com.scr.project.sam.AbstractIntegrationTest
import com.scr.project.sam.domains.actor.dao.ActorDao
import com.scr.project.sam.domains.actor.dao.bradPitt
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.repository.SimpleActorRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.kotlin.test.test
import java.time.LocalDate
import java.util.Locale

@SpringBootTest
internal class SimpleActorRepositoryImplTest(
    @Autowired private val simpleActorRepository: SimpleActorRepository,
    @Autowired private val actorDao: ActorDao,
) : AbstractIntegrationTest() {

    @BeforeEach
    fun setUp() {
        actorDao.initTestData()
    }

    @Test
    fun `insert should succeed`() {
        val actor = Actor("surname", "name", Locale("", "FR"), LocalDate.of(1980, 1, 1), LocalDate.of(1990, 1, 1))
        val initialCount = actorDao.count()
        simpleActorRepository.insert(actor)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull
                assertThat(it.name).isEqualTo(actor.name)
                assertThat(it.surname).isEqualTo(actor.surname)
                assertThat(it.nationality).isEqualTo(actor.nationality)
                assertThat(it.birthDate).isEqualTo(actor.birthDate)
                assertThat(it.deathDate).isEqualTo(actor.deathDate)
                assertThat(actorDao.count()).isEqualTo(initialCount + 1)
            }
            .verifyComplete()
    }

    @Test
    fun `findById should succeed when id in database`() {
        val actor = bradPitt()
        simpleActorRepository.findById(actor.id!!.toHexString())
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isEqualTo(actor.id)
                assertThat(it.name).isEqualTo(actor.name)
                assertThat(it.surname).isEqualTo(actor.surname)
                assertThat(it.nationality).isEqualTo(actor.nationality)
                assertThat(it.birthDate).isEqualTo(actor.birthDate)
                assertThat(it.deathDate).isEqualTo(actor.deathDate)
            }
            .verifyComplete()
    }

    @Test
    fun `findById should return null when id not in database`() {
        simpleActorRepository.findById("dummyId")
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }
}