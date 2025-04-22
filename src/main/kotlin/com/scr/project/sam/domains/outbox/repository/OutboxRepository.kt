package com.scr.project.sam.domains.outbox.repository

import com.scr.project.sam.domains.outbox.model.entity.Outbox
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface OutboxRepository : ReactiveMongoRepository<Outbox, ObjectId>