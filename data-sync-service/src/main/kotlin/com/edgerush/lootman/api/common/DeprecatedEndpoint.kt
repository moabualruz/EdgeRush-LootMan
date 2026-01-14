package com.edgerush.lootman.api.common

/**
 * Annotation to mark API endpoints as deprecated.
 *
 * When applied to a controller method, the DeprecationHeaderFilter will add
 * appropriate HTTP headers to the response:
 * - Deprecation: date="YYYY-MM-DD" (RFC 8594)
 * - Sunset: YYYY-MM-DD (when sunset is specified)
 * - Link: <replacement>; rel="successor-version" (when replacement is specified)
 *
 * Example usage:
 * ```kotlin
 * @GetMapping("/api/v1/old-endpoint")
 * @DeprecatedEndpoint(
 *     since = "2026-01-01",
 *     sunset = "2026-06-01",
 *     replacement = "/api/v2/new-endpoint"
 * )
 * fun oldEndpoint(): Response {
 *     // ...
 * }
 * ```
 *
 * @property since The date when the endpoint was deprecated (ISO 8601 format: YYYY-MM-DD)
 * @property sunset The date when the endpoint will be removed (ISO 8601 format: YYYY-MM-DD), optional
 * @property replacement The path to the new endpoint that replaces this one, optional
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DeprecatedEndpoint(
    val since: String,
    val sunset: String = "",
    val replacement: String = "",
)
