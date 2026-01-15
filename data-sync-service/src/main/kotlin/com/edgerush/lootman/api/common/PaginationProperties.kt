package com.edgerush.lootman.api.common

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for pagination.
 *
 * Binds to api.pagination.* properties in application.yaml.
 */
@ConfigurationProperties(prefix = "api.pagination")
data class PaginationProperties(
    val defaultPageSize: Int = 20,
    val maxPageSize: Int = 100,
) {
    /**
     * Create a PageRequest with configured defaults.
     *
     * @param page The page number (0-indexed)
     * @param size Optional size, uses defaultPageSize if not provided
     * @return A PageRequest with validated parameters
     */
    fun createPageRequest(
        page: Int = 0,
        size: Int? = null,
    ): PageRequest =
        PageRequest(
            page = page,
            size = size ?: defaultPageSize,
            maxPageSize = maxPageSize,
        )
}
