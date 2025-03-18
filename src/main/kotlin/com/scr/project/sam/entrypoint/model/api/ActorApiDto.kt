package com.scr.project.sam.entrypoint.model.api

import com.scr.project.commons.cinema.model.api.DTO
import com.scr.project.sam.entrypoint.resource.validation.IsValidDeathDate
import com.scr.project.sam.entrypoint.resource.validation.ValidationGroups.ActorRequest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Null
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Pattern
import org.jetbrains.annotations.NotNull
import java.time.LocalDate

@IsValidDeathDate
data class ActorApiDto(
    @field:NotBlank
    val surname: String,
    @field:NotBlank
    val name: String,
    @field:NotNull
    @field:Pattern(regexp = "^[A-Z]{2}$")
    val nationalityCode: String,
    @field:NotNull
    @field:PastOrPresent
    val birthDate: LocalDate,
    @field:PastOrPresent
    val deathDate: LocalDate? = null,
    @field:Null(groups = [ActorRequest::class], message = "isAlive must be null in request")
    val isAlive: Boolean? = null,
    @field:Null(groups = [ActorRequest::class])
    val nationality: String? = null,
    @field:Null(groups = [ActorRequest::class])
    var id: String? = null
) : DTO