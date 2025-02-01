package com.scr.project.sam.entrypoint.unit.mapper

import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.entrypoint.mapper.toApiDto
import com.scr.project.sam.entrypoint.mapper.toEntity
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Locale

class ActorMappingsTest {

    @Test
    fun `toEntity should succeed`() {
        val actorApiDto = ActorApiDto(
            "surname",
            "name",
            Locale("", "FR"),
            LocalDate.of(1980, 1 ,1),
            LocalDate.of(1990, 1, 1),
        )
        val actor = actorApiDto.toEntity()
        assertThat(actor).isNotNull
        assertThat(actor.surname).isEqualTo(actorApiDto.surname)
        assertThat(actor.name).isEqualTo(actorApiDto.name)
        assertThat(actor.nationality).isEqualTo(actorApiDto.nationality)
        assertThat(actor.birthDate).isEqualTo(actorApiDto.birthDate)
        assertThat(actor.deathDate).isEqualTo(actorApiDto.deathDate)
    }

    @Test
    fun `toApiDto should succeed when death date is filled`() {
        val actor = Actor(
            "surname",
            "name",
            Locale("", "FR"),
            LocalDate.of(1980, 1, 1),
            LocalDate.of(1990, 1, 1),
            ObjectId.get()
        )
        val actorApiDto = actor.toApiDto()
        assertThat(actorApiDto).isNotNull
        assertThat(actorApiDto.surname).isEqualTo(actor.surname)
        assertThat(actorApiDto.name).isEqualTo(actor.name)
        assertThat(actorApiDto.nationality).isEqualTo(actor.nationality)
        assertThat(actorApiDto.birthDate).isEqualTo(actor.birthDate)
        assertThat(actorApiDto.deathDate).isEqualTo(actor.deathDate)
        assertThat(actorApiDto.isAlive).isFalse()
        assertThat(actorApiDto.id).isEqualTo(actor.id?.toHexString())
    }

    @Test
    fun `toApiDto should succeed when death date is null`() {
        val actor = Actor(
            "surname",
            "name",
            Locale("", "FR"),
            LocalDate.of(1980, 1, 1),
            null,
            ObjectId.get()
        )
        val actorApiDto = actor.toApiDto()
        assertThat(actorApiDto).isNotNull
        assertThat(actorApiDto.surname).isEqualTo(actor.surname)
        assertThat(actorApiDto.name).isEqualTo(actor.name)
        assertThat(actorApiDto.nationality).isEqualTo(actor.nationality)
        assertThat(actorApiDto.birthDate).isEqualTo(actor.birthDate)
        assertThat(actorApiDto.deathDate).isNull()
        assertThat(actorApiDto.isAlive).isTrue()
        assertThat(actorApiDto.id).isEqualTo(actor.id?.toHexString())
    }
}