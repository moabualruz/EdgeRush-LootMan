package com.edgerush.datasync

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.nio.file.Files
import java.nio.file.Path

class AcceptanceSmokeTest {

    private val mapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Test
    fun `sample datasets load`() {
        val projectRoot = Path.of("").toAbsolutePath().normalize()
        val dataPath = projectRoot.resolve("../docs/data/mock_wowaudit.json").normalize()
        require(Files.exists(dataPath)) { "Expected mock data at $dataPath" }

        val raw = Files.readString(dataPath)
        val root = mapper.readTree(raw)
        val roster = mapper.readValue<List<RaiderSample>>(root["roster"].toString())

        assertAll(
            { assertThat(roster).isNotEmpty() },
            { assertThat(roster.first().character).isEqualTo("MageA") },
            { assertThat(roster.first().attendance_percent).isGreaterThan(80) }
        )
    }
}
