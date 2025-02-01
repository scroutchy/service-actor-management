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
    birthDate,
    deathDate,
    deathDate?.let { false } ?: true,
    id?.toHexString(),
)