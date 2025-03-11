package com.scr.project.sam.domains.security.config

import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(@Value("\${security.jwt.secretKey}") private val secretKey: String) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { it.anyExchange().authenticated() }
            .oauth2ResourceServer { r -> r.jwt { it.jwtDecoder(jwtDecoder()) } }
            .build()
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        val key = Keys.hmacShaKeyFor(secretKey.toByteArray())
        return NimbusReactiveJwtDecoder.withSecretKey(key).build()
    }
}