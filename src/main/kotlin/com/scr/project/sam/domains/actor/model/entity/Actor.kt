package com.scr.project.sam.domains.actor.model.entity

import com.scr.project.commons.cinema.model.entity.Auditable
import org.bson.codecs.pojo.annotations.BsonId
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
    @field:Id @BsonId var id: ObjectId? = null
) : Auditable()