package com.edgerush.lootman.infrastructure.shared

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.model.ItemQuality
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * Unit tests for JdbcGearRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the raider_gear_items table.
 */
class JdbcGearRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcGearRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcGearRepository(jdbcTemplate)
    }

    @Nested
    inner class FindCurrentGearTests {

        @Test
        fun `should return equipped gear set when found`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("gearSet = ?") },
                    any<RowMapper<GearItem>>(),
                    eq(raiderId.value),
                    eq("EQUIPPED")
                )
            } returns listOf(
                createGearItem(EquipmentSlot.HEAD, 619),
                createGearItem(EquipmentSlot.CHEST, 619),
                createGearItem(EquipmentSlot.MAIN_HAND, 626)
            )

            // When
            val result = repository.findCurrentGear(raiderId)

            // Then
            result shouldNotBe null
            result?.gearSetType shouldBe GearSetType.EQUIPPED
            result?.items?.size shouldBe 3
        }

        @Test
        fun `should return null when no gear items found`() {
            // Given
            val raiderId = RaiderId(999L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("gearSet = ?") },
                    any<RowMapper<GearItem>>(),
                    eq(raiderId.value),
                    eq("EQUIPPED")
                )
            } returns emptyList()

            // When
            val result = repository.findCurrentGear(raiderId)

            // Then
            result shouldBe null
        }
    }

    @Nested
    inner class FindByRaiderIdAndTypeTests {

        @Test
        fun `should return equipped gear set`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("gearSet = ?") },
                    any<RowMapper<GearItem>>(),
                    eq(raiderId.value),
                    eq("EQUIPPED")
                )
            } returns listOf(
                createGearItem(EquipmentSlot.HEAD, 619),
                createGearItem(EquipmentSlot.SHOULDER, 619)
            )

            // When
            val result = repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)

            // Then
            result shouldNotBe null
            result?.gearSetType shouldBe GearSetType.EQUIPPED
            result?.items?.size shouldBe 2
        }

        @Test
        fun `should return best gear set`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("gearSet = ?") },
                    any<RowMapper<GearItem>>(),
                    eq(raiderId.value),
                    eq("BEST")
                )
            } returns listOf(
                createGearItem(EquipmentSlot.HEAD, 626),
                createGearItem(EquipmentSlot.SHOULDER, 626)
            )

            // When
            val result = repository.findByRaiderIdAndType(raiderId, GearSetType.BEST)

            // Then
            result shouldNotBe null
            result?.gearSetType shouldBe GearSetType.BEST
        }

        @Test
        fun `should map all database fields to domain model`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") },
                    any<RowMapper<GearItem>>(),
                    eq(raiderId.value),
                    any()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GearItem>>()
                val rs = mockResultSet(
                    slot = "HEAD",
                    itemId = 12345L,
                    name = "Helm of Dominance",
                    itemLevel = 626,
                    quality = 4, // EPIC
                    enchant = "Enchant: +150 Intellect",
                    sockets = 2
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)

            // Then
            result shouldNotBe null
            val headItem = result?.items?.get(EquipmentSlot.HEAD)
            headItem shouldNotBe null
            headItem?.itemId?.value shouldBe 12345L
            headItem?.name shouldBe "Helm of Dominance"
            headItem?.itemLevel shouldBe 626
            headItem?.quality shouldBe ItemQuality.EPIC
            headItem?.enchant shouldBe "Enchant: +150 Intellect"
            headItem?.sockets shouldBe 2
        }

        @Test
        fun `should handle null enchant and zero sockets`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") },
                    any<RowMapper<GearItem>>(),
                    eq(raiderId.value),
                    any()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GearItem>>()
                val rs = mockResultSet(
                    slot = "TRINKET_1",
                    itemId = 54321L,
                    name = "Trinket of Power",
                    itemLevel = 619,
                    quality = 4,
                    enchant = null,
                    sockets = 0
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)

            // Then
            result shouldNotBe null
            val trinketItem = result?.items?.get(EquipmentSlot.TRINKET_1)
            trinketItem?.enchant shouldBe null
            trinketItem?.sockets shouldBe 0
        }
    }

    @Nested
    inner class SaveTests {

        @Test
        fun `should delete existing gear and insert new items`() {
            // Given
            val raiderId = RaiderId(100L)
            val gearSet = createGearSet(GearSetType.EQUIPPED)

            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(raiderId, gearSet)

            // Then
            result shouldBe gearSet

            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE FROM") },
                    raiderId.value,
                    "EQUIPPED"
                )
            }
        }

        @Test
        fun `should insert all gear items from gear set`() {
            // Given
            val raiderId = RaiderId(100L)
            val gearSet = createGearSet(GearSetType.EQUIPPED, itemCount = 3)

            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            repository.save(raiderId, gearSet)

            // Then
            // Should delete once, then insert 3 times
            verify(exactly = 1) {
                jdbcTemplate.update(
                    match { it.contains("DELETE FROM") },
                    raiderId.value,
                    "EQUIPPED"
                )
            }
            verify(exactly = 3) {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg()
                )
            }
        }

        @Test
        fun `should save best gear set type`() {
            // Given
            val raiderId = RaiderId(100L)
            val gearSet = createGearSet(GearSetType.BEST)

            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            repository.save(raiderId, gearSet)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE FROM") },
                    raiderId.value,
                    "BEST"
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        slot: String = "HEAD",
        itemId: Long = 12345L,
        name: String = "Test Item",
        itemLevel: Int = 619,
        quality: Int = 4,
        enchant: String? = null,
        sockets: Int = 0
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("slot") } returns slot
        every { rs.getLong("itemId") } returns itemId
        every { rs.getString("name") } returns name
        every { rs.getInt("itemLevel") } returns itemLevel
        every { rs.getInt("quality") } returns quality
        every { rs.getString("enchant") } returns enchant
        every { rs.getInt("sockets") } returns sockets
        return rs
    }

    private fun createGearItem(
        slot: EquipmentSlot,
        itemLevel: Int = 619
    ): GearItem = GearItem(
        itemId = ItemId(12345L),
        name = "Test Item ${slot.name}",
        itemLevel = itemLevel,
        quality = ItemQuality.EPIC,
        slot = slot,
        isTierPiece = false,
        enchant = null,
        sockets = 0
    )

    private fun createGearSet(
        gearSetType: GearSetType,
        itemCount: Int = 2
    ): GearSet {
        val slots = listOf(EquipmentSlot.HEAD, EquipmentSlot.SHOULDER, EquipmentSlot.CHEST)
        val items = slots.take(itemCount).associateWith { slot ->
            createGearItem(slot)
        }
        return GearSet(items = items, gearSetType = gearSetType)
    }
}
