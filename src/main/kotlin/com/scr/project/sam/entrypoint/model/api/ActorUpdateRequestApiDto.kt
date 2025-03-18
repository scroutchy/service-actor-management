package com.scr.project.sam.entrypoint.model.api

import com.scr.project.commons.cinema.model.api.DTO
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class ActorUpdateRequestApiDto(@field:PastOrPresent val deathDate: LocalDate) : DTO