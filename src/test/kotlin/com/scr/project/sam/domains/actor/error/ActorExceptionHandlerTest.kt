package com.scr.project.sam.domains.actor.error

import com.mongodb.ErrorCategory
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_NOT_FOUND
import com.scr.project.sam.domains.actor.error.ActorExceptionHandler.ErrorResponse
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity

class ActorExceptionHandlerTest {

    private val handler = ActorExceptionHandler()

    @Test
    fun `handle OnActorNotFound returns correct response`() {
        val id = ObjectId.get()
        val response: ResponseEntity<ErrorResponse> = handler.handleOnActorNotFound(OnActorNotFound(id))
        assertThat(response.statusCode).isEqualTo(NOT_FOUND)
        assertThat(response.body?.errorCode).isEqualTo(ACTOR_NOT_FOUND.name)
        assertThat(response.body?.errorReason).isEqualTo(ACTOR_NOT_FOUND.wording)
        assertThat(response.body?.message).isEqualTo("The actor with id $id was not found")
    }

    @Test
    fun `handle DuplicateKeyException returns correct response`() {
        val response: ResponseEntity<ErrorResponse> = handler.handleDuplicateKeyException(DuplicateKeyException("message"))
        assertThat(response.statusCode).isEqualTo(CONFLICT)
        assertThat(response.body?.errorCode).isEqualTo(ErrorCategory.DUPLICATE_KEY.name)
        assertThat(response.body?.errorReason).isEqualTo("Already existing key")
        assertThat(response.body?.message).isEqualTo("The input request defines an actor that already exists.")
    }
}