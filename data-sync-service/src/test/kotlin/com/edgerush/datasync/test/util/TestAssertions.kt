package com.edgerush.datasync.test.util

import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Custom assertion utilities for tests.
 *
 * Provides domain-specific assertions that make tests more readable.
 */
object TestAssertions {

    /**
     * Asserts that a FLPS score is valid (between 0.0 and 1.0).
     *
     * @param score The score to validate
     * @param message Optional custom error message
     */
    fun assertValidFlpsScore(score: Double, message: String = "FLPS score must be between 0.0 and 1.0") {
        score shouldBeGreaterThan -0.001
        score shouldBeLessThanOrEqual 1.001
    }

    /**
     * Asserts that two LocalDateTime values are approximately equal (within tolerance).
     *
     * @param actual The actual datetime
     * @param expected The expected datetime
     * @param toleranceSeconds The tolerance in seconds (default: 5)
     */
    fun assertDateTimeApproximately(
        actual: LocalDateTime,
        expected: LocalDateTime,
        toleranceSeconds: Long = 5
    ) {
        val diff = ChronoUnit.SECONDS.between(expected, actual)
        val absDiff = if (diff < 0) -diff else diff
        (absDiff <= toleranceSeconds) shouldBe true
    }

    /**
     * Asserts that a percentage is valid (between 0.0 and 100.0).
     *
     * @param percentage The percentage to validate
     */
    fun assertValidPercentage(percentage: Double) {
        percentage shouldBeGreaterThan -0.001
        percentage shouldBeLessThanOrEqual 100.001
    }

    /**
     * Asserts that a value is within a range (inclusive).
     *
     * @param value The value to check
     * @param min The minimum value (inclusive)
     * @param max The maximum value (inclusive)
     */
    fun assertInRange(value: Double, min: Double, max: Double) {
        value shouldBeGreaterThan min - 0.001
        value shouldBeLessThanOrEqual max + 0.001
    }
}
