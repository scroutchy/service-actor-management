package com.scr.project.sam

import org.junit.jupiter.api.TestInstance
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractIntegrationTest {

    companion object {

        val mongoDBContainer = MongoDBContainer("mongo:6.0").apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun mongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }
    }
}