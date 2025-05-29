package com.scr.project.sam.domains.outbox.service

import com.scr.project.sam.config.Properties.KAFKA_ENABLING_PROPERTY
import com.scr.project.sam.domains.outbox.model.entity.Outbox
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.Disposable
import reactor.core.publisher.Flux
import java.time.Duration

@Component
@ConditionalOnProperty(name = [KAFKA_ENABLING_PROPERTY], havingValue = "true", matchIfMissing = false)
class OutboxPollingManager(
    private val outboxRelayerService: OutboxRelayerService
) {

    private val logger: Logger = LoggerFactory.getLogger(OutboxPollingManager::class.java)
    private var pollingDisposable: Disposable? = null

    /**
     * Démarre le polling périodique pour traiter les événements outbox.
     */
    @PostConstruct
    fun startPolling() {
        pollingDisposable?.dispose()
        pollingDisposable = pollAndProcess().subscribe(
            {},
            { error -> logger.error("Polling error: ", error) }
        )
        logger.info("Outbox polling started.")
    }

    /**
     * Arrête le polling en cours.
     */
    fun stopPolling() {
        pollingDisposable?.dispose()
        logger.info("Outbox polling stopped.")
    }

    private fun pollAndProcess(pollInterval: Duration = Duration.ofSeconds(1)): Flux<Outbox> {
        return Flux.interval(pollInterval)
            .doOnSubscribe { logger.debug("Polling interval initiated.") }
            .doOnNext { logger.trace("Polling tick: $it") }
            .flatMap {
                logger.debug("Fetching pending outbox events...")
                outboxRelayerService.processOutbox()
            }
            .doOnError { e -> logger.warn("Error during pollAndProcess flatMap operation: ${e.message}", e) }
    }
}

