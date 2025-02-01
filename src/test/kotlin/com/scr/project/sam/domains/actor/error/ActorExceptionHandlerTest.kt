package com.scr.project.sam.domains.actor.error

import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_NOT_FOUND
import com.scr.project.sam.domains.actor.error.ActorExceptionHandler.ErrorResponse
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity

class ActorExceptionHandlerTest {

    private val handler = ActorExceptionHandler()
    private val id = ObjectId.get()
    private val exception = OnActorNotFound(id)

    @Test
    fun handleOnActorNotFoundReturnsCorrectResponse() {
        val response: ResponseEntity<ErrorResponse> = handler.handleOnActorNotFound(exception)
        assertThat(response.statusCode).isEqualTo(NOT_FOUND)
        assertThat(response.body?.errorCode).isEqualTo(ACTOR_NOT_FOUND.name)
        assertThat(response.body?.errorReason).isEqualTo(ACTOR_NOT_FOUND.wording)
        assertThat(response.body?.message).isEqualTo("The actor with id $id was not found")
    }
}