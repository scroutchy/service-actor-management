package com.scr.project.sam.domains.actor.dao

import com.mongodb.client.MongoCollection
import com.scr.project.sam.domains.actor.model.entity.Actor
import jakarta.annotation.PostConstruct
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.findOneById
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

@Repository
class ActorDaoImpl(@Value("\${spring.data.mongodb.uri}") private val mongoUri: String): ActorDao {

    private lateinit var collection: MongoCollection<Actor>

    @PostConstruct
    fun init() {
        val client = KMongo.createClient(mongoUri)
        val database = client.getDatabase("test")
        collection = database.getCollection("actor", Actor::class.java)
    }

    override fun insert(actor: Actor) {
        collection.insertOne(actor)
    }

    override fun insertAll(actors: List<Actor>) {
        actors.forEach { insert(it) }
    }

    override fun findById(id: ObjectId): Actor? {
        return collection.findOneById(id)
    }

    override fun count(): Long {
        return collection.countDocuments()
    }

    override fun deleteAll() {
        collection.deleteMany(Document())
    }

    override fun initTestData() {
        deleteAll()
        insertAll(listOf(bradPitt()))
    }
}