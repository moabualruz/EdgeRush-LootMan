package com.edgerush.lootman.api.common

import kotlin.math.ceil

/**
 * Generic paged response wrapper.
 *
 * Contains the content for the current page along with pagination metadata.
 *
 * @param T The type of items in the content list
 */
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
) {
    /**
     * Total number of pages.
     */
    val totalPages: Int
        get() = if (totalElements == 0L) 0 else ceil(totalElements.toDouble() / size).toInt()

    /**
     * Whether this is the first page.
     */
    val isFirst: Boolean
        get() = page == 0

    /**
     * Whether this is the last page.
     */
    val isLast: Boolean
        get() = totalElements == 0L || page >= totalPages - 1

    /**
     * Whether there is a next page.
     */
    val hasNext: Boolean
        get() = !isLast

    /**
     * Whether there is a previous page.
     */
    val hasPrevious: Boolean
        get() = page > 0

    /**
     * Map the content to a different type while preserving pagination metadata.
     */
    fun <R> map(transform: (T) -> R): PagedResponse<R> =
        PagedResponse(
            content = content.map(transform),
            page = page,
            size = size,
            totalElements = totalElements,
        )

    companion object {
        /**
         * Create a PagedResponse from content, PageRequest, and total count.
         */
        fun <T> of(
            content: List<T>,
            pageRequest: PageRequest,
            totalElements: Long,
        ): PagedResponse<T> =
            PagedResponse(
                content = content,
                page = pageRequest.page,
                size = pageRequest.size,
                totalElements = totalElements,
            )

        /**
         * Create an empty PagedResponse.
         */
        fun <T> empty(pageRequest: PageRequest): PagedResponse<T> =
            PagedResponse(
                content = emptyList(),
                page = pageRequest.page,
                size = pageRequest.size,
                totalElements = 0,
            )
    }
}
