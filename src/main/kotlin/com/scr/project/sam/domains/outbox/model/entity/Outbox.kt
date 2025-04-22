package com.scr.project.sam.domains.outbox.model.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Outbox(
    val aggregateType: String,
    val aggregateId: String,
    val payload: String,
    val topic: String,
    val createdAt: Instant = Instant.now(),
    @Id
    val id: ObjectId = ObjectId.get(),
)