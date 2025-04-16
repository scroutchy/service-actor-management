package com.scr.project.sam

import com.scr.project.sam.SchemaCompatibilityLevel.FORWARD_TRANSITIVE
import io.confluent.kafka.schemaregistry.CompatibilityChecker.FORWARD_TRANSITIVE_CHECKER
import io.confluent.kafka.schemaregistry.CompatibilityChecker.FULL_TRANSITIVE_CHECKER
import io.confluent.kafka.schemaregistry.ParsedSchema
import io.confluent.kafka.schemaregistry.avro.AvroSchema
import org.apache.avro.Schema.Parser
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter
import kotlin.properties.Delegates

class EventCompatibilityChecker(
    private val schemaDirectory: File,
    private val schemaHistoryDirectory: File,
    private val schemaLibsDirectory: File
) {

    companion object {

        private const val UNKNOWN_VERSION = 999
        private val MAJOR_VERSION_REGEX = ".*_v(\\d+)\\..*".toRegex()
    }

    private data class ParsingResult(val schemaFile: File, val schema: ParsedSchema?, val exception: Throwable?)

    private val logger = LoggerFactory.getLogger(EventCompatibilityChecker::class.java)
    private var schemas: Map<File, ParsedSchema?> by Delegates.notNull()

    init {
        val librarySchemaFiles = schemaLibsDirectory.toSchemaFiles()
        val schemaFiles = schemaDirectory.toSchemaFiles()
        schemas = parseSchemas(librarySchemaFiles.plus(schemaFiles))
    }

    private fun File.toSchemaFiles() = walk().sortedByDepth().filter { it.isSchemaFile() }.toList()

    private fun Sequence<File>.sortedByDepth() = sortedByDescending { it.path.count { c -> c == File.separatorChar } }

    private fun parseSchemas(schemaFiles: List<File>, previousFailingSchema: List<File> = emptyList()): Map<File, ParsedSchema?> {
        val parser = Parser()
        val results = schemaFiles.map { parser.safeParse(it) }
        val failures = results.filter { it.exception != null }
        if (failures.isNotEmpty()) {
            val failingSchemas = failures.map { it.schemaFile }
            if (previousFailingSchema.containsAll(failingSchemas) && failingSchemas.containsAll(previousFailingSchema)) {
                error("Unrecoverable parsing errors on schemas:\n$failures")
            }
            return parseSchemas(schemaFiles.pushToEnd(failingSchemas), failingSchemas)
        }
        return results.associate { it.schemaFile to it.schema }
    }

    private fun List<File>.pushToEnd(files: List<File>) = minus(files).plus(files)

    private fun Parser.safeParse(it: File) =
        runCatching { ParsingResult(it, AvroSchema(parse(it)), null) }
            .getOrElse { ex -> ParsingResult(it, null, ex) }

    fun assertSchemaCompatibility(topicName: String, compatibilityLevel: SchemaCompatibilityLevel = FORWARD_TRANSITIVE) {
        logger.info("Asserting compatibility for schema $topicName with level $compatibilityLevel between major versions")
        val activeSchemas = activeSchemas(topicName)
        assert(activeSchemas.isNotEmpty()) { "Unknown schema $topicName found in compatibility.properties" }
        val schemaHistory = schemaHistory(topicName)
        activeSchemas.forEach {
            if (schemaHistory.lastOrNull()?.canonicalString() != it.canonicalString()) {
                assert(FULL_TRANSITIVE_CHECKER.isCompatible(it, schemaHistory.toMinorVersions(it)).isEmpty()) {
                    "Version ${it.name()} of schema $topicName is not FULL_TRANSITIVE compatible with previous minor versions"
                }
                if (compatibilityLevel == FORWARD_TRANSITIVE) {
                    assert(FORWARD_TRANSITIVE_CHECKER.isCompatible(it, schemaHistory.toPreviousVersions(it)).isEmpty()) {
                        "Version ${it.name()} of schema $topicName is not FORWARD_TRANSITIVE compatible with all previous versions"
                    }
                }
                val outputDir = File("$schemaHistoryDirectory/$topicName")
                if (!outputDir.exists()) outputDir.mkdirs()
                val outputFile = File(outputDir, "$topicName.avsc")
                val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
                val prettyPrinter = objectMapper.writerWithDefaultPrettyPrinter()
                outputFile.writeText(prettyPrinter.writeValueAsString(objectMapper.readTree(it.canonicalString())))
            }
        }
    }

    private fun activeSchemas(topicName: String): List<ParsedSchema> {
        logger.info("loading active schemas:")
        return schemaDirectory
            .listFiles { f: File -> f.isFile && f.name.matches("$topicName(_v\\d+)?\\.avsc".toRegex()) }.orEmpty()
            .sortedBy { it.toMajorVersion() }
            .onEach { f -> logger.info("+ ${f.name}") }
            .map { schemas[it] ?: error("schema not found for $it") }
    }

    private fun File.toMajorVersion(): Int {
        return MAJOR_VERSION_REGEX.matchEntire(name)?.destructured?.let { (v) -> v.toInt() } ?: UNKNOWN_VERSION
    }

    private fun schemaHistory(topicName: String): List<ParsedSchema> {
        logger.info("loading schema history:")
        return File(schemaHistoryDirectory, topicName)
            .listFiles(FileFilter { it.isSchemaFile() })
            .orEmpty()
            .sortedBy { it.toMajorVersion() }
            .onEach { f -> logger.info("+ ${f.name}") }
            .map { AvroSchema(Parser().parse(it)) }
    }

    private fun List<ParsedSchema>.toPreviousVersions(schema: ParsedSchema): List<ParsedSchema> {
        return takeWhile { it.name() != schema.name() } + toMinorVersions(schema)
    }

    private fun List<ParsedSchema>.toMinorVersions(schema: ParsedSchema) = filter { it.name() == schema.name() }

    private fun File.isSchemaFile() = isFile && extension == "avsc"
}