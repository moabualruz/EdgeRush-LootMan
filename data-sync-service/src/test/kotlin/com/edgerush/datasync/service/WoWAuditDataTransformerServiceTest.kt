package com.edgerush.datasync.service

import com.edgerush.datasync.repository.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Transactional
class WoWAuditDataTransformerServiceTest {
    @Autowired
    private lateinit var attendanceStatRepository: AttendanceStatRepository

    @Autowired
    private lateinit var historicalActivityRepository: HistoricalActivityRepository

    @Autowired
    private lateinit var characterHistoryRepository: CharacterHistoryRepository

    @Autowired
    private lateinit var wishlistSnapshotRepository: WishlistSnapshotRepository

    @Autowired
    private lateinit var lootAwardRepository: LootAwardRepository

    @Autowired
    private lateinit var raidRepository: LegacyRaidRepository

    @Autowired
    private lateinit var raidSignupRepository: RaidSignupRepository

    @Autowired
    private lateinit var raiderService: RaiderService

    @Test
    @Transactional
    fun `should create transformer service without errors`() {
        // Given
        val transformer =
            WoWAuditDataTransformerService(
                attendanceStatRepository,
                historicalActivityRepository,
                characterHistoryRepository,
                wishlistSnapshotRepository,
                lootAwardRepository,
                raidRepository,
                raidSignupRepository,
                raiderService,
            )

        // When
        val attendanceData = transformer.getAttendanceData()
        val activityData = transformer.getActivityData()
        val wishlistData = transformer.getWishlistData()
        val lootHistoryData = transformer.getLootHistoryData()
        val gearData = transformer.getCharacterGearData()

        // Then
        assertThat(attendanceData).isNotNull
        assertThat(activityData).isNotNull
        assertThat(wishlistData).isNotNull
        assertThat(lootHistoryData).isNotNull
        assertThat(gearData).isNotNull

        // Data should be empty but structures should be valid
        assertThat(attendanceData.characters).isEmpty()
        assertThat(activityData.characters).isEmpty()
        assertThat(wishlistData.characters).isEmpty()
        assertThat(lootHistoryData.characters).isEmpty()
        assertThat(gearData.characters).isEmpty()
    }
}
