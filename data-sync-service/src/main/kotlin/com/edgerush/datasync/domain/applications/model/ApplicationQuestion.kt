package com.edgerush.datasync.domain.applications.model

/**
 * Entity representing a question and answer in an application
 */
data class ApplicationQuestion(
    val position: Int,
    val question: String,
    val answer: String,
    val files: List<ApplicationFile> = emptyList()
) {
    init {
        require(position >= 0) { "Question position must be non-negative" }
        require(question.isNotBlank()) { "Question cannot be blank" }
        require(answer.isNotBlank()) { "Answer cannot be blank" }
    }
}

/**
 * Value object representing a file attachment
 */
data class ApplicationFile(
    val originalFilename: String,
    val url: String
) {
    init {
        require(originalFilename.isNotBlank()) { "Filename cannot be blank" }
        require(url.isNotBlank()) { "URL cannot be blank" }
    }
}
