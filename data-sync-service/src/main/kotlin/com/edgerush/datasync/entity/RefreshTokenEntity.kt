package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("user_refresh_tokens")
data class RefreshTokenEntity(
    @Id
    val id: Long? = null,
    @Column("user_id")
    val userId: Long,
    @Column("token_hash")
    val tokenHash: String,
    @Column("expires_at")
    val expiresAt: Instant,
    @Column("created_at")
    val createdAt: Instant,
    @Column("revoked_at")
    val revokedAt: Instant?,
)
