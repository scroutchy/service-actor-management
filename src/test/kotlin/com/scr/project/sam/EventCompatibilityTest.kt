package com.scr.project.sam

import com.scr.project.sam.SchemaCompatibilityLevel.FORWARD_TRANSITIVE
import com.scr.project.sam.SchemaCompatibilityLevel.NONE
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.Properties
import kotlin.io.path.inputStream

open class EventCompatibilityTest {

    private val schemaDir = Paths.get("src/main/avro")
    private val schemaHistoryDir = Paths.get("src/test/resources/avro_history")
    private val schemaLibsDir = Paths.get("build/schemas-libs")
    private val compatibilityPropertiesFile = schemaDir.resolve("compatibility.properties")
    private val compatibilityProperties: Properties = Properties()
    protected var eventCompatibilityChecker =
        EventCompatibilityChecker(schemaDir.toFile(), schemaHistoryDir.toFile(), schemaLibsDir.toFile())

    @Test
    fun assertEventCompatibility() {
        compatibilityProperties.load(compatibilityPropertiesFile.inputStream())
        val excludedTopics = compatibilityProperties.stringPropertyNames().filter { compatibilityProperties[it].toLevel() == NONE }

        schemaDir.toFile().listFiles { file -> file.extension == "avsc" }
            .orEmpty()
            .distinctBy { file -> file.name.substringBefore("_v") }
            .forEach { schemaFile ->
                val topicName = schemaFile.name.substringBefore("_v").substringBefore(".")
                val compatibilityLevel = if (!excludedTopics.contains(topicName)) FORWARD_TRANSITIVE else NONE
                eventCompatibilityChecker.assertSchemaCompatibility(topicName, compatibilityLevel)
            }
    }

    private fun Any?.toLevel(): SchemaCompatibilityLevel = if (NONE.name == this) NONE else FORWARD_TRANSITIVE
}