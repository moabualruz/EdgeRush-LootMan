package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("application_questions")
data class ApplicationQuestionEntity(
    @Id val id: Long? = null,
    @Column("application_id")
    val applicationId: Long,
    @Column("position")
    val position: Int?,
    @Column("question")
    val question: String?,
    @Column("answer")
    val answer: String?,
    @Column("files_json")
    val filesJson: String?,
)
