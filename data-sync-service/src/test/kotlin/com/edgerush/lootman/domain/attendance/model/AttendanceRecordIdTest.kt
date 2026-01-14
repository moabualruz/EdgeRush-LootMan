package com.edgerush.lootman.domain.attendance.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AttendanceRecordIdTest : UnitTest() {

    @Nested
    inner class ConstructorTests {

        @Test
        fun `should create valid id with non-blank value`() {
            // Given & When
            val id = AttendanceRecordId("test-id-123")

            // Then
            id.value shouldBe "test-id-123"
        }

        @Test
        fun `should throw exception when value is blank`() {
            // Given & When & Then
            shouldThrow<IllegalArgumentException> {
                AttendanceRecordId("")
            }.message shouldBe "Attendance Record ID cannot be blank"
        }

        @Test
        fun `should throw exception when value is only whitespace`() {
            // Given & When & Then
            shouldThrow<IllegalArgumentException> {
                AttendanceRecordId("   ")
            }.message shouldBe "Attendance Record ID cannot be blank"
        }
    }

    @Nested
    inner class GenerateTests {

        @Test
        fun `should generate unique id`() {
            // Given & When
            val id1 = AttendanceRecordId.generate()
            val id2 = AttendanceRecordId.generate()

            // Then
            id1 shouldNotBe id2
            id1.value.shouldNotBeBlank()
            id2.value.shouldNotBeBlank()
        }

        @Test
        fun `should generate valid UUID format`() {
            // Given & When
            val id = AttendanceRecordId.generate()

            // Then
            id.value.length shouldBe 36 // UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
            id.value.count { it == '-' } shouldBe 4
        }
    }

    @Nested
    inner class EqualityTests {

        @Test
        fun `should be equal when values are the same`() {
            // Given
            val id1 = AttendanceRecordId("same-value")
            val id2 = AttendanceRecordId("same-value")

            // Then
            id1 shouldBe id2
        }

        @Test
        fun `should not be equal when values differ`() {
            // Given
            val id1 = AttendanceRecordId("value-1")
            val id2 = AttendanceRecordId("value-2")

            // Then
            id1 shouldNotBe id2
        }
    }
}
