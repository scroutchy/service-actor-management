package com.scr.project.sam.domains.actor.dao

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.scr.project.sam.domains.actor.model.entity.Actor
import jakarta.annotation.PostConstruct
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import java.time.LocalDate
import java.util.*


class ActorDaoImpl(@Value("\${spring.data.mongodb.uri}") private val mongoUri: String): ActorDao {

    private lateinit var collection: MongoCollection<Document>

    @PostConstruct
    fun init() {
        val client = MongoClients.create(mongoUri)
        val database = client.getDatabase("test")
        collection = database.getCollection("actor")
    }

    override fun insert(actor: Actor) {
        val document = Document("_id", actor.id)
            .append("surname", actor.surname)
            .append("name", actor.name)
            .append("nationality", actor.nationality.toString())
            .append("birthDate", actor.birthDate.toString())
            .append("deathDate", actor.deathDate?.toString())
        collection.insertOne(document)
    }

    override fun findById(id: ObjectId): Actor? {
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

    override fun count(): Long {
        return collection.countDocuments()
    }

    override fun deleteAll() {
        collection.deleteMany(Document())
    }
}