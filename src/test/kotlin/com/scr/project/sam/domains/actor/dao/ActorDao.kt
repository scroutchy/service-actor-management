package com.scr.project.sam.domains.actor.dao

import com.scr.project.sam.domains.actor.model.entity.Actor
import org.bson.types.ObjectId

interface ActorDao {
    fun insert(actor: Actor)
    fun insertAll(actors: List<Actor>)
    fun findById(id: ObjectId): Actor?
    fun count(): Long
    fun deleteAll()
    fun initTestData()
}