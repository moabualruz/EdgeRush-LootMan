package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("recruitment_comments")
data class RecruitmentCommentEntity(
    @Id
    val id: Long? = null,
    @Column("application_id")
    val applicationId: String,
    @Column("author_id")
    val authorId: Long,
    @Column("text")
    val text: String,
    @Column("created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
)
