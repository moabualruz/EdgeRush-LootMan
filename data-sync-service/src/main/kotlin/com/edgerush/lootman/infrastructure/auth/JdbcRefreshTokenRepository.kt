package com.edgerush.lootman.infrastructure.auth

import com.edgerush.datasync.entity.RefreshTokenEntity
import com.edgerush.lootman.domain.auth.model.RefreshTokenId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserRefreshToken
import com.edgerush.lootman.domain.auth.repository.RefreshTokenRepository
import com.edgerush.lootman.infrastructure.springdata.RefreshTokenEntitySpringRepository
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of RefreshTokenRepository.
 *
 * Persists refresh tokens to the user_refresh_tokens table using Spring Data JDBC.
 */
@Repository
class JdbcRefreshTokenRepository(
    private val springRepository: RefreshTokenEntitySpringRepository,
) : RefreshTokenRepository {

    override fun findById(id: RefreshTokenId): UserRefreshToken? =
        springRepository.findById(id.value).orElse(null)?.toDomain()

    override fun findByTokenHash(tokenHash: String): UserRefreshToken? =
        springRepository.findByTokenHash(tokenHash)?.toDomain()

    override fun findByUserId(userId: UserId): List<UserRefreshToken> =
        springRepository.findByUserIdOrderByCreatedAtDesc(userId.value).map { it.toDomain() }

    override fun findValidByUserId(userId: UserId): List<UserRefreshToken> =
        springRepository.findValidByUserId(userId.value).map { it.toDomain() }

    override fun save(token: UserRefreshToken): UserRefreshToken {
        val entity = token.toEntity()
        val savedEntity = springRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun deleteById(id: RefreshTokenId) {
        springRepository.deleteById(id.value)
    }

    override fun deleteByUserId(userId: UserId): Int =
        springRepository.deleteByUserId(userId.value)

    override fun revokeAllByUserId(userId: UserId): Int =
        springRepository.revokeAllByUserId(userId.value)

    override fun deleteExpired(): Int =
        springRepository.deleteExpired()

    private fun RefreshTokenEntity.toDomain(): UserRefreshToken =
        UserRefreshToken(
            id = id?.let { RefreshTokenId(it) },
            userId = UserId(userId),
            tokenHash = tokenHash,
            expiresAt = expiresAt,
            createdAt = createdAt,
            revokedAt = revokedAt,
        )

    private fun UserRefreshToken.toEntity(): RefreshTokenEntity =
        RefreshTokenEntity(
            id = id?.value,
            userId = userId.value,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
            createdAt = createdAt,
            revokedAt = revokedAt,
        )
}
