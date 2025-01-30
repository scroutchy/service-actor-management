package com.scr.project.sam.domains.actor.error

import com.scr.project.sam.domains.actor.error.ActorErrorReasonCode.ACTOR_NOT_FOUND
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(NOT_FOUND)
class OnActorNotFound(val id: ObjectId) : RuntimeException(ACTOR_NOT_FOUND.wording)