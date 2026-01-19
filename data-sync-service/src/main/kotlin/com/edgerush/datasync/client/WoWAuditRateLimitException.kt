package com.edgerush.datasync.client

/**
 * Exception thrown when WoWAudit API returns HTTP 429 (Too Many Requests).
 */
class WoWAuditRateLimitException(message: String) : WoWAuditClientException(message)
