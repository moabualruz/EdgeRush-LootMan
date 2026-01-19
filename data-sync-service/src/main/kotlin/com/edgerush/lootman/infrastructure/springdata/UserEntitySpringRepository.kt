package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface UserEntitySpringRepository :
    CrudRepository<UserEntity, Long>,
    PagingAndSortingRepository<UserEntity, Long> {

    fun findByDiscordId(discordId: String): UserEntity?
    fun findByBattlenetId(battlenetId: String): UserEntity?
    fun findByUsernameIgnoreCase(username: String): UserEntity?
    fun findByEmailIgnoreCase(email: String): UserEntity?
    fun findByGuildId(guildId: String): List<UserEntity>
    fun findByGuildId(guildId: String, pageable: Pageable): Page<UserEntity>
    fun existsByUsernameIgnoreCase(username: String): Boolean
    fun existsByEmailIgnoreCase(email: String): Boolean
    fun existsByDiscordId(discordId: String): Boolean
    fun existsByBattlenetId(battlenetId: String): Boolean
    fun countByGuildId(guildId: String): Long
}
