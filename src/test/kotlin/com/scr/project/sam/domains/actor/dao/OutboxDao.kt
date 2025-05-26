package com.scr.project.sam.domains.actor.dao

import com.scr.project.commons.cinema.outbox.model.entity.Outbox
import com.scr.project.commons.cinema.test.dao.GenericDao

class OutboxDao(mongoUri: String) : GenericDao<Outbox>(mongoUri, Outbox::class.java, "outbox") {
}