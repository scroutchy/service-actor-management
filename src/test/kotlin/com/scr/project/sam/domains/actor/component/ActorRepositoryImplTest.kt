package com.scr.project.sam.domains.actor.component

import com.scr.project.sam.AbstractIntegrationTest
import com.scr.project.sam.domains.actor.dao.ActorDao
import com.scr.project.sam.domains.actor.model.business.ActorUpdateRequest
import com.scr.project.sam.domains.actor.repository.ActorRepositoryImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.kotlin.test.test
import java.time.LocalDate

@SpringBootTest
internal class ActorRepositoryImplTest(
    @Autowired private val repository: ActorRepositoryImpl,
    @Autowired private val actorDao: ActorDao,
) : AbstractIntegrationTest() {

    @BeforeEach
    fun setUp() {
        actorDao.initTestData()
    }

    @Test
    fun `update should update the death date of existing actor`() {
        val actor = actorDao.findAnyBy { it.deathDate != null }!!
        val updateRequest = ActorUpdateRequest(actor.id!!, LocalDate.now())
        repository.update(updateRequest)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.id).isNotNull
                assertThat(it.name).isEqualTo(actor.name)
                assertThat(it.surname).isEqualTo(actor.surname)
                assertThat(it.nationality).isEqualTo(actor.nationality)
                assertThat(it.birthDate).isEqualTo(actor.birthDate)
                assertThat(it.deathDate).isEqualTo(LocalDate.now())
            }
            .verifyComplete()
        val updatedActor = actorDao.findById(actor.id!!)!!
        assertThat(updatedActor.id).isEqualTo(actor.id)
        assertThat(updatedActor.deathDate).isEqualTo(LocalDate.now())
    }
}