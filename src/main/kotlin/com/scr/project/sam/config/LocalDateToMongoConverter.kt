package com.scr.project.sam.config

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.Date

/**
 * MongoDB converter that converts an [LocalDate] to [Date]
 * @see Converter
 */
@Component
@WritingConverter
class LocalDateToMongoConverter : Converter<LocalDate, Date> {

    override fun convert(source: LocalDate): Date {
        return Date(source.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
    }
}