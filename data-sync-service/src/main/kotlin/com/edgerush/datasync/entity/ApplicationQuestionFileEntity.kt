package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("application_question_files")
data class ApplicationQuestionFileEntity(
    @Id
    val id: Long? = null,
    val applicationId: Long,
    val questionPosition: Int?,
    val question: String?,
    val originalFilename: String?,
    val url: String?,
)
