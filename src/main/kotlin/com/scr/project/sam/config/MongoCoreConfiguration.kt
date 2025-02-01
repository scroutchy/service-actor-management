package com.scr.project.sam.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@Configuration(proxyBeanMethods = false)
@EnableReactiveMongoRepositories(basePackages = ["com.scr.project.sam.domains"])
class MongoCoreConfiguration {

    @Bean
    fun customConversions() = MongoCustomConversions(
        listOf(LocalDateToMongoConverter(), MongoToLocalDateConverter())
    )
}