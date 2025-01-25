package com.scr.project.sam.entrypoint.mapper

import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.entrypoint.model.api.ActorApiDto

fun ActorApiDto.toEntity() = Actor(
    surname,
    name,
    nationality,
    birthDate,
    deathDate,
)

fun Actor.toApiDto() = ActorApiDto(
    surname,
    name,
    nationality,
    deathDate?.let { false } ?: true,
    birthDate,
    deathDate,
    id?.toHexString(),
)