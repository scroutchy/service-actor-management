package com.scr.project.sam.domains.actor.dao

import com.scr.project.commons.cinema.test.dao.GenericDao
import com.scr.project.sam.domains.outbox.model.entity.Outbox

class OutboxDao(mongoUri: String) : GenericDao<Outbox>(mongoUri, Outbox::class.java, "outbox") {
}