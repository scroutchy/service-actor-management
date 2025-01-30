package com.scr.project.sam.domains.actor.service

import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.ports.ActorPort
import com.scr.project.sam.domains.actor.repository.ActorRepository
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ActorService(val actorRepository: ActorRepository): ActorPort {

    private val logger: Logger = LoggerFactory.getLogger(ActorService::class.java)

    override fun create(actor: Actor): Mono<Actor> {
        return actorRepository.insert(actor)
            .doOnSubscribe { logger.debug("Creating actor") }
            .doOnSuccess { logger.info("Creation of actor with id ${it.id} was successfull.") }
            .doOnError { logger.error("Error when creating actor") }
    }

    override fun findById(id: ObjectId): Mono<Actor> {
        return actorRepository.findById(id.toHexString())
            .doOnSubscribe { logger.debug("Finding actor") }
            .doOnSuccess { logger.debug("Finding actor with id ${it.id} was successfull.") }
            .doOnError { logger.error("Error when finding actor") }
    }
}