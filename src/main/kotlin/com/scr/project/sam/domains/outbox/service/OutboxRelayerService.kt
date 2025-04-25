package com.scr.project.sam.domains.outbox.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.scr.project.sam.domains.outbox.model.entity.Outbox
import com.scr.project.sam.domains.outbox.repository.OutboxRepository
import org.apache.kafka.clients.producer.ProducerRecord
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono

@Service
class OutboxRelayerService(
    private val outboxRepository: OutboxRepository,
    private val kafkaSender: KafkaSender<String, Any>,
    private val objectMapper: ObjectMapper
) {

    private val logger: Logger = LoggerFactory.getLogger(OutboxRelayerService::class.java)

    @Scheduled(fixedDelay = 1000, initialDelayString = "\${outbox.initialDelay:0}")
    fun processOutbox() {
        outboxRepository.findAll()
            .flatMapSequential {
                logger.debug("Processing outbox event: {}", it.id)
                processSingleOutboxEvent(it)
                    .onErrorResume { e ->
                        logger.warn("Error processing outbox event ${it.id}: ${e.message}")
                        Mono.empty()
                    }
            }
            .subscribe(
                { logger.debug("Outbox event processing step completed.") },
                { e -> logger.warn("Error during outbox processing loop: {}", e.message, e) },
                { logger.debug("Outbox relayer scan completed.") }
            )
    }

    private fun processSingleOutboxEvent(outbox: Outbox): Mono<Unit> {
        return createSenderRecord(outbox)
            .flatMap { kafkaSender.send(it.toMono()).singleOrEmpty() }
            .doOnSuccess {
                logger.info(
                    "Outbox event {} successfully sent to Kafka (Offset: {}, Partition: {}).",
                    outbox.id, it?.recordMetadata()?.offset(), it?.recordMetadata()?.partition()
                )
            }
            .doOnError { e -> logger.warn("Failed to send outbox event {} to Kafka: {}", outbox.id, e.message, e) }
            .flatMap { outboxRepository.delete(outbox) }
            .doOnSubscribe { logger.debug("Deleting outbox event {} after successful sending.", outbox.id) }
            .doOnSuccess { logger.debug("Outbox event {} successfully deleted.", outbox.id) }
            .doOnError { logger.warn("Failed to delete outbox event {}: {}", outbox.id, it.message) }
            .thenReturn(Unit)
    }

    private fun createSenderRecord(outbox: Outbox): Mono<SenderRecord<String, Any, ObjectId>> {
        return ProducerRecord(outbox.topic, outbox.aggregateId, objectMapper.readValue(outbox.payload, Class.forName(outbox.aggregateType)))
            .toMono()
            .map { SenderRecord.create(it, outbox.id) }
    }
}