package com.scr.project.sam.domains.actor.error

import com.mongodb.ErrorCategory
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_NOT_FOUND
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.CONFLICT
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

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(ex: DuplicateKeyException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            ErrorCategory.DUPLICATE_KEY.name,
            "Already existing key",
            "The input request defines an actor that already exists."
        )
        return ResponseEntity(body, CONFLICT)
    }

    data class ErrorResponse(val errorCode: String, val errorReason: String, val message: String)
}

