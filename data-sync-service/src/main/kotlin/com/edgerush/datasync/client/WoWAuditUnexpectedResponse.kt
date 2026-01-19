package com.edgerush.datasync.client

/**
 * Exception thrown when WoWAudit API returns an unexpected response format (e.g., HTML instead of JSON).
 */
class WoWAuditUnexpectedResponse(message: String) : WoWAuditClientException(message)
