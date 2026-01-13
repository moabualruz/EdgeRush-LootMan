package com.edgerush.lootman.infrastructure.audit

import com.edgerush.lootman.domain.audit.model.AuditLog
import com.edgerush.lootman.domain.audit.model.AuditOperation
import com.edgerush.lootman.domain.audit.repository.AuditLogRepository
import com.edgerush.lootman.infrastructure.metrics.CustomMetrics
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(1)
class AuditWebFilter(
    private val auditLogRepository: AuditLogRepository,
    private val customMetrics: CustomMetrics? = null
) : WebFilter {

    companion object {
        private val WRITE_METHODS = setOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH)
        private const val API_PATH_PREFIX = "/api/"
        private val EXCLUDED_PATH_PREFIXES = listOf("/actuator", "/swagger", "/v3/api-docs")

        private const val HEADER_USER_ID = "X-User-Id"
        private const val HEADER_USERNAME = "X-Username"
        private const val HEADER_ADMIN_MODE = "X-Admin-Mode"
        private const val HEADER_REQUEST_ID = "X-Request-Id"

        private const val DEFAULT_USER_ID = "anonymous"
        private const val DEFAULT_USERNAME = "anonymous"
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val method = request.method
        val path = request.uri.path

        if (!shouldPotentiallyAudit(method, path)) {
            return chain.filter(exchange)
        }

        return chain.filter(exchange)
            .doFinally {
                val response = exchange.response
                val statusCode = response.statusCode?.value() ?: 0

                if (statusCode in 200..299) {
                    captureAuditLog(request, statusCode)
                }
            }
    }

    private fun shouldPotentiallyAudit(method: HttpMethod?, path: String): Boolean {
        if (method !in WRITE_METHODS) {
            return false
        }

        if (!path.startsWith(API_PATH_PREFIX)) {
            return false
        }

        if (EXCLUDED_PATH_PREFIXES.any { path.startsWith(it) }) {
            return false
        }

        return true
    }

    private fun captureAuditLog(request: ServerHttpRequest, statusCode: Int) {
        val method = request.method
        val path = request.uri.path

        val operation = when (method) {
            HttpMethod.POST -> AuditOperation.CREATE
            HttpMethod.PUT, HttpMethod.PATCH -> AuditOperation.UPDATE
            HttpMethod.DELETE -> AuditOperation.DELETE
            else -> return
        }

        val (entityType, entityId) = extractEntityInfo(path)

        val headers = request.headers
        val userId = headers.getFirst(HEADER_USER_ID) ?: DEFAULT_USER_ID
        val username = headers.getFirst(HEADER_USERNAME) ?: DEFAULT_USERNAME
        val isAdminMode = headers.getFirst(HEADER_ADMIN_MODE)?.toBoolean() ?: false
        val requestId = headers.getFirst(HEADER_REQUEST_ID)

        val auditLog = AuditLog.create(
            operation = operation,
            entityType = entityType,
            entityId = entityId,
            userId = userId,
            username = username,
            isAdminMode = isAdminMode,
            requestId = requestId
        )

        try {
            auditLogRepository.save(auditLog)
            customMetrics?.recordAuditEntry(operation.name, entityType)
        } catch (e: Exception) {
            // Log error but do not fail the request
        }
    }

    private fun extractEntityInfo(path: String): Pair<String, String> {
        val pathParts = path.removePrefix("/api/v1/")
            .removePrefix("/api/")
            .split("/")
            .filter { it.isNotBlank() }

        if (pathParts.isEmpty()) {
            return "unknown" to "unknown"
        }

        return if (pathParts.size >= 2) {
            val lastPart = pathParts.last()
            val secondLastPart = pathParts[pathParts.size - 2]

            if (pathParts.size >= 3 && looksLikeId(secondLastPart)) {
                pathParts[pathParts.size - 3] to lastPart
            } else {
                secondLastPart to lastPart
            }
        } else {
            pathParts[0] to pathParts[0]
        }
    }

    private fun looksLikeId(value: String): Boolean {
        return value.all { it.isDigit() } ||
            value.matches(Regex("[0-9a-fA-F-]{8,}"))
    }
}
