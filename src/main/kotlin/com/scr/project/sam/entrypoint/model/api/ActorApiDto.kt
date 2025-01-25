package com.scr.project.sam.entrypoint.model.api

import org.bson.types.ObjectId
import java.time.LocalDate
import java.util.Locale.IsoCountryCode

data class ActorApiDto(
    val surname: String,
    val name: String,
    val nationality: IsoCountryCode,
    val isAlive: Boolean,
    val birthDate: LocalDate,
    val deathDate: LocalDate? = null,
    var id: ObjectId? = null
)