package com.scr.project.sam.domains.actor.error

import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_NOT_FOUND
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ActorExceptionHandler {

    @ExceptionHandler(OnActorNotFound::class)
    fun handleOnActorNotFound(ex: OnActorNotFound): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(ACTOR_NOT_FOUND.name, ACTOR_NOT_FOUND.wording, "The actor with id ${ex.id} was not found")
        return ResponseEntity(body, NOT_FOUND)
    }

    data class ErrorResponse(val errorCode: String, val errorReason: String, val message: String)
}

