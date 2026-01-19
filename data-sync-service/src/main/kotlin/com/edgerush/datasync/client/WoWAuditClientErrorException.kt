package com.edgerush.datasync.client

/**
 * Exception thrown when WoWAudit API returns a 4xx client error.
 */
class WoWAuditClientErrorException(message: String) : WoWAuditClientException(message)
