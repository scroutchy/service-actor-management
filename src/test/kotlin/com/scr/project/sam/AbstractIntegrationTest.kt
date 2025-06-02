package com.scr.project.sam

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName

@TestInstance(PER_CLASS)
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @Autowired
    protected lateinit var testJwtUtil: TestJwtUtil

    companion object {

        private val network = Network.newNetwork()
        val mongoDBContainer = MongoDBContainer("mongo:6.0").apply {
            withNetwork(network)
            start()
        }
        val keycloakContainer = KeycloakContainer().apply {
            withAdminUsername("admin")
            withAdminPassword("admin")
            withRealmImportFile("my-realm.json")
            start()
        }
        val kafkaContainer = ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.2"))
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withEnv("KAFKA_LOG4J_ROOT_LOGLEVEL", "DEBUG")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .apply { start() }
        val schemaRegistryContainer = GenericContainer(DockerImageName.parse("confluentinc/cp-schema-registry:7.8.2"))
            .withNetwork(network)
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9093")
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            .withEnv("SCHEMA_REGISTRY_LOG4J_ROOT_LOGLEVEL", "DEBUG")
            .waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { "${keycloakContainer.authServerUrl}/realms/my-realm" }
            registry.add("spring.kafka.bootstrap-servers") { kafkaContainer.bootstrapServers }
            registry.add("spring.kafka.schema.registry.url") {
                "http://${schemaRegistryContainer.host}:${schemaRegistryContainer.getMappedPort(8081)}"
            }
        }
    }
}