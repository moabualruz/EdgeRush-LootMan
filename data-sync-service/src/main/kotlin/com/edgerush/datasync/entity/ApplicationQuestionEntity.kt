package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("application_questions")
data class ApplicationQuestionEntity(
    @Id
    val id: Long? = null,
    val applicationId: Long,
    val question: String?,
    val answer: String?,
    val filesJson: String?
)

