package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.UpdateRaiderStatusRequest
import com.edgerush.datasync.domain.shared.model.*
import com.edgerush.datasync.domain.shared.repository.RaiderRepository
import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime

/**
 * Integration tests for RaiderController.
 */
class RaiderControllerIntegrationTest : IntegrationTest() {
    
    @Autowired
    private lateinit var raiderRepository: RaiderRepository
    
    @Test
    fun `should get all raiders`() {
        // Given
        val raider = createAndSaveTestRaider("Testchar1", "TestRealm")
        
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/raiders",
            List::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
    }
    
    @Test
    fun `should get raider by ID`() {
        // Given
        val raider = createAndSaveTestRaider("Testchar2", "TestRealm")
        
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/raiders/${raider.id.value}",
            Map::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!["characterName"] shouldBe "Testchar2"
    }
    
    @Test
    fun `should return 404 when raider not found`() {
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/raiders/999999",
            Map::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.NOT_FOUND
    }
    
    @Test
    fun `should get raider by character name and realm`() {
        // Given
        val raider = createAndSaveTestRaider("Testchar3", "TestRealm")
        
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/raiders/character/Testchar3/realm/TestRealm",
            Map::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!["characterName"] shouldBe "Testchar3"
    }
    
    @Test
    fun `should update raider status`() {
        // Given
        val raider = createAndSaveTestRaider("Testchar4", "TestRealm")
        val request = UpdateRaiderStatusRequest(status = "BENCHED")
        
        // When
        val response = restTemplate.exchange(
            "/api/v1/raiders/${raider.id.value}/status",
            HttpMethod.PATCH,
            HttpEntity(request),
            Map::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!["status"] shouldBe "BENCHED"
    }
    
    @Test
    fun `should get all active raiders`() {
        // Given
        createAndSaveTestRaider("Active1", "TestRealm", RaiderStatus.ACTIVE)
        createAndSaveTestRaider("Inactive1", "TestRealm", RaiderStatus.INACTIVE)
        
        // When
        val response = restTemplate.getForEntity(
            "/api/v1/raiders/active",
            List::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        // Note: Actual count depends on test isolation
    }
    
    private fun createAndSaveTestRaider(
        characterName: String,
        realm: String,
        status: RaiderStatus = RaiderStatus.ACTIVE
    ): Raider {
        val raider = Raider.create(
            characterName = characterName,
            realm = realm,
            region = "US",
            wowauditId = System.currentTimeMillis(),
            clazz = WowClass.WARRIOR,
            spec = "Arms",
            role = RaiderRole.DPS,
            rank = "Member",
            status = status
        )
        return raiderRepository.save(raider)
    }
}
