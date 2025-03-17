package com.scr.project.sam.domains.actor.error

import com.mongodb.ErrorCategory.DUPLICATE_KEY
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_ALREADY_DEAD
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_NOT_FOUND
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.INCONSISTENT_DEATH_DATE
import com.scr.project.sam.domains.actor.error.ActorErrors.OnActorAlreadyDead
import com.scr.project.sam.domains.actor.error.ActorErrors.OnActorNotFound
import com.scr.project.sam.domains.actor.error.ActorErrors.OnInconsistentDeathDate
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ActorExceptionHandler {

    @ExceptionHandler(OnActorNotFound::class)
    @ResponseStatus(NOT_FOUND)
    fun handleOnActorNotFound(ex: OnActorNotFound): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(ACTOR_NOT_FOUND.name, ACTOR_NOT_FOUND.wording, "The actor with id ${ex.id} was not found")
        return ResponseEntity(body, NOT_FOUND)
    }

    @ExceptionHandler(OnInconsistentDeathDate::class)
    @ResponseStatus(BAD_REQUEST)
    fun handleOnInconsistentDeathDate(ex: OnInconsistentDeathDate): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            INCONSISTENT_DEATH_DATE.name,
            INCONSISTENT_DEATH_DATE.wording,
            "Death date ${ex.deathDate} is prior to birth date of actor ${ex.id}."
        )
        return ResponseEntity(body, BAD_REQUEST)
    }

    @ExceptionHandler(OnActorAlreadyDead::class)
    @ResponseStatus(BAD_REQUEST)
    fun handleOnActorAlreadyDead(ex: OnActorAlreadyDead): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(ACTOR_ALREADY_DEAD.name, ACTOR_ALREADY_DEAD.wording, "Actor with id ${ex.id} is already dead.")
        return ResponseEntity(body, BAD_REQUEST)
    }

    @ExceptionHandler(DuplicateKeyException::class)
    @ResponseStatus(CONFLICT)
    fun handleDuplicateKeyException(ex: DuplicateKeyException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            DUPLICATE_KEY.name,
            "Already existing key",
            "The input request defines an actor that already exists."
        )
        return ResponseEntity(body, CONFLICT)
    }

    data class ErrorResponse(val errorCode: String, val errorReason: String, val message: String)
}

