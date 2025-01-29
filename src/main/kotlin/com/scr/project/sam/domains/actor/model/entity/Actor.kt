package com.scr.project.sam.domains.actor.model.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.util.Locale

@Document
data class Actor(
    val surname: String,
    val name: String,
    val nationality: Locale,
    val birthDate: LocalDate,
    val deathDate: LocalDate? = null,
    @field:Id var id: ObjectId? = null
)