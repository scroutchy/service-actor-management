package com.scr.project.sam.domains.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Flux

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(@Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") private val issuerUri: String) {

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
                    .pathMatchers(OPTIONS).permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt {} }
            .build()
    }

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder {
        // Récupérez l'URI du JWKS (nécessaire pour la validation de la signature)
        val jwkSetUri = "$issuerUri/protocol/openid-connect/certs"
        val jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build()
        // Créez un validateur qui ne contient PAS la validation de l'issuer
        val withoutIssuer: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(
            // Vous pouvez ajouter d'autres validateurs ici si nécessaire (par exemple, pour l'audience)
        )

        jwtDecoder.setJwtValidator(withoutIssuer)
        return jwtDecoder
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