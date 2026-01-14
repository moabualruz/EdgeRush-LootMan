package com.edgerush.lootman.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Discord Bot Application for EdgeRush LootMan.
 *
 * This bot provides:
 * - FLPS score queries (/flps)
 * - Loot history commands (/loot history)
 * - Leaderboard displays (/leaderboard)
 * - Automated notifications for loot awards, RDF expiry, penalties
 * - Admin commands for guild management
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class DiscordBotApplication

fun main(args: Array<String>) {
    runApplication<DiscordBotApplication>(*args)
}
