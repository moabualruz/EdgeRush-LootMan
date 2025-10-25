package com.edgerush.datasync.service

data class RaidSummary(
    val id: Long,
    val date: String?,
    val startTime: String?,
    val endTime: String?,
    val instance: String?,
    val difficulty: String?,
    val optional: Boolean?,
    val status: String?,
    val presentSize: Int?,
    val totalSize: Int?,
    val notes: String?,
    val selectionsImage: String?
)
