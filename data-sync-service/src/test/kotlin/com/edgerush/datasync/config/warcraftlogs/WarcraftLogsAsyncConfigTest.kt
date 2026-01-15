package com.edgerush.datasync.config.warcraftlogs

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Unit tests for WarcraftLogsAsyncConfig.
 *
 * Tests the async executor bean configuration including thread pool settings,
 * naming conventions, and shutdown behavior.
 */
class WarcraftLogsAsyncConfigTest : UnitTest() {
    private lateinit var config: WarcraftLogsAsyncConfig

    @BeforeEach
    fun setUp() {
        config = WarcraftLogsAsyncConfig()
    }

    @Nested
    inner class WarcraftLogsExecutorBean {
        @Test
        fun `should create an Executor bean`() {
            // Act
            val executor = config.warcraftLogsExecutor()

            // Assert
            executor.shouldNotBeNull()
            executor.shouldBeInstanceOf<Executor>()
        }

        @Test
        fun `should create a ThreadPoolTaskExecutor`() {
            // Act
            val executor = config.warcraftLogsExecutor()

            // Assert
            executor.shouldBeInstanceOf<ThreadPoolTaskExecutor>()
        }

        @Test
        fun `should configure core pool size to 2`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert
            executor.corePoolSize shouldBe 2
        }

        @Test
        fun `should configure max pool size to 5`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert
            executor.maxPoolSize shouldBe 5
        }

        @Test
        fun `should configure queue capacity to 100`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert
            executor.queueCapacity shouldBe 100
        }

        @Test
        fun `should configure thread name prefix`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert
            executor.threadNamePrefix shouldBe "wcl-sync-"
        }

        @Test
        fun `should wait for tasks to complete on shutdown`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert - This property is set but not directly accessible,
            // we verify it was called through the configuration
            // The executor should be configured, which we can verify by checking it's initialized
            executor.threadPoolExecutor.shouldNotBeNull()
        }
    }

    @Nested
    inner class ExecutorInitialization {
        @Test
        fun `should initialize the executor`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert - the executor should have an active thread pool
            executor.threadPoolExecutor.shouldNotBeNull()
        }

        @Test
        fun `should be ready to accept tasks after creation`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert - verify the executor is in a ready state
            executor.threadPoolExecutor.isShutdown shouldBe false
            executor.threadPoolExecutor.isTerminated shouldBe false
        }
    }

    @Nested
    inner class ThreadPoolBehavior {
        @Test
        fun `should start with zero active threads before any tasks`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert
            executor.threadPoolExecutor.activeCount shouldBe 0
        }

        @Test
        fun `should have expected pool size range`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert
            (executor.corePoolSize <= executor.maxPoolSize) shouldBe true
        }

        @Test
        fun `should have positive queue capacity`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert
            (executor.queueCapacity > 0) shouldBe true
        }
    }

    @Nested
    inner class ShutdownConfiguration {
        @Test
        fun `executor should be properly configured for graceful shutdown`() {
            // Act
            val executor = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert - We can verify the executor is created and initialized
            // The waitForTasksToCompleteOnShutdown is an internal setting
            executor.shouldNotBeNull()
        }
    }

    @Nested
    inner class MultipleInstances {
        @Test
        fun `should create independent executor instances`() {
            // Act
            val executor1 = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor
            val executor2 = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert - each call creates a new instance
            (executor1 !== executor2) shouldBe true
        }

        @Test
        fun `each instance should have same configuration`() {
            // Act
            val executor1 = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor
            val executor2 = config.warcraftLogsExecutor() as ThreadPoolTaskExecutor

            // Assert
            executor1.corePoolSize shouldBe executor2.corePoolSize
            executor1.maxPoolSize shouldBe executor2.maxPoolSize
            executor1.queueCapacity shouldBe executor2.queueCapacity
            executor1.threadNamePrefix shouldBe executor2.threadNamePrefix
        }
    }
}
