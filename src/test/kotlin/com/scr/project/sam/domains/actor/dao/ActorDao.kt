package com.scr.project.sam.domains.actor.dao

import com.scr.project.sam.domains.actor.model.entity.Actor
import org.bson.types.ObjectId

interface ActorDao {
    fun insertActor(actor: Actor)
    fun getActorById(id: ObjectId): Actor?
    fun countActors(): Long
}