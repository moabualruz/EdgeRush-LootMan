package com.edgerush.lootman.infrastructure.external.raidbots

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Instant

@Service
class RaidbotsService(
    private val config: RaidbotsConfig
) {
    private val webClient = WebClient.builder()
        .baseUrl(config.url)
        .defaultHeader("Authorization", "Bearer ${config.apiKey}")
        .build()

    fun submitSimulation(simcInput: String): String {
        if (!config.enabled) throw IllegalStateException("Raidbots integration is disabled")

        val response = webClient.post()
            .uri("/api/v1/sim")
            .bodyValue(RaidbotsSimRequest(
                type = "advanced",
                advancedInput = simcInput,
                simcVersion = "nightly"
            ))
            .retrieve()
            .bodyToMono<RaidbotsSimResponse>()
            .block() ?: throw RuntimeException("Failed to submit simulation to Raidbots")

        return response.simId
    }

    fun getSimulationStatus(simId: String): RaidbotsSimStatus {
        return webClient.get()
            .uri("/api/v1/sim/$simId")
            .retrieve()
            .bodyToMono<RaidbotsSimStatus>()
            .block() ?: throw RuntimeException("Failed to get simulation status")
    }
}

data class RaidbotsSimRequest(
    val type: String,
    @JsonProperty("advanced_input") val advancedInput: String,
    @JsonProperty("simc_version") val simcVersion: String
)

data class RaidbotsSimResponse(
    val simId: String
)

data class RaidbotsSimStatus(
    val simId: String,
    val progress: Int, // 0-100
    val state: String // pending, running, complete, error
)
