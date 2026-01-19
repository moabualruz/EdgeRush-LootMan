package com.edgerush.datasync.test.base

import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.DockerClientFactory

/**
 * JUnit 5 condition that checks if Docker is available for Testcontainers.
 *
 * Tests annotated with @EnabledIfDockerAvailable will be skipped
 * when Docker is not available, instead of failing with an error.
 *
 * KNOWN ISSUE: Docker Desktop 4.55+ on Windows has a bug where its credential
 * helper proxy returns invalid info responses (Status 400 with empty fields),
 * which breaks Testcontainers' Docker detection. Tests will be skipped on
 * affected systems. Workarounds:
 *   - Run tests in WSL2 where Docker works natively
 *   - Use an external PostgreSQL via docker-compose instead of Testcontainers
 *   - Downgrade Docker Desktop to a version before 4.55.0
 *   - Wait for Docker Desktop to fix this bug
 *
 * See: https://java.testcontainers.org/on_failure.html
 */
class DockerAvailableCondition : ExecutionCondition {
    companion object {
        /**
         * Set to true to force run Docker-dependent tests even when Docker detection fails.
         * Useful for debugging or when you know Docker is available but detection is broken.
         */
        var forceEnabled: Boolean = System.getProperty("testcontainers.force.enabled", "false").toBoolean()
    }

    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        if (forceEnabled) {
            return ConditionEvaluationResult.enabled("Docker tests force-enabled via system property")
        }

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
