package com.edgerush.datasync.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Configuration for WebClient instances with explicit timeouts and retry strategies.
 *
 * Provides resilient HTTP clients for external API integrations (WoWAudit, WarcraftLogs).
 */
@Configuration
class WebClientConfig(
    private val httpClientsProperties: HttpClientsProperties,
) {
    @Bean
    fun restTemplate(): org.springframework.web.client.RestTemplate {
        return org.springframework.web.client.RestTemplate()
    }

    @Bean
    fun wowauditWebClient(
        builder: WebClient.Builder,
        syncProperties: SyncProperties,
    ): WebClient {
        val apiKey = syncProperties.wowaudit.apiKey
        val defaultHeaders: (HttpHeaders) -> Unit = { headers ->
            if (!apiKey.isNullOrBlank()) {
                headers.setBearerAuth(apiKey)
            }
            headers.addIfAbsent(HttpHeaders.USER_AGENT, "EdgeRushLootMan/0.1 (+https://wowaudit.com)")
            headers.addIfAbsent(HttpHeaders.ACCEPT, "application/json")
        }

        return builder
            .baseUrl(syncProperties.wowaudit.baseUrl)
            .clientConnector(ReactorClientHttpConnector(createHttpClient()))
            .defaultHeaders(defaultHeaders)
            .filter(retryOn5xxFilter())
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { it.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE_BYTES) }
                    .build(),
            )
            .build()
    }

    /**
     * Creates a configured Netty HttpClient with explicit timeouts.
     */
    private fun createHttpClient(): HttpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, httpClientsProperties.connectTimeoutMs)
            .responseTimeout(Duration.ofMillis(httpClientsProperties.readTimeoutMs.toLong()))
            .doOnConnected { conn ->
                conn.addHandlerLast(
                    ReadTimeoutHandler(httpClientsProperties.readTimeoutMs.toLong(), TimeUnit.MILLISECONDS),
                )
                conn.addHandlerLast(
                    WriteTimeoutHandler(httpClientsProperties.writeTimeoutMs.toLong(), TimeUnit.MILLISECONDS),
                )
            }

    /**
     * Exchange filter that retries on 5xx server errors with exponential backoff.
     */
    private fun retryOn5xxFilter(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofResponseProcessor { response ->
            if (response.statusCode().is5xxServerError) {
                response.createError()
            } else {
                Mono.just(response)
            }
        }

    companion object {
        private const val MAX_IN_MEMORY_SIZE_BYTES = 16 * 1024 * 1024 // 16 MB
    }
}

/**
 * Extension function to create a retrying WebClient from an existing one.
 *
 * Usage:
 * ```
 * webClient.get()
 *     .uri("/api/data")
 *     .retrieve()
 *     .bodyToMono(String::class.java)
 *     .retryOn5xx(httpClientsProperties)
 * ```
 */
fun <T> Mono<T>.retryOn5xx(properties: HttpClientsProperties): Mono<T> =
    this.retryWhen(
        Retry.backoff(properties.maxRetries.toLong(), Duration.ofMillis(properties.retryBackoffMs))
            .filter { it is ServerErrorException }
            .onRetryExhaustedThrow { _, signal ->
                signal.failure()
            },
    )

/**
 * Exception thrown when a 5xx server error is encountered.
 */
class ServerErrorException(
    val statusCode: HttpStatusCode,
    message: String,
) : RuntimeException(message)

/**
 * Extension to create a ServerErrorException from a ClientResponse.
 */
private fun ClientResponse.createError(): Mono<ClientResponse> =
    this.bodyToMono(String::class.java)
        .defaultIfEmpty("")
        .flatMap { body ->
            Mono.error(ServerErrorException(this.statusCode(), "Server error: ${this.statusCode()} - $body"))
        }
