package com.scr.project.sam.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class CorsConfiguration : WebFluxConfigurer {

    private val logger: Logger = LoggerFactory.getLogger(CorsConfiguration::class.java)

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:4200", "http://ui-cinema.kind.hp")
            .allowedMethods("GET", "POST", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
        logger.info("CORS policy loaded")
    }
}