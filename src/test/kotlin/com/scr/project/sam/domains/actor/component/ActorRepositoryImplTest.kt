package com.scr.project.sam.domains.actor.component

import com.scr.project.sam.AbstractIntegrationTest
import com.scr.project.sam.domains.actor.dao.ActorDao
import com.scr.project.sam.domains.actor.model.business.ActorUpdateRequest
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.repository.ActorRepositoryImpl
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import reactor.kotlin.test.test
import java.time.LocalDate
import java.util.Locale

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

    @Test
    fun `findAll should return all living actors when input is true and size is lower than page size`() {
        val actors = listOf(
            Actor(
                id = ObjectId.get(),
                name = "John",
                surname = "Doe",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1980, 1, 1),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Jane",
                surname = "Smith",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1990, 2, 2),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Alice",
                surname = "Johnson",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1975, 3, 3),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Bob",
                surname = "Brown",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1985, 4, 4),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Charlie",
                surname = "Davis",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1995, 5, 5),
                deathDate = null
            )
        )
        actorDao.deleteAll()
        actorDao.insertAll(actors)

        actorDao.findAllBy { it.deathDate == null }
        repository.findAll(false, Pageable.ofSize(10).withPage(0))
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).hasSize(5)
                assertThat(it).containsAll(actors)
            }
            .verifyComplete()
    }

    @Test
    fun `findAll should return limited set of actors when page size is lower than total number of actors`() {
        val actors = listOf(
            Actor(
                id = ObjectId.get(),
                name = "John",
                surname = "Doe",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1980, 1, 1),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Jane",
                surname = "Smith",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1990, 2, 2),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Alice",
                surname = "Johnson",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1975, 3, 3),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Bob",
                surname = "Brown",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1985, 4, 4),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Charlie",
                surname = "Davis",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1995, 5, 5),
                deathDate = null
            )
        )
        actorDao.deleteAll()
        actorDao.insertAll(actors)

        actorDao.findAllBy { it.deathDate == null }
        repository.findAll(false, Pageable.ofSize(3).withPage(0))
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).hasSize(3)
                assertThat(actors).containsAll(it)
            }
            .verifyComplete()
    }

    @Test
    fun `findAll should return limited set of actors when page size is lower than total number of actors for second page`() {
        val actors = listOf(
            Actor(
                id = ObjectId.get(),
                name = "John",
                surname = "Doe",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1980, 1, 1),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Jane",
                surname = "Smith",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1990, 2, 2),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Alice",
                surname = "Johnson",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1975, 3, 3),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Bob",
                surname = "Brown",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1985, 4, 4),
                deathDate = null
            ),
            Actor(
                id = ObjectId.get(),
                name = "Charlie",
                surname = "Davis",
                nationality = Locale.Builder().setRegion("US").build(),
                birthDate = LocalDate.of(1995, 5, 5),
                deathDate = null
            )
        )
        actorDao.deleteAll()
        actorDao.insertAll(actors)

        actorDao.findAllBy { it.deathDate == null }
        repository.findAll(false, Pageable.ofSize(3).withPage(1))
            .collectList()
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).hasSize(2)
                assertThat(actors).containsAll(it)
            }
            .verifyComplete()
    }

    @Test
    fun `findAll should return all actors when input is false`() {
        val allActors = actorDao.findAll()
        repository.findAll(true, Pageable.unpaged())
            .collectList()
            .test()
            .expectSubscription()
            .expectNextMatches {
                it.size == allActors.size && it.containsAll(allActors)
            }
            .verifyComplete()
    }

    @Test
    fun `findAll should return empty list when there are no actors`() {
        actorDao.deleteAll()
        repository.findAll(true, Pageable.unpaged())
            .collectList()
            .test()
            .expectSubscription()
            .expectNextMatches {
                it.isEmpty()
            }
            .verifyComplete()
    }
}