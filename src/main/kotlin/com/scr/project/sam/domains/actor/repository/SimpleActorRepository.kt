package com.scr.project.sam.domains.actor.repository

import com.scr.project.sam.domains.actor.model.entity.Actor
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SimpleActorRepository : ReactiveMongoRepository<Actor, String>