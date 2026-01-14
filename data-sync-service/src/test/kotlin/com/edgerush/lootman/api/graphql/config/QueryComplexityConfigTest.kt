package com.edgerush.lootman.api.graphql.config

import com.edgerush.datasync.test.base.UnitTest
import graphql.analysis.MaxQueryComplexityInstrumentation
import graphql.analysis.MaxQueryDepthInstrumentation
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for GraphQL query complexity configuration.
 *
 * Tests that query complexity and depth limiting are properly configured
 * to prevent denial of service attacks via expensive queries.
 */
class QueryComplexityConfigTest : UnitTest() {

    private val config = QueryComplexityConfig()

    @Nested
    inner class MaxQueryComplexity {

        @Test
        fun `should create max query complexity instrumentation`() {
            // Act
            val instrumentation = config.maxQueryComplexityInstrumentation()

            // Assert
            instrumentation.shouldNotBeNull()
            instrumentation.shouldBeInstanceOf<MaxQueryComplexityInstrumentation>()
        }
    }

    @Nested
    inner class MaxQueryDepth {

        @Test
        fun `should create max query depth instrumentation`() {
            // Act
            val instrumentation = config.maxQueryDepthInstrumentation()

            // Assert
            instrumentation.shouldNotBeNull()
            instrumentation.shouldBeInstanceOf<MaxQueryDepthInstrumentation>()
        }
    }
}
