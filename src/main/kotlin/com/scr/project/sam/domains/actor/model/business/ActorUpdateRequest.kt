package com.scr.project.sam.domains.actor.model.business

import org.bson.types.ObjectId
import java.time.LocalDate

data class ActorUpdateRequest(val id: ObjectId, val deathDate: LocalDate)