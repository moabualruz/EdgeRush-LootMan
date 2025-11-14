package com.edgerush.datasync.client.warcraftlogs

import com.edgerush.datasync.config.warcraftlogs.WarcraftLogsProperties
import com.edgerush.datasync.model.warcraftlogs.CharacterPerformanceData
import com.edgerush.datasync.model.warcraftlogs.WarcraftLogsFight
import com.edgerush.datasync.model.warcraftlogs.WarcraftLogsReport
import com.edgerush.datasync.service.warcraftlogs.ClientCredentials
import com.edgerush.datasync.service.warcraftlogs.WarcraftLogsMetrics
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import java.time.Instant

@Component
class WarcraftLogsClientImpl(
    private val properties: WarcraftLogsProperties,
    private val authService: WarcraftLogsAuthService,
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper,
    private val metrics: WarcraftLogsMetrics
) : WarcraftLogsClient {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override suspend fun fetchReportsForGuild(
        guildName: String,
        realm: String,
        region: String,
        startTime: Instant,
        endTime: Instant
    ): List<WarcraftLogsReport> {
        logger.info("Fetching Warcraft Logs reports for guild: $guildName-$realm ($region)")
        
        // Normalize realm name for API (remove spaces, lowercase)
        val realmSlug = realm.replace(" ", "-").lowercase()
        
        val query = """
            query {
              reportData {
                reports(
                  guildName: "$guildName"
                  guildServerSlug: "$realmSlug"
                  guildServerRegion: "$region"
                  startTime: ${startTime.toEpochMilli()}
                  endTime: ${endTime.toEpochMilli()}
                ) {
                  data {
                    code
                    title
                    startTime
                    endTime
                    owner { name }
                    zone { id }
                  }
                }
              }
            }
        """.trimIndent()
        
        try {
            val credentials = ClientCredentials(properties.clientId, properties.clientSecret)
            val responseJson = executeGraphQLQuery(query, credentials)
            
            val reports = parseReportsResponse(responseJson)
            logger.info("Successfully fetched ${reports.size} reports from Warcraft Logs API")
            return reports
        } catch (ex: WarcraftLogsException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("Failed to fetch Warcraft Logs reports", ex)
            throw WarcraftLogsApiException("Failed to fetch reports: ${ex.message}", 500, ex)
        }
    }
    
    override suspend fun fetchFightData(reportCode: String): List<WarcraftLogsFight> {
        logger.info("Fetching fight data for report: $reportCode")
        
        val query = """
            query {
              reportData {
                report(code: "$reportCode") {
                  fights {
                    id
                    encounterID
                    name
                    difficulty
                    kill
                    startTime
                    endTime
                    bossPercentage
                  }
                }
              }
            }
        """.trimIndent()
        
        try {
            val credentials = ClientCredentials(properties.clientId, properties.clientSecret)
            val responseJson = executeGraphQLQuery(query, credentials)
            
            val fights = parseFightsResponse(responseJson)
            logger.info("Successfully fetched ${fights.size} fights for report: $reportCode")
            return fights
        } catch (ex: WarcraftLogsException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("Failed to fetch fight data", ex)
            throw WarcraftLogsApiException("Failed to fetch fights: ${ex.message}", 500, ex)
        }
    }
    
    override suspend fun fetchCharacterPerformance(
        reportCode: String,
        fightId: Int,
        characterName: String
    ): CharacterPerformanceData? {
        logger.info("Fetching performance data for $characterName in report $reportCode, fight $fightId")
        
        val query = """
            query {
              reportData {
                report(code: "$reportCode") {
                  table(
                    fightIDs: [$fightId]
                    dataType: DamageTaken
                  )
                  events(
                    fightIDs: [$fightId]
                    dataType: Deaths
                  ) {
                    data
                  }
                  playerDetails(fightIDs: [$fightId])
                }
              }
            }
        """.trimIndent()
        
        try {
            val credentials = ClientCredentials(properties.clientId, properties.clientSecret)
            val responseJson = executeGraphQLQuery(query, credentials)
            
            val performance = parsePerformanceResponse(responseJson, characterName)
            if (performance != null) {
                logger.info("Successfully fetched performance data for $characterName")
            } else {
                logger.warn("No performance data found for $characterName in fight $fightId")
            }
            return performance
        } catch (ex: WarcraftLogsException) {
            logger.error("Failed to fetch character performance", ex)
            return null
        } catch (ex: Exception) {
            logger.error("Failed to fetch character performance", ex)
            return null
        }
    }
    
    private suspend fun executeGraphQLQuery(
        query: String,
        credentials: ClientCredentials
    ): JsonNode {
        val startTime = java.time.Instant.now()
        val accessToken = authService.getAccessToken(credentials)
        
        val response = webClient.post()
            .uri(properties.baseUrl)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .bodyValue(mapOf("query" to query))
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(String::class.java).map { body ->
                    when (response.statusCode()) {
                        HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN -> 
                            WarcraftLogsAuthenticationException("Authentication failed: $body")
                        HttpStatus.TOO_MANY_REQUESTS -> {
                            val retryAfter = response.headers().asHttpHeaders()
                                .getFirst("Retry-After")?.toLongOrNull()
                            WarcraftLogsRateLimitException("Rate limit exceeded", retryAfter)
                        }
                        else -> WarcraftLogsApiException("API error: $body", response.statusCode().value())
                    }
                }
            }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java).map { body ->
                    WarcraftLogsApiException("Server error: $body", response.statusCode().value())
                }
            }
            .awaitBodyOrNull<String>() ?: throw WarcraftLogsApiException("Empty response from API", 500)
        
        val jsonNode = objectMapper.readTree(response)
        
        // Record API latency
        val duration = java.time.Duration.between(startTime, java.time.Instant.now())
        metrics.recordApiCall(duration)
        
        // Check for GraphQL errors
        if (jsonNode.has("errors")) {
            val errors = jsonNode.get("errors")
            val errorMessage = errors.joinToString("; ") { it.get("message")?.asText() ?: "Unknown error" }
            throw WarcraftLogsApiException("GraphQL errors: $errorMessage", 400)
        }
        
        return jsonNode
    }
    
    private fun parseReportsResponse(jsonNode: JsonNode): List<WarcraftLogsReport> {
        val reportsData = jsonNode.path("data").path("reportData").path("reports").path("data")
        
        if (!reportsData.isArray) {
            return emptyList()
        }
        
        return reportsData.map { reportNode ->
            WarcraftLogsReport(
                code = reportNode.path("code").asText(),
                title = reportNode.path("title").asText(""),
                startTime = reportNode.path("startTime").asLong(),
                endTime = reportNode.path("endTime").asLong(),
                owner = reportNode.path("owner").path("name").asText("Unknown"),
                zone = reportNode.path("zone").path("id").asInt(0),
                fights = emptyList() // Fights are fetched separately
            )
        }
    }
    
    private fun parseFightsResponse(jsonNode: JsonNode): List<WarcraftLogsFight> {
        val fightsData = jsonNode.path("data").path("reportData").path("report").path("fights")
        
        if (!fightsData.isArray) {
            return emptyList()
        }
        
        return fightsData.mapNotNull { fightNode ->
            try {
                WarcraftLogsFight(
                    id = fightNode.path("id").asInt(),
                    encounterID = fightNode.path("encounterID").asInt(),
                    name = fightNode.path("name").asText("Unknown"),
                    difficulty = fightNode.path("difficulty").asInt(),
                    kill = fightNode.path("kill").asBoolean(false),
                    startTime = fightNode.path("startTime").asLong(),
                    endTime = fightNode.path("endTime").asLong(),
                    bossPercentage = fightNode.path("bossPercentage").asDouble(0.0)
                )
            } catch (ex: Exception) {
                logger.warn("Failed to parse fight data: ${ex.message}")
                null
            }
        }
    }
    
    private fun parsePerformanceResponse(jsonNode: JsonNode, characterName: String): CharacterPerformanceData? {
        try {
            val reportData = jsonNode.path("data").path("reportData").path("report")
            
            // Parse damage taken table
            val damageTable = reportData.path("table")
            val deathsData = reportData.path("events").path("data")
            val playerDetails = reportData.path("playerDetails")
            
            // Find character in player details
            if (!playerDetails.isArray) {
                return null
            }
            
            val characterData = playerDetails.find { 
                it.path("name").asText().equals(characterName, ignoreCase = true) 
            } ?: return null
            
            // Count deaths
            var deaths = 0
            if (deathsData.isArray) {
                deaths = deathsData.count { event ->
                    event.path("targetName").asText().equals(characterName, ignoreCase = true)
                }
            }
            
            // Parse damage taken (simplified - actual implementation would need more complex parsing)
            val damageTaken = damageTable.path("totalDamageTaken").asLong(0)
            val avoidableDamageTaken = damageTable.path("avoidableDamageTaken").asLong(0)
            
            return CharacterPerformanceData(
                name = characterData.path("name").asText(),
                server = characterData.path("server").asText(""),
                `class` = characterData.path("type").asText(""),
                spec = characterData.path("specs").path(0).asText(""),
                deaths = deaths,
                damageTaken = damageTaken,
                avoidableDamageTaken = avoidableDamageTaken,
                itemLevel = characterData.path("minItemLevel").asInt(0),
                performancePercentile = null // Would need separate query for rankings
            )
        } catch (ex: Exception) {
            logger.error("Failed to parse performance data", ex)
            return null
        }
    }
}
