package com.edgerush.lootman.api.common

/**
 * Request parameters for pagination.
 *
 * Represents the page number and size for paginated queries.
 * Validates input and caps size at maxPageSize.
 */
class PageRequest private constructor(
    val page: Int,
    val size: Int,
    val maxPageSize: Int,
) {
    /**
     * Calculate the offset for database queries.
     */
    val offset: Long
        get() = page.toLong() * size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PageRequest) return false
        return page == other.page && size == other.size && maxPageSize == other.maxPageSize
    }

    override fun hashCode(): Int = 31 * (31 * page + size) + maxPageSize

    override fun toString(): String = "PageRequest(page=$page, size=$size, maxPageSize=$maxPageSize)"

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val DEFAULT_MAX_PAGE_SIZE = 100

        /**
         * Create a PageRequest with validation.
         * Size is automatically capped at maxPageSize.
         */
        operator fun invoke(
            page: Int,
            size: Int,
            maxPageSize: Int = DEFAULT_MAX_PAGE_SIZE,
        ): PageRequest {
            require(page >= 0) { "Page must be non-negative" }
            require(size > 0) { "Size must be positive" }
            val cappedSize = minOf(size, maxPageSize)
            return PageRequest(page, cappedSize, maxPageSize)
        }

        /**
         * Create a PageRequest with default values.
         * Size is automatically capped at maxPageSize.
         */
        fun withDefaults(
            page: Int = 0,
            size: Int? = null,
            defaultSize: Int = DEFAULT_PAGE_SIZE,
            maxPageSize: Int = DEFAULT_MAX_PAGE_SIZE,
        ): PageRequest = PageRequest(
            page = page,
            size = minOf(size ?: defaultSize, maxPageSize),
            maxPageSize = maxPageSize,
        )
    }
}
