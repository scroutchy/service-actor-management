package com.scr.project.sam.domains.actor.error

import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_ALREADY_DEAD
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_NOT_FOUND
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.INCONSISTENT_DEATH_DATE
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.LocalDate

sealed class ActorErrors : RuntimeException() {

    @ResponseStatus(NOT_FOUND)
    class OnActorNotFound(val id: ObjectId) : ActorErrors() {

        override val message = ACTOR_NOT_FOUND.wording
    }

    @ResponseStatus(BAD_REQUEST)
    class OnInconsistentDeathDate(val id: ObjectId, val deathDate: LocalDate) : ActorErrors() {

        override val message = INCONSISTENT_DEATH_DATE.wording
    }

    @ResponseStatus(BAD_REQUEST)
    class OnActorAlreadyDead(val id: ObjectId) : ActorErrors() {

        override val message = ACTOR_ALREADY_DEAD.wording
    }
}