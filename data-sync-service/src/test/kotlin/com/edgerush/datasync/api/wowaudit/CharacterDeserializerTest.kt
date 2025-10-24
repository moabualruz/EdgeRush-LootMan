package com.edgerush.datasync.api.wowaudit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CharacterDeserializerTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun `deserializes minimal character payload`() {
        val json = """
            {
              "characters": [
                {
                  "name": "MageA",
                  "realm": "twisting-nether",
                  "region": "eu",
                  "class": "Mage",
                  "spec": "Fire",
                  "role": "DPS",
                  "gear": {
                    "equipped": {
                      "head": {"item_id": 123, "item_level": 730, "quality": 4}
                    },
                    "best": {},
                    "spark": {}
                  },
                  "statistics": {
                    "wcl": {"raid_finder": 98, "normal": 85, "heroic": 60, "mythic": 40}
                  },
                  "collectibles": {"mounts": 10},
                  "timestamps": {"join_date": "2024-01-01T00:00:00Z"}
                }
              ]
            }
        """

        val response = mapper.readValue(json, CharactersResponse::class.java)
        assertThat(response.characters).hasSize(1)
        val character = response.characters.first()
        assertThat(character.name).isEqualTo("MageA")
        assertThat(character.gear.equipped.head?.itemId).isEqualTo(123)
        assertThat(character.statistics.warcraftLogs?.raidFinder).isEqualTo(98)
    }
}
