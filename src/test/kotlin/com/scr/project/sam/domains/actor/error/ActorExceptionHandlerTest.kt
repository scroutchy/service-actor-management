package com.scr.project.sam.domains.actor.error

import com.mongodb.ErrorCategory
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_ALREADY_DEAD
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_NOT_FOUND
import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.INCONSISTENT_DEATH_DATE
import com.scr.project.sam.domains.actor.error.ActorErrors.OnActorAlreadyDead
import com.scr.project.sam.domains.actor.error.ActorErrors.OnActorNotFound
import com.scr.project.sam.domains.actor.error.ActorErrors.OnInconsistentDeathDate
import com.scr.project.sam.domains.actor.error.ActorExceptionHandler.ErrorResponse
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import java.time.LocalDate

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
    fun `handle OnInconsistentDeathDate returns correct response`() {
        val id = ObjectId.get()
        val response: ResponseEntity<ErrorResponse> = handler.handleOnInconsistentDeathDate(OnInconsistentDeathDate(id, LocalDate.now()))
        assertThat(response.statusCode).isEqualTo(BAD_REQUEST)
        assertThat(response.body?.errorCode).isEqualTo(INCONSISTENT_DEATH_DATE.name)
        assertThat(response.body?.errorReason).isEqualTo(INCONSISTENT_DEATH_DATE.wording)
        assertThat(response.body?.message).isEqualTo("Death date ${LocalDate.now()} is prior to birth date of actor ${id}.")
    }

    @Test
    fun `handle OnActorAlreadyDead returns correct response`() {
        val id = ObjectId.get()
        val response: ResponseEntity<ErrorResponse> = handler.handleOnActorAlreadyDead(OnActorAlreadyDead(id))
        assertThat(response.statusCode).isEqualTo(BAD_REQUEST)
        assertThat(response.body?.errorCode).isEqualTo(ACTOR_ALREADY_DEAD.name)
        assertThat(response.body?.errorReason).isEqualTo(ACTOR_ALREADY_DEAD.wording)
        assertThat(response.body?.message).isEqualTo("Actor with id ${id} is already dead.")
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