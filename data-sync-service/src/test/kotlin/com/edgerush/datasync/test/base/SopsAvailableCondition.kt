package com.edgerush.datasync.test.base

import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit 5 condition that checks if SOPS is available.
 *
 * Tests annotated with @EnabledIfSopsAvailable will be skipped
 * when SOPS is not available, instead of failing with an error.
 */
class SopsAvailableCondition : ExecutionCondition {
    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        return try {
            val process = ProcessBuilder("sops", "--version")
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                ConditionEvaluationResult.enabled("SOPS is available")
            } else {
                ConditionEvaluationResult.disabled("SOPS command failed with exit code $exitCode - skipping test")
            }
        } catch (e: Exception) {
            ConditionEvaluationResult.disabled("SOPS is not available: ${e.message} - skipping test")
        }
    }
}

/**
 * Annotation to mark tests that require SOPS.
 *
 * When SOPS is not available, tests will be skipped instead of failing.
 *
 * Usage:
 * ```kotlin
 * @EnabledIfSopsAvailable
 * class MySopsIntegrationTest : UnitTest() {
 *     // tests here will be skipped if SOPS is not available
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(SopsAvailableCondition::class)
annotation class EnabledIfSopsAvailable
