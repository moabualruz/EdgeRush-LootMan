package com.edgerush.datasync.service

data class RaiderRecord(
    val name: String,
    val realm: String,
    val region: String = "",
    val clazz: String = "",
    val spec: String = "",
    val role: String = ""
)

