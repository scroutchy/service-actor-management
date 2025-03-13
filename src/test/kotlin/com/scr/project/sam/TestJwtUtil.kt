package com.scr.project.sam

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class TestJwtUtil(@Value("\${security.jwt.secretKey}") private val secretKey: String) {

    final val standardToken: String

    init {
        standardToken = generateMockToken()
    }

    final fun generateMockToken(roles: List<String> = listOf("USER")): String {
        val claims = Jwts.claims().setSubject("test-user")
//        claims["roles"] = roles // Optional: Include roles if your security config uses them
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + (24 * 60 * 60 * 1000)))
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()))
            .compact()
    }
}