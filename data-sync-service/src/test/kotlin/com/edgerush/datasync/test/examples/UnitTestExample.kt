package com.edgerush.datasync.test.examples

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

/**
 * Example unit test demonstrating the AAA (Arrange-Act-Assert) pattern.
 *
 * This example shows:
 * - How to extend UnitTest base class
 * - How to use MockK for mocking
 * - How to follow the AAA pattern
 * - How to use Kotest matchers for assertions
 * - How to verify mock interactions
 */
class UnitTestExample : UnitTest() {
    // Example service interface
    interface Calculator {
        fun add(
            a: Int,
            b: Int,
        ): Int

        fun multiply(
            a: Int,
            b: Int,
        ): Int
    }

    // Example service that depends on Calculator
    class MathService(private val calculator: Calculator) {
        fun calculate(
            a: Int,
            b: Int,
        ): Int {
            val sum = calculator.add(a, b)
            return calculator.multiply(sum, 2)
        }
    }

    @Test
    fun `should calculate correctly when given valid inputs`() {
        // Arrange (Given) - Set up test data and mocks
        val calculator = mockk<Calculator>()
        val service = MathService(calculator)

        every { calculator.add(5, 3) } returns 8
        every { calculator.multiply(8, 2) } returns 16

        // Act (When) - Execute the code under test
        val result = service.calculate(5, 3)

        // Assert (Then) - Verify the results
        result shouldBe 16
        verify(exactly = 1) { calculator.add(5, 3) }
        verify(exactly = 1) { calculator.multiply(8, 2) }
    }

    @Test
    fun `should handle zero values correctly`() {
        // Arrange
        val calculator = mockk<Calculator>()
        val service = MathService(calculator)

        every { calculator.add(0, 0) } returns 0
        every { calculator.multiply(0, 2) } returns 0

        // Act
        val result = service.calculate(0, 0)

        // Assert
        result shouldBe 0
    }
}
