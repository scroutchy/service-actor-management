package com.scr.project.sam.config

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * MongoDB converter that converts a [Date] to [LocalDate]
 * @see Converter
 */
@Component
@ReadingConverter
class MongoToLocalDateConverter : Converter<Date, LocalDate> {

    override fun convert(source: Date): LocalDate {
        return source.toInstant().atZone(ZoneId.of("UTC")).toLocalDate()
    }
}