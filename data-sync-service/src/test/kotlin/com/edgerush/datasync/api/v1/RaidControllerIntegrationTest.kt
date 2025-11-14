package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaidRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidRequest
import com.edgerush.datasync.api.dto.response.RaidResponse
import com.edgerush.datasync.domain.raids.model.RaidDifficulty
import com.edgerush.datasync.domain.raids.model.RaidStatus
import com.edgerush.datasync.domain.raids.repository.RaidRepository
import com.edgerush.datasync.infrastructure.persistence.mapper.RaidMapper
import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalTime

/**
 * Integration tests for RaidController.
 *
 * Tests the REST API endpoints for raid management using the new use cases.
 */
class RaidControllerIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var raidRepository: RaidRepository

    @Autowired
    private lateinit var raidMapper: RaidMapper

    @Test
    fun `should schedule new raid when valid request is provided`() {
        // Arrange
        val request =
            CreateRaidRequest(
                date = LocalDate.now().plusDays(7),
                startTime = LocalTime.of(19, 0),
                endTime = LocalTime.of(22, 0),
                instance = "Nerub-ar Palace",
                difficulty = "Mythic",
                optional = false,
                status = "Scheduled",
                notes = "Progression raid",
                teamId = 1L,
            )

        // Act
        val response =
            restTemplate.postForEntity(
                "/api/v1/raids",
                request,
                RaidResponse::class.java,
            )

        // Assert
        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldNotBe null
        response.body!!.date shouldBe request.date
        response.body!!.instance shouldBe request.instance
        response.body!!.difficulty shouldBe request.difficulty
        response.body!!.status shouldBe "Scheduled"
    }

    @Test
    fun `should return 400 when scheduling raid with invalid date`() {
        // Arrange
        val request =
            mapOf(
                "date" to null,
                "instance" to "Nerub-ar Palace",
                "difficulty" to "Mythic",
            )

        // Act
        val response =
            restTemplate.postForEntity(
                "/api/v1/raids",
                request,
                String::class.java,
            )

        // Assert
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
    }

    @Test
    fun `should retrieve raid by id when raid exists`() {
        // Arrange
        val raid = createTestRaid()
        val savedRaid = raidRepository.save(raid)

        // Act
        val response =
            restTemplate.getForEntity(
                "/api/v1/raids/${savedRaid.id.value}",
                RaidResponse::class.java,
            )

        // Assert
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!.raidId shouldBe savedRaid.id.value
        response.body!!.instance shouldBe raid.instance
    }

    @Test
    fun `should return 404 when retrieving non-existent raid`() {
        // Act
        val response =
            restTemplate.getForEntity(
                "/api/v1/raids/99999",
                String::class.java,
            )

        // Assert
        response.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    @Test
    fun `should update raid when valid request is provided`() {
        // Arrange
        val raid = createTestRaid()
        val savedRaid = raidRepository.save(raid)

        val updateRequest =
            UpdateRaidRequest(
                instance = "Nerub-ar Palace Updated",
                difficulty = "Heroic",
                notes = "Updated notes",
            )

        // Act
        val response =
            restTemplate.exchange(
                "/api/v1/raids/${savedRaid.id.value}",
                HttpMethod.PUT,
                HttpEntity(updateRequest),
                RaidResponse::class.java,
            )

        // Assert
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!.instance shouldBe updateRequest.instance
        response.body!!.difficulty shouldBe updateRequest.difficulty
        response.body!!.notes shouldBe updateRequest.notes
    }

    @Test
    fun `should delete raid when raid exists`() {
        // Arrange
        val raid = createTestRaid()
        val savedRaid = raidRepository.save(raid)

        // Act
        val response =
            restTemplate.exchange(
                "/api/v1/raids/${savedRaid.id.value}",
                HttpMethod.DELETE,
                null,
                Void::class.java,
            )

        // Assert
        response.statusCode shouldBe HttpStatus.NO_CONTENT

        // Verify raid is deleted
        val deletedRaid = raidRepository.findById(savedRaid.id)
        deletedRaid shouldBe null
    }

    @Test
    fun `should list all raids with pagination`() {
        // Arrange
        val raid1 = createTestRaid(instanceName = "Raid 1")
        val raid2 = createTestRaid(instanceName = "Raid 2")
        val raid3 = createTestRaid(instanceName = "Raid 3")
        raidRepository.save(raid1)
        raidRepository.save(raid2)
        raidRepository.save(raid3)

        // Act
        val response =
            restTemplate.getForEntity(
                "/api/v1/raids?page=0&size=2",
                String::class.java,
            )

        // Assert
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
    }

    @Test
    fun `should find raids by team id`() {
        // Arrange
        val teamId = 1L
        val raid1 = createTestRaid(instanceName = "Team Raid 1")
        val raid2 = createTestRaid(instanceName = "Team Raid 2")
        val raid3 = createTestRaid(instanceName = "Other Team Raid")
        
        // Save raids and manually set teamId in database
        val saved1 = raidRepository.save(raid1)
        val saved2 = raidRepository.save(raid2)
        val saved3 = raidRepository.save(raid3)
        
        // Update teamId in database directly
        jdbcTemplate.update("UPDATE raids SET team_id = ? WHERE raid_id = ?", teamId, saved1.id.value)
        jdbcTemplate.update("UPDATE raids SET team_id = ? WHERE raid_id = ?", teamId, saved2.id.value)
        jdbcTemplate.update("UPDATE raids SET team_id = ? WHERE raid_id = ?", 2L, saved3.id.value)

        // Act
        val response =
            restTemplate.getForEntity(
                "/api/v1/raids/team/$teamId",
                Array<RaidResponse>::class.java,
            )

        // Assert
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!.size shouldBe 2
        response.body!!.all { it.teamId == teamId } shouldBe true
    }

    @Test
    fun `should maintain backward compatibility with existing endpoints`() {
        // Arrange
        val raid = createTestRaid()
        val savedRaid = raidRepository.save(raid)
        
        // Set teamId in database
        val teamId = 1L
        jdbcTemplate.update("UPDATE raids SET team_id = ? WHERE raid_id = ?", teamId, savedRaid.id.value)

        // Act - Test all CRUD operations
        val getResponse =
            restTemplate.getForEntity(
                "/api/v1/raids/${savedRaid.id.value}",
                RaidResponse::class.java,
            )

        val listResponse =
            restTemplate.getForEntity(
                "/api/v1/raids",
                String::class.java,
            )

        val teamResponse =
            restTemplate.getForEntity(
                "/api/v1/raids/team/$teamId",
                Array<RaidResponse>::class.java,
            )

        // Assert - All endpoints should work
        getResponse.statusCode shouldBe HttpStatus.OK
        listResponse.statusCode shouldBe HttpStatus.OK
        teamResponse.statusCode shouldBe HttpStatus.OK
    }

    private fun createTestRaid(
        instanceName: String = "Nerub-ar Palace",
    ): com.edgerush.datasync.domain.raids.model.Raid {
        return com.edgerush.datasync.domain.raids.model.Raid.schedule(
            guildId = com.edgerush.datasync.domain.raids.model.GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(7),
            startTime = LocalTime.of(19, 0),
            endTime = LocalTime.of(22, 0),
            instance = instanceName,
            difficulty = RaidDifficulty.MYTHIC,
            optional = false,
            notes = "Test raid",
        )
    }
}
