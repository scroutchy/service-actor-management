package com.scr.project.sam.entrypoint.model.api

import java.time.LocalDate
import java.util.Locale

data class ActorApiDto(
    val surname: String,
    val name: String,
    val nationality: Locale,
    val isAlive: Boolean,
    val birthDate: LocalDate,
    val deathDate: LocalDate? = null,
    var id: String? = null
)