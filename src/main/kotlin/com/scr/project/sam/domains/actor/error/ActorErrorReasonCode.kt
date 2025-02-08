package com.scr.project.sam.domains.actor.error

enum class ActorErrorReasonCode(val wording: String) {
    ACTOR_NOT_FOUND("Actor is not registered"),
    INCONSISTENT_DEATH_DATE("Death date cannot be prior to birth date"),
    ACTOR_ALREADY_DEAD("The actor is already dead"),
}