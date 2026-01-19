package com.edgerush.datasync.client

/**
 * Exception thrown when WoWAudit API returns a 5xx server error.
 */
class WoWAuditServerException(message: String) : WoWAuditClientException(message)
