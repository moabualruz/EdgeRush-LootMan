package com.edgerush.datasync.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    fun wowauditWebClient(
        builder: WebClient.Builder,
        properties: SyncProperties,
    ): WebClient {
        val apiKey = properties.wowaudit.apiKey
        val defaultHeaders: (HttpHeaders) -> Unit = { headers ->
            if (!apiKey.isNullOrBlank()) {
                headers.setBearerAuth(apiKey)
            }
            headers.addIfAbsent(HttpHeaders.USER_AGENT, "EdgeRushLootMan/0.1 (+https://wowaudit.com)")
            headers.addIfAbsent(HttpHeaders.ACCEPT, "application/json")
        }

        return builder
            .baseUrl(properties.wowaudit.baseUrl)
            .defaultHeaders(defaultHeaders)
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { it.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE_BYTES) }
                    .build(),
            )
            .build()
    }

    companion object {
        private const val MAX_IN_MEMORY_SIZE_BYTES = 16 * 1024 * 1024 // 16 MB
    }
}
