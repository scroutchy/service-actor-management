package com.scr.project.sam.entrypoint.resource

import org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders.ACCEPT_RANGES
import org.springframework.http.HttpHeaders.CONTENT_RANGE
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.PARTIAL_CONTENT
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.Long.MAX_VALUE
import kotlin.math.min

typealias RangedResponse<T> = Mono<ResponseEntity<List<T>>>

@JvmSynthetic
fun <T> Flux<T>.toRangedResponse(
    itemClass: Class<T>,
    range: Pageable = Pageable.ofSize(DEFAULT_PAGE_SIZE),
    totalCount: Long? = null,
): RangedResponse<T> {
    return collectList()
        .map {
            status(it.inferStatus(range, totalCount))
                .addRangeHeaders(ContentRange(itemClass.resourceName(), range, it.inferCount(range), it.size.toLong()))
                .body(it)
        }
}

private fun ResponseEntity.BodyBuilder.addRangeHeaders(contentRange: ContentRange): ResponseEntity.BodyBuilder {
    return header(ACCEPT_RANGES, contentRange.unit)
        .header(CONTENT_RANGE, contentRange.toContentRangeHeader())
}

private fun List<*>.inferCount(range: Pageable) = when {
    isEmpty() && range.offset > 0 -> null
    size < range.pageSize -> range.offset + size
    else -> null
}

private fun List<*>.inferStatus(range: Pageable, totalCount: Long? = null) = when {
    totalCount != null && totalCount <= range.offset + range.pageSize -> OK
    totalCount == null && size < range.pageSize -> OK
    else -> PARTIAL_CONTENT
}

private fun <T> Class<T>.resourceName(): String {
    return simpleName.lowercase().removeSuffix("dto").removeSuffix("api").let { "${it}s" }
}

data class ContentRange(val unit: String, val range: Pageable, val count: Long? = null, val totalCount: Long? = null) {

    fun toContentRangeHeader() = "$unit ${toStringRange()}/${toStringCount()}"

    /**
     * Transforms range to a lower-bound + higher-bound representation (eg: 10-19)
     * @return a representation of the range
     */
    private fun toStringRange() = when {
        isOffsetGreaterThanCount(count) -> "*"
        else -> "${range.offset}-${min(count ?: MAX_VALUE, range.offset + range.pageSize) - 1}"
    }

    private fun isOffsetGreaterThanCount(count: Long?) = count != null && count <= range.offset

    private fun toStringCount() = totalCount?.toString() ?: "*"
}
