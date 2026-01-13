package com.edgerush.datasync.test.base

import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.DockerClientFactory

/**
 * JUnit 5 condition that checks if Docker is available.
 *
 * Tests annotated with @EnabledIfDockerAvailable will be skipped
 * when Docker is not available, instead of failing with an error.
 */
class DockerAvailableCondition : ExecutionCondition {
    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        return try {
            if (DockerClientFactory.instance().isDockerAvailable) {
                ConditionEvaluationResult.enabled("Docker is available")
            } else {
                ConditionEvaluationResult.disabled("Docker is not available - skipping test")
            }
        } catch (e: Exception) {
            ConditionEvaluationResult.disabled("Docker check failed: ${e.message} - skipping test")
        }
    }
}

/**
 * Annotation to mark tests that require Docker.
 *
 * When Docker is not available, tests will be skipped instead of failing.
 *
 * Usage:
 * ```kotlin
 * @EnabledIfDockerAvailable
 * class MyIntegrationTest : IntegrationTest() {
 *     // tests here will be skipped if Docker is not available
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(DockerAvailableCondition::class)
annotation class EnabledIfDockerAvailable
