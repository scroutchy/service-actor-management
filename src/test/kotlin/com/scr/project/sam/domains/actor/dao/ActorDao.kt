package com.scr.project.sam.domains.actor.dao

import com.scr.project.sam.domains.actor.model.entity.Actor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

@Repository
class ActorDao(@Value("\${spring.data.mongodb.uri}") private val mongoUri: String) :
    GenericDao<Actor>(mongoUri, Actor::class.java, "actor") {

    override fun defaultEntities() = listOf(bradPitt(), jamesDean())
}