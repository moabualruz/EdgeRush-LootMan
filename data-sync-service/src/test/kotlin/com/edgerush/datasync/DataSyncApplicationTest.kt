package com.edgerush.datasync

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Unit tests for DataSyncApplication.
 *
 * Note: Testing the main() function that calls runApplication<T>() is not practical
 * in unit tests because it attempts to start the full Spring context. The main()
 * function is exercised by integration tests which use @SpringBootTest.
 */
class DataSyncApplicationTest : UnitTest() {

    @Test
    fun `DataSyncApplication class should be instantiable`() {
        // Act
        val application = DataSyncApplication()

        // Assert
        application shouldNotBe null
    }

    @Test
    fun `DataSyncApplication should be annotated with SpringBootApplication`() {
        // Arrange
        val applicationClass = DataSyncApplication::class.java

        // Act
        val annotation = applicationClass.getAnnotation(SpringBootApplication::class.java)

        // Assert
        annotation shouldNotBe null
    }

    @Test
    fun `DataSyncApplication should have correct package`() {
        // Arrange
        val applicationClass = DataSyncApplication::class.java

        // Assert
        applicationClass.packageName shouldBe "com.edgerush.datasync"
    }
}
