package com.scr.project.sam.domains.actor.dao

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.scr.project.sam.domains.actor.model.entity.Actor
import org.bson.Document
import org.bson.types.ObjectId
import java.time.LocalDate
import java.util.*


class ActorDaoImpl: ActorDao {

    private val client = MongoClients.create("mongodb://localhost:27017")
    private val database = client.getDatabase("test")
    private val collection: MongoCollection<Document> = database.getCollection("actor")

    override fun insertActor(actor: Actor) {
        val document = Document("_id", actor.id)
            .append("surname", actor.surname)
            .append("name", actor.name)
            .append("nationality", actor.nationality.toString())
            .append("birthDate", actor.birthDate.toString())
            .append("deathDate", actor.deathDate?.toString())
        collection.insertOne(document)
    }

    override fun getActorById(id: ObjectId): Actor? {
        val document = collection.find(Document("_id", id)).first() ?: return null
        return Actor(
            document.getString("surname"),
            document.getString("name"),
            Locale("", document.getString("nationality")),
            LocalDate.parse(document.getString("birthDate")),
            document.getString("deathDate")?.let { LocalDate.parse(it) },
            document.getObjectId("_id")
        )
    }

    override fun countActors(): Long {
        return collection.countDocuments()
    }
}