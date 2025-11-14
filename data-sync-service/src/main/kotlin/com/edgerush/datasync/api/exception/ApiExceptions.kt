package com.edgerush.datasync.api.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class AccessDeniedException(message: String) : RuntimeException(message)

class ValidationException(message: String) : RuntimeException(message)
