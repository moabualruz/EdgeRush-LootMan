package com.edgerush.lootman.infrastructure.external.blizzard

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant

@Service
class BlizzardDataService(
    @Value("\${spring.oauth2.battlenet.client-id}") private val clientId: String,
    @Value("\${spring.oauth2.battlenet.client-secret}") private val clientSecret: String,
    @Value("\${spring.oauth2.battlenet.region:us}") private val region: String
) {
    private val webClient = WebClient.builder().build()
    private var accessToken: String? = null
    private var tokenExpiry: Instant = Instant.MIN

    private fun getBaseUrl(): String {
        return if (region == "cn") "https://gateway.battlenet.com.cn" else "https://$region.api.blizzard.com"
    }

    private fun getAuthUrl(): String {
        return if (region == "cn") "https://www.battlenet.com.cn/oauth/token" else "https://$region.battle.net/oauth/token"
    }

    @Synchronized
    private fun getAccessToken(): String {
        if (accessToken != null && Instant.now().isBefore(tokenExpiry)) {
            return accessToken!!
        }

        // Fetch new client credentials token
        val response = webClient.post()
            .uri(getAuthUrl())
            .headers { it.setBasicAuth(clientId, clientSecret) }
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .block() ?: throw IllegalStateException("Failed to retrieve Battle.net access token")

        accessToken = response.access_token
        tokenExpiry = Instant.now().plusSeconds(response.expires_in - 60) // Buffer
        return accessToken!!
    }

    fun getRaids(): List<BlizzardRaid> {
        // Hardcoding current expansion raid tier ID (e.g., Nerub-ar Palace) logic or fetching 'current tier'
        // For now, let's fetch instances from the Journal API
        // https://us.api.blizzard.com/data/wow/journal-instance/index?namespace=static-us
        
        val url = "${getBaseUrl()}/data/wow/journal-instance/index?namespace=static-$region&locale=en_US"
        
        return webClient.get()
            .uri(url)
            .header("Authorization", "Bearer ${getAccessToken()}")
            .retrieve()
            .bodyToMono(JournalInstanceIndexResponse::class.java)
            .block()
            ?.instances
            ?.map { 
                 BlizzardRaid(it.id, it.name) 
            } ?: emptyList()
    }

    fun getRaidMaps(instanceId: Int): List<BlizzardMap> {
         // 1. Get Instance Details to find description/background if needed
         // 2. We actually need encounters to get maps, or journal-instance/{id} which usually lists maps/encounters
         
         val url = "${getBaseUrl()}/data/wow/journal-instance/$instanceId?namespace=static-$region&locale=en_US"
         val instance = webClient.get()
            .uri(url)
            .header("Authorization", "Bearer ${getAccessToken()}")
            .retrieve()
            .bodyToMono(JournalInstanceDetailResponse::class.java)
            .block()

         return instance?.maps?.map { 
             BlizzardMap(it.id, it.name, it.description)
         } ?: emptyList()
    }
    
    fun getMap(mapId: Int): BlizzardMapDetails? {
        // Fetch specific map data, ideally generating a tile URL or direct image URL if available
        // The Game Data API 'media' endpoint for Journal Media is often where the images live.
        throw NotImplementedError("Map detail fetching needs refinement on exact Blizzard endpoint")
    }
}

data class TokenResponse(val access_token: String, val expires_in: Long)
data class JournalInstanceIndexResponse(val instances: List<JournalInstanceRef>)
data class JournalInstanceRef(val id: Int, val name: String)
data class JournalInstanceDetailResponse(val id: Int, val name: String, val maps: List<JournalMapRef> = emptyList())
data class JournalMapRef(val id: Int, val name: String, val description: String?)

data class BlizzardRaid(val id: Int, val name: String)
data class BlizzardMap(val id: Int, val name: String, val description: String?)
data class BlizzardMapDetails(val id: Int, val imageUrl: String)
