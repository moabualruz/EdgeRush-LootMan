package com.edgerush.datasync.schema

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class WoWAuditSchemaTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `schema lists expected column categories`() {
        val schemaPath = Path.of("..", "docs", "wowaudit-raw-schema.json").normalize()
        assertThat(Files.exists(schemaPath)).`as`("schema file exists").isTrue()

        val root = mapper.readTree(Files.readString(schemaPath))
        val categories = root.fieldNames().asSequence().toSet()
        assertThat(categories).contains(
            "gear_equipped",
            "gear_best",
            "spark_items",
            "tier_summary",
            "renown_campaign",
            "pvp",
            "raid_progression",
        )

        val totalColumns =
            root.fields().asSequence()
                .flatMap { (_, value) -> value.elements().asSequence() }
                .count()
        assertThat(totalColumns)
            .`as`("total raw_data columns captured")
            .isGreaterThanOrEqualTo(400)
    }
}
