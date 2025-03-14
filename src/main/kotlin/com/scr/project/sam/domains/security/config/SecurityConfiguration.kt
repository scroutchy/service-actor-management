package com.scr.project.sam.domains.security.config

import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Flux

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(@Value("\${security.jwt.secretKey}") private val secretKey: String) {

    companion object {

        const val ROLE_WRITE = "WRITE"
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers(POST).hasRole(ROLE_WRITE)
                    .pathMatchers(PATCH).hasRole(ROLE_WRITE)
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt {} }
            .build()
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        val key = Keys.hmacShaKeyFor(secretKey.toByteArray())
        return NimbusReactiveJwtDecoder.withSecretKey(key).build()
    }

    @Bean
    fun jwtAuthenticationConverter(): ReactiveJwtAuthenticationConverter {
        return ReactiveJwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter { jwt ->
                val roles = jwt.getClaimAsStringList("roles") ?: emptyList()
                Flux.fromIterable(roles.map { SimpleGrantedAuthority(it) })
            }
        }
    }
}