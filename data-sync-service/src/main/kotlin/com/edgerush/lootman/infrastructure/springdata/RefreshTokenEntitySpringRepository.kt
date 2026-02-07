package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RefreshTokenEntity
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenEntitySpringRepository :
    CrudRepository<RefreshTokenEntity, Long>,
    PagingAndSortingRepository<RefreshTokenEntity, Long> {
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<RefreshTokenEntity>

    @Query(
        "SELECT * FROM user_refresh_tokens WHERE user_id = :userId AND revoked_at IS NULL AND expires_at > NOW() ORDER BY created_at DESC",
    )
    fun findValidByUserId(userId: Long): List<RefreshTokenEntity>

    fun deleteByUserId(userId: Long): Int

    @Modifying
    @Query("UPDATE user_refresh_tokens SET revoked_at = NOW() WHERE user_id = :userId AND revoked_at IS NULL")
    fun revokeAllByUserId(userId: Long): Int

    @Modifying
    @Query("DELETE FROM user_refresh_tokens WHERE expires_at < NOW()")
    fun deleteExpired(): Int
}
