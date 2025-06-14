package com.scr.project.sam.domains.actor.service

import com.scr.project.sam.domains.actor.error.ActorErrors.OnActorAlreadyDead
import com.scr.project.sam.domains.actor.error.ActorErrors.OnActorNotFound
import com.scr.project.sam.domains.actor.error.ActorErrors.OnInconsistentDeathDate
import com.scr.project.sam.domains.actor.messaging.v1.RewardedMessagingV1
import com.scr.project.sam.domains.actor.model.business.ActorUpdateRequest
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.domains.actor.ports.ActorPort
import com.scr.project.sam.domains.actor.repository.ActorRepositoryImpl
import com.scr.project.sam.domains.actor.repository.SimpleActorRepository
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class ActorService(
    val simpleActorRepository: SimpleActorRepository,
    val actorRepository: ActorRepositoryImpl,
    val actorMessagingV1: RewardedMessagingV1
) : ActorPort {

    private val logger: Logger = LoggerFactory.getLogger(ActorService::class.java)

    @Transactional
    override fun create(actor: Actor): Mono<Actor> {
        return simpleActorRepository.insert(actor)
            .flatMap(actorMessagingV1::notify)
            .doOnSubscribe { logger.debug("Creating actor") }
            .doOnSuccess { logger.info("Creation of actor with id ${it.id} was successfull.") }
            .doOnError { logger.error("Error when creating actor") }
    }

    override fun findById(id: ObjectId): Mono<Actor> {
        return simpleActorRepository.findById(id.toHexString())
            .doOnSubscribe { logger.debug("Finding actor") }
            .switchIfEmpty { Mono.error(OnActorNotFound(id)) }
            .doOnSuccess { logger.debug("Finding actor with id ${it.id} was successfull.") }
            .doOnError { logger.warn("Error when finding actor with id $id") }
    }

    override fun update(updateRequest: ActorUpdateRequest): Mono<Actor> {
        return findById(updateRequest.id)
            .map { validateDeathDate(updateRequest, it) }
            .doOnSubscribe { logger.debug("Updating actor with id {${updateRequest.id}}") }
            .flatMap(actorRepository::update)
            .doOnSuccess { logger.info("Update of actor with id {${updateRequest.id}} was successfull.") }
            .doOnError { logger.warn("Error when updating actor with id {${updateRequest.id}}") }
    }

    override fun findAll(includeDead: Boolean, pageable: Pageable): Flux<Actor> {
        return actorRepository.findAll(includeDead, pageable)
            .doOnSubscribe { logger.debug("Listing actors") }
            .doOnComplete { logger.debug("Listing actors was successful.") }
            .doOnError { logger.warn("Error when listing actors") }
    }

    private fun validateDeathDate(updateRequest: ActorUpdateRequest, actor: Actor): ActorUpdateRequest {
        updateRequest.deathDate.takeIf { it.isBefore(actor.birthDate) }
            ?.let { throw OnInconsistentDeathDate(updateRequest.id, updateRequest.deathDate) }
        actor.deathDate?.let { throw OnActorAlreadyDead(updateRequest.id) }
        return updateRequest
    }
}