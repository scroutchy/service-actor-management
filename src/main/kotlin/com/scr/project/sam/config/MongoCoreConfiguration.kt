package com.scr.project.sam.config

import com.scr.project.commons.cinema.config.LocalDateToMongoConverter
import com.scr.project.commons.cinema.config.MongoToLocalDateConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration(proxyBeanMethods = false)
@EnableReactiveMongoRepositories(basePackages = ["com.scr.project"])
@EnableTransactionManagement
class MongoCoreConfiguration {

    @Bean
    fun customConversions() = MongoCustomConversions(
        listOf(LocalDateToMongoConverter(), MongoToLocalDateConverter())
    )

    @Bean
    fun reactiveMongoTransactionManager(factory: ReactiveMongoDatabaseFactory) = ReactiveMongoTransactionManager(factory)

    @Bean
    fun transactionalOperator(reactiveTransactionManager: ReactiveTransactionManager) =
        TransactionalOperator.create(reactiveTransactionManager)
}