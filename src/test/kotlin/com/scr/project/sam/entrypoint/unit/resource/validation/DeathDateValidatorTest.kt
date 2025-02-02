package com.scr.project.sam.entrypoint.unit.resource.validation

import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import jakarta.validation.Validation.buildDefaultValidatorFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DeathDateValidatorTest {

    private val validator = buildDefaultValidatorFactory().validator

    @Test
    fun `isValid should succeed when actor death date is after actor birth date`() {
        val actorRequest = ActorApiDto(
            "surname",
            "name",
            "FR",
            LocalDate.of(2000, 1, 1),
            LocalDate.of(2020, 1, 1),
        )
        val violations = validator.validate(actorRequest)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `isValid should fail when actor death date is before actor birth date`() {
        val actorRequest = ActorApiDto(
            "surname",
            "name",
            "FR",
            LocalDate.of(2000, 1, 1),
            LocalDate.of(1990, 1, 1),
        )
        val violations = validator.validate(actorRequest)
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `isValid should succeed when actor death date is not filled`() {
        val actorRequest = ActorApiDto(
            "surname",
            "name",
            "FR",
            LocalDate.of(2000, 1, 1),
        )
        val violations = validator.validate(actorRequest)
        assertThat(violations).isEmpty()
    }
}