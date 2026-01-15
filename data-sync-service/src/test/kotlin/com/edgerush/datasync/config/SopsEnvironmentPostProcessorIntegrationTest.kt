package com.edgerush.datasync.config

import com.edgerush.datasync.test.base.EnabledIfSopsAvailable
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MutablePropertySources
import java.io.File
import java.nio.file.Path

/**
 * SOPS-dependent integration tests for SopsEnvironmentPostProcessor.
 *
 * These tests actually execute SOPS commands and require SOPS to be available.
 * They are skipped when SOPS is not available.
 */
@EnabledIfSopsAvailable
class SopsEnvironmentPostProcessorIntegrationTest : UnitTest() {
    private lateinit var processor: SopsEnvironmentPostProcessor
    private lateinit var environment: ConfigurableEnvironment
    private lateinit var application: SpringApplication
    private lateinit var propertySources: MutablePropertySources

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        processor = SopsEnvironmentPostProcessor()
        environment = mockk(relaxed = true)
        application = mockk(relaxed = true)
        propertySources = MutablePropertySources()

        every { environment.propertySources } returns propertySources
    }

    @Nested
    inner class SopsVersionCheck {
        @Test
        fun `sops version command should succeed when SOPS is available`() {
            // Arrange
            val processBuilder =
                ProcessBuilder("sops", "--version")
                    .redirectErrorStream(true)

            // Act
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            // Assert
            exitCode shouldBe 0
            output shouldNotBe ""
        }
    }

    @Nested
    inner class DecryptSecretsExecution {
        @Test
        fun `should handle decryption of non-SOPS encrypted file`() {
            // Arrange - Create a regular YAML file (not SOPS encrypted)
            val secretsFile = File(tempDir.toFile(), "plain-secrets.yaml")
            secretsFile.writeText(
                """
                database:
                  password: plaintext123
                api_key: myapikey
                """.trimIndent(),
            )

            val ageKeyFile = File(tempDir.toFile(), "keys.txt")
            ageKeyFile.writeText("# fake age key for testing\n")

            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns ageKeyFile.absolutePath
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns secretsFile.absolutePath

            // Act - Try to decrypt a non-encrypted file (should fail gracefully)
            processor.postProcessEnvironment(environment, application)

            // Assert - No secrets should be added because file isn't SOPS encrypted
            // SOPS will return non-zero exit code for non-encrypted files
            propertySources.get("sops-secrets") shouldBe null
        }

        @Test
        fun `should handle invalid age key file path`() {
            // Arrange
            val secretsFile = File(tempDir.toFile(), "secrets.enc.yaml")
            secretsFile.writeText(
                """
                sops:
                  version: 3.7.3
                database:
                  password: ENC[AES256_GCM,data...]
                """.trimIndent(),
            )

            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns "/nonexistent/keys.txt"
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns secretsFile.absolutePath

            // Act
            processor.postProcessEnvironment(environment, application)

            // Assert - No secrets added because of invalid key file
            propertySources.get("sops-secrets") shouldBe null
        }

        @Test
        fun `should handle empty secrets file`() {
            // Arrange
            val secretsFile = File(tempDir.toFile(), "empty-secrets.yaml")
            secretsFile.writeText("")

            val ageKeyFile = File(tempDir.toFile(), "keys.txt")
            ageKeyFile.writeText("")

            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns ageKeyFile.absolutePath
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns secretsFile.absolutePath

            // Act
            processor.postProcessEnvironment(environment, application)

            // Assert - No secrets added for empty file
            propertySources.get("sops-secrets") shouldBe null
        }

        @Test
        fun `should handle malformed YAML in secrets file`() {
            // Arrange
            val secretsFile = File(tempDir.toFile(), "malformed.yaml")
            secretsFile.writeText(
                """
                this is not: valid: yaml: content
                { invalid brackets [
                """.trimIndent(),
            )

            val ageKeyFile = File(tempDir.toFile(), "keys.txt")
            ageKeyFile.writeText("")

            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns ageKeyFile.absolutePath
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns secretsFile.absolutePath

            // Act - Should not throw
            processor.postProcessEnvironment(environment, application)

            // Assert - No secrets added due to malformed YAML
            propertySources.get("sops-secrets") shouldBe null
        }
    }

    @Nested
    inner class ProcessExecution {
        @Test
        fun `should invoke decryptSecrets method with valid file paths`() {
            // Arrange
            val secretsFile = File(tempDir.toFile(), "test-secrets.yaml")
            secretsFile.writeText("key: value")

            val ageKeyFile = File(tempDir.toFile(), "age-keys.txt")
            ageKeyFile.writeText("")

            // Use reflection to invoke decryptSecrets directly
            val method =
                SopsEnvironmentPostProcessor::class.java.getDeclaredMethod(
                    "decryptSecrets",
                    String::class.java,
                    String::class.java,
                )
            method.isAccessible = true

            // Act
            val result = method.invoke(processor, secretsFile.absolutePath, ageKeyFile.absolutePath)

            // Assert - Result will be null because file isn't actually SOPS encrypted
            // but the method was invoked successfully (no exception)
            result shouldBe null
        }

        @Test
        fun `should handle SOPS process with stderr output`() {
            // Arrange
            val secretsFile = File(tempDir.toFile(), "invalid-sops.yaml")
            secretsFile.writeText(
                """
                # This file is not SOPS encrypted, so SOPS will output an error
                plain: value
                """.trimIndent(),
            )

            val ageKeyFile = File(tempDir.toFile(), "keys.txt")
            ageKeyFile.writeText("")

            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns ageKeyFile.absolutePath
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns secretsFile.absolutePath

            // Act - Should handle SOPS error gracefully
            processor.postProcessEnvironment(environment, application)

            // Assert - No secrets added when SOPS fails
            propertySources.get("sops-secrets") shouldBe null
        }
    }

    @Nested
    inner class EnvironmentVariableFallback {
        @Test
        fun `should check system environment when property not in Spring environment`() {
            // Arrange - No property in environment, relies on System.getenv fallback
            val secretsFile = File(tempDir.toFile(), "secrets.yaml")
            secretsFile.writeText("test: value")

            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns null
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns null

            // Act - Will check System.getenv which will likely return null
            processor.postProcessEnvironment(environment, application)

            // Assert - No secrets added because env vars not set
            propertySources.get("sops-secrets") shouldBe null
        }
    }
}
