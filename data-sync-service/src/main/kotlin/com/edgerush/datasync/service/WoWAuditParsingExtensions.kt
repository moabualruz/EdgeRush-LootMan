package com.edgerush.datasync.service

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

fun JsonNode.asIntOrNull(): Int? = if (this.isMissingNode || this.isNull) null else this.asInt()

fun JsonNode.asDoubleOrNull(): Double? = if (this.isMissingNode || this.isNull) null else this.asDouble()

fun JsonNode.asBooleanOrNull(): Boolean? = if (this.isMissingNode || this.isNull) null else this.asBoolean()

fun parseLocalDate(value: String?): LocalDate? =
    value?.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    }

fun parseLocalTime(value: String?): LocalTime? =
    value?.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalTime.parse(it) }.getOrNull()
    }

fun parseOffsetDateTime(value: String?): OffsetDateTime? =
    value?.takeIf { it.isNotBlank() }?.let {
        runCatching { OffsetDateTime.parse(it) }.getOrNull()
    }
