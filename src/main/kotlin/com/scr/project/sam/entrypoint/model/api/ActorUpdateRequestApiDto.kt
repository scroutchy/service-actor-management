package com.scr.project.sam.entrypoint.model.api

import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class ActorUpdateRequestApiDto(@field:PastOrPresent val deathDate: LocalDate) : DTO