package com.edgerush.datasync.config

import com.edgerush.lootman.api.common.DeprecatedEndpoint
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Web filter that adds deprecation headers to responses for deprecated endpoints.
 *
 * This filter checks if the target endpoint is annotated with @DeprecatedEndpoint
 * and adds the following headers to the response:
 * - Deprecation: date="YYYY-MM-DD" (RFC 8594)
 * - Sunset: YYYY-MM-DD (when sunset is specified)
 * - Link: <replacement>; rel="successor-version" (when replacement is specified)
 *
 * These headers inform API consumers that an endpoint is deprecated and when
 * it will be removed, helping them plan their migration.
 */
@Component
class DeprecationHeaderFilter(
    private val handlerMapping: RequestMappingHandlerMapping,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        return handlerMapping.getHandler(exchange)
            .cast(HandlerMethod::class.java)
            .doOnNext { handler ->
                addDeprecationHeaders(exchange, handler)
            }
            .onErrorResume { _ ->
                // If we can't get the handler, just continue without deprecation headers
                Mono.empty()
            }
            .then(chain.filter(exchange))
    }

    private fun addDeprecationHeaders(
        exchange: ServerWebExchange,
        handler: HandlerMethod,
    ) {
        val annotation =
            handler.getMethodAnnotation(DeprecatedEndpoint::class.java)
                ?: return

        val headers = exchange.response.headers

        // Add Deprecation header with the date (RFC 8594)
        headers.add("Deprecation", "date=\"${annotation.since}\"")

        // Add Sunset header if specified
        if (annotation.sunset.isNotBlank()) {
            headers.add("Sunset", annotation.sunset)
        }

        // Add Link header for successor version if specified
        if (annotation.replacement.isNotBlank()) {
            headers.add("Link", "<${annotation.replacement}>; rel=\"successor-version\"")
        }
    }
}
