package com.scr.project.sam.domains.actor.dao

import com.scr.project.sam.domains.actor.model.entity.Actor
import org.bson.types.ObjectId
import java.time.LocalDate
import java.util.Locale

fun bradPitt() = Actor(
    "Pitt",
    "Brad",
    Locale("", "US"),
    LocalDate.of(1963, 12, 18),
    null,
    ObjectId("679bf64bf44d492fd46eec9f"),
)