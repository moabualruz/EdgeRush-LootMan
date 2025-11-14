package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.applications.model.*
import com.edgerush.datasync.entity.ApplicationEntity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class ApplicationMapperTest {

    private lateinit var mapper: ApplicationMapper

    @BeforeEach
    fun setup() {
        mapper = ApplicationMapper()
    }

    @Test
    fun `should map entity to domain`() {
        // Given
        val entity = ApplicationEntity(
            applicationId = 1L,
            appliedAt = OffsetDateTime.now(),
            status = "PENDING",
            role = "DPS",
            age = 25,
            country = "USA",
            battletag = "player#1234",
            discordId = "discord123",
            mainCharacterName = "TestChar",
            mainCharacterRealm = "TestRealm",
            mainCharacterClass = "Warrior",
            mainCharacterRole = "Tank",
            mainCharacterRace = "Human",
            mainCharacterFaction = "Alliance",
            mainCharacterLevel = 80,
            mainCharacterRegion = "US",
            syncedAt = OffsetDateTime.now()
        )

        // When
        val domain = mapper.toDomain(entity, emptyList(), emptyList())

        // Then
        domain.id.value shouldBe 1L
        domain.status shouldBe ApplicationStatus.PENDING
        domain.applicantInfo.age shouldBe 25
        domain.mainCharacter.name shouldBe "TestChar"
    }

    @Test
    fun `should map domain to entity`() {
        // Given
        val application = Application.create(
            id = ApplicationId(1L),
            appliedAt = OffsetDateTime.now(),
            applicantInfo = ApplicantInfo(25, "USA", "player#1234", "discord123", "DPS"),
            mainCharacter = CharacterInfo("TestChar", "TestRealm", "US", "Warrior", "Tank", "Human", "Alliance", 80)
        )

        // When
        val entity = mapper.toEntity(application)

        // Then
        entity.applicationId shouldBe 1L
        entity.status shouldBe "PENDING"
        entity.age shouldBe 25
        entity.mainCharacterName shouldBe "TestChar"
    }
}
