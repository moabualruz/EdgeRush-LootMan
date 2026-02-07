package com.edgerush.lootman.infrastructure.auth

import com.edgerush.datasync.entity.UserEntity
import com.edgerush.lootman.domain.auth.model.User
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserRole
import com.edgerush.lootman.domain.auth.repository.UserRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.infrastructure.springdata.UserEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of UserRepository.
 *
 * Persists users to the users table using Spring Data JDBC.
 */
@Repository
class JdbcUserRepository(
    private val springRepository: UserEntitySpringRepository,
) : UserRepository {
    override fun findById(id: UserId): User? = springRepository.findById(id.value).orElse(null)?.toDomain()

    override fun findByDiscordId(discordId: String): User? = springRepository.findByDiscordId(discordId)?.toDomain()

    override fun findByBattlenetId(battlenetId: String): User? = springRepository.findByBattlenetId(battlenetId)?.toDomain()

    override fun findByGuildId(guildId: GuildId): List<User> = springRepository.findByGuildId(guildId.value).map { it.toDomain() }

    override fun findByUsername(username: String): User? = springRepository.findByUsernameIgnoreCase(username)?.toDomain()

    override fun findByEmail(email: String): User? = springRepository.findByEmailIgnoreCase(email)?.toDomain()

    override fun existsByUsername(username: String): Boolean = springRepository.existsByUsernameIgnoreCase(username)

    override fun existsByEmail(email: String): Boolean = springRepository.existsByEmailIgnoreCase(email)

    override fun save(user: User): User {
        val entity = user.toEntity()
        val savedEntity = springRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun deleteById(id: UserId) {
        springRepository.deleteById(id.value)
    }

    override fun existsByDiscordId(discordId: String): Boolean = springRepository.existsByDiscordId(discordId)

    override fun existsByBattlenetId(battlenetId: String): Boolean = springRepository.existsByBattlenetId(battlenetId)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<User> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("id"),
            )
        return springRepository.findAll(pageRequest).content.map { it.toDomain() }
    }

    override fun count(): Long = springRepository.count()

    private fun UserEntity.toDomain(): User =
        User(
            id = id?.let { UserId(it) },
            discordId = discordId,
            battlenetId = battlenetId,
            username = username,
            email = email,
            passwordHash = passwordHash,
            avatarUrl = avatarUrl,
            role = UserRole.fromString(role),
            guildId = guildId?.let { GuildId(it) },
            createdAt = createdAt,
            lastLogin = lastLogin,
        )

    private fun User.toEntity(): UserEntity =
        UserEntity(
            id = id?.value,
            discordId = discordId,
            battlenetId = battlenetId,
            username = username,
            email = email,
            passwordHash = passwordHash,
            avatarUrl = avatarUrl,
            role = role.name,
            guildId = guildId?.value,
            createdAt = createdAt,
            lastLogin = lastLogin,
        )
}
