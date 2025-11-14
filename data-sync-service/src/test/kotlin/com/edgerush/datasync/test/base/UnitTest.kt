package com.edgerush.datasync.test.base

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 * Base class for unit tests using MockK.
 *
 * This class provides:
 * - Automatic MockK initialization for @MockK and @InjectMockKs annotations
 * - Automatic cleanup of mocks after each test
 * - Fast execution (< 100ms per test)
 * - No external dependencies (database, network, etc.)
 *
 * Usage:
 * ```kotlin
 * class MyServiceTest : UnitTest() {
 *     @MockK
 *     private lateinit var dependency: Dependency
 *
 *     @InjectMockKs
 *     private lateinit var service: MyService
 *
 *     @Test
 *     fun `should do something when condition is met`() {
 *         // Arrange
 *         every { dependency.method() } returns "result"
 *
 *         // Act
 *         val result = service.doSomething()
 *
 *         // Assert
 *         result shouldBe "expected"
 *         verify(exactly = 1) { dependency.method() }
 *     }
 * }
 * ```
 */
abstract class UnitTest {

    @BeforeEach
    fun setupMocks() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
}
