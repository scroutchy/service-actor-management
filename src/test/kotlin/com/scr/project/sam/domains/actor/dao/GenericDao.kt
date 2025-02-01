package com.scr.project.sam.domains.actor.dao

import com.mongodb.client.MongoCollection
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.deleteMany
import org.litote.kmongo.findOneById
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.testcontainers.shaded.com.google.common.base.Predicate

@Component
abstract class GenericDao<T : Any>(
    @Value("\${spring.data.mongodb.uri}") private val mongoUri: String,
    entityClass: Class<T>,
    collectionName: String,
) {

    private val client = KMongo.createClient(mongoUri)
    private val database = client.getDatabase("test")
    protected val collection: MongoCollection<T> = database.getCollection(collectionName, entityClass)

    fun insert(entity: T) = collection.insertOne(entity)

    fun insertAll(entities: List<T>) = entities.forEach { insert(it) }

    fun findById(id: ObjectId): T? {
        return collection.findOneById(id)
    }

    fun findAll(): List<T> {
        return collection.find().toList()
    }

    fun findAnyBy(predicate: Predicate<T>): T? {
        return collection.find().filter { predicate.test(it) }.runCatching { random() }
            .getOrElse { throw Exception("No test data matching the predicate") }
    }

    fun findAny(): T? {
        return findAnyBy { true }
    }

    fun count() = collection.countDocuments()

    fun deleteAll() = collection.deleteMany()

    fun defaultEntities() = emptyList<T>()

    fun initTestData() {
        deleteAll()
        insertAll(defaultEntities())
    }
}