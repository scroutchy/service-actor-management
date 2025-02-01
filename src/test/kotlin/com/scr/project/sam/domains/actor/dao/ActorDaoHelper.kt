package com.scr.project.sam.domains.actor.dao

import com.scr.project.sam.domains.actor.model.entity.Actor
import org.bson.types.ObjectId
import java.time.LocalDate
import java.util.Locale

fun bradPitt() = Actor(
    "Pitt",
    "Brad",
    Locale.Builder().setRegion("US").build(),
    LocalDate.of(1963, 12, 18),
    null,
    ObjectId("679bf64bf44d492fd46eec9f"),
)

fun jamesDean() = Actor(
    "Dean",
    "James",
    Locale.Builder().setRegion("US").build(),
    LocalDate.of(1931, 2, 8),
    LocalDate.of(1955, 9, 30),
    ObjectId("679bf64bf44d492fd46eeca0")
)