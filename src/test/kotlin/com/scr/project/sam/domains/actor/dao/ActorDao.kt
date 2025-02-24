package com.scr.project.sam.domains.actor.dao

import com.scr.project.commons.cinema.test.dao.GenericDao
import com.scr.project.sam.domains.actor.model.entity.Actor

class ActorDao(mongoUri: String) : GenericDao<Actor>(mongoUri, Actor::class.java, "actor") {

    override fun defaultEntities() = listOf(bradPitt(), jamesDean())
}