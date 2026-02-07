package com.edgerush.lootman.domain.recruitment

import java.time.Instant

data class RecruitmentComment(
    val id: Long? = null,
    val applicationId: String,
    val authorId: Long,
    val text: String,
    val createdAt: Instant,
)
