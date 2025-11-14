package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.UpdateBenchmarkRequest
import com.edgerush.datasync.domain.shared.model.BenchmarkMode
import com.edgerush.datasync.domain.shared.model.Guild
import com.edgerush.datasync.domain.shared.model.GuildId
import com.edgerush.datasync.domain.shared.repository.GuildRepository
import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

/**
 * Integration tests for GuildController.
 */
class GuildControllerIntegrationTest : IntegrationTest() {
    
    @Autowired
    private lateinit var guildRepository: GuildRepository
    
    @Test
    fun `should get guild configuration`() {
        // Given
        val guild = createAndSaveTestGuild("test-guild-1")
        
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/guilds/${guild.id.value}",
            Map::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!["guildId"] shouldBe "test-guild-1"
    }
    
    @Test
    fun `should create default guild when not found`() {
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/guilds/new-guild",
            Map::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!["guildId"] shouldBe "new-guild"
        response.body!!["name"] shouldBe "Guild new-guild"
    }
    
    @Test
    fun `should get all guilds`() {
        // Given
        createAndSaveTestGuild("test-guild-2")
        createAndSaveTestGuild("test-guild-3")
        
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/guilds",
            List::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
    }
    
    @Test
    fun `should update benchmark configuration`() {
        // Given
        val guild = createAndSaveTestGuild("test-guild-4")
        val request = UpdateBenchmarkRequest(
            mode = "CUSTOM",
            customRms = 0.95,
            customIpi = 0.90
        )
        
        // When
        val response = restTemplate.exchange(
            "/api/v1/guilds/${guild.id.value}/benchmark",
            HttpMethod.PATCH,
            HttpEntity(request),
            Map::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!["benchmarkMode"] shouldBe "CUSTOM"
        response.body!!["customBenchmarkRms"] shouldBe 0.95
        response.body!!["customBenchmarkIpi"] shouldBe 0.90
    }
    
    private fun createAndSaveTestGuild(guildId: String): Guild {
        val guild = Guild.create(
            guildId = guildId,
            name = "Test Guild $guildId",
            description = "Test description"
        )
        return guildRepository.save(guild)
    }
}
