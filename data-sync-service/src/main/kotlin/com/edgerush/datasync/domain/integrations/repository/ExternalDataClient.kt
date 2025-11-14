package com.edgerush.datasync.domain.integrations.repository

/**
 * Interface for external data clients
 * Implementations should handle API communication with external services
 */
interface ExternalDataClient<T> {
    /**
     * Fetch data from the external source
     * @return Raw data from the external API
     */
    suspend fun fetchData(endpoint: String): Result<T>

    /**
     * Check if the client is healthy and can communicate with the external service
     */
    suspend fun healthCheck(): Result<Boolean>
}
