package com.scr.project.sam

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @Autowired
    protected lateinit var testJwtUtil: TestJwtUtil

    companion object {

        val mongoDBContainer = MongoDBContainer("mongo:6.0").apply { start() }
        val keycloakContainer = KeycloakContainer().apply {
            withAdminUsername("admin")
            withAdminPassword("admin")
            withRealmImportFile("my-realm.json")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { "${keycloakContainer.authServerUrl}/realms/my-realm" }
        }
    }
}