package com.scr.project.sam.entrypoint.mapper

import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import java.util.Locale

fun ActorApiDto.toEntity() = Actor(
    surname,
    name,
    Locale.Builder().setRegion(nationalityCode).build(),
    birthDate,
    deathDate,
)

fun Actor.toApiDto() = ActorApiDto(
    surname,
    name,
    nationality.country,
    birthDate,
    deathDate,
    deathDate == null,
    nationality.displayCountry,
    id?.toHexString(),
)