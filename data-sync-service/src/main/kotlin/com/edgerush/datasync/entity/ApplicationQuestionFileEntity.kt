package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("application_question_files")
data class ApplicationQuestionFileEntity(
    @Id
    val id: Long? = null,
    @Column("application_id")
    val applicationId: Long,
    @Column("question_position")
    val questionPosition: Int?,
    @Column("question")
    val question: String?,
    @Column("original_filename")
    val originalFilename: String?,
    @Column("url")
    val url: String?,
)
