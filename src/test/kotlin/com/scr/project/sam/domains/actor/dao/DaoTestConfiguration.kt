package com.scr.project.sam.domains.actor.dao

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
internal class DaoTestConfiguration {

    @Bean
    fun actorDao(@Value("\${spring.data.mongodb.uri}") mongoUri: String) = ActorDao(mongoUri)
}