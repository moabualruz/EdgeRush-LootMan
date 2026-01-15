package com.edgerush.datasync.config

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
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
 * Unit tests for SopsEnvironmentPostProcessor.
 *
 * Note: These tests verify the processor's behavior without actually calling sops.
 * Integration tests with real SOPS decryption should be done manually or in CI
 * with appropriate keys configured.
 */
class SopsEnvironmentPostProcessorTest : UnitTest() {
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
    inner class WhenSopsNotConfigured {
        @Test
        fun `should skip decryption when SOPS_AGE_KEY_FILE not set`() {
            // Arrange
            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns null

            // Act
            processor.postProcessEnvironment(environment, application)

            // Assert
            propertySources.get("sops-secrets").shouldBeNull()
        }

        @Test
        fun `should skip decryption when SOPS_AGE_KEY_FILE is blank`() {
            // Arrange
            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns ""

            // Act
            processor.postProcessEnvironment(environment, application)

            // Assert
            propertySources.get("sops-secrets").shouldBeNull()
        }

        @Test
        fun `should skip decryption when SOPS_AGE_KEY_FILE is whitespace only`() {
            // Arrange
            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns "   "

            // Act
            processor.postProcessEnvironment(environment, application)

            // Assert
            propertySources.get("sops-secrets").shouldBeNull()
        }
    }

    @Nested
    inner class WhenSecretsFileNotFound {
        @Test
        fun `should skip decryption when secrets file does not exist`() {
            // Arrange
            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns "/path/to/keys.txt"
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns "/nonexistent/secrets.enc.yaml"

            // Act
            processor.postProcessEnvironment(environment, application)

            // Assert
            propertySources.get("sops-secrets").shouldBeNull()
        }

        @Test
        fun `should use default secrets file path when SOPS_SECRETS_FILE not set`() {
            // Arrange
            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns "/path/to/keys.txt"
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns null
            // Default path is "secrets/secrets.enc.yaml" which doesn't exist

            // Act
            processor.postProcessEnvironment(environment, application)

            // Assert - should skip because default file doesn't exist
            propertySources.get("sops-secrets").shouldBeNull()
        }
    }

    @Nested
    inner class YamlFlattening {
        private fun invokeFlattenYaml(yamlContent: String): Map<String, Any> {
            val method =
                SopsEnvironmentPostProcessor::class.java.getDeclaredMethod(
                    "flattenYaml",
                    String::class.java,
                )
            method.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return method.invoke(processor, yamlContent) as Map<String, Any>
        }

        private fun invokeFlattenMap(
            map: Map<String, Any>,
            prefix: String,
        ): Map<String, Any> {
            val method =
                SopsEnvironmentPostProcessor::class.java.getDeclaredMethod(
                    "flattenMap",
                    Map::class.java,
                    String::class.java,
                )
            method.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return method.invoke(processor, map, prefix) as Map<String, Any>
        }

        @Test
        fun `should flatten simple yaml to properties`() {
            // Arrange
            val yaml =
                """
                key1: value1
                key2: value2
                """.trimIndent()

            // Act
            val result = invokeFlattenYaml(yaml)

            // Assert
            result["key1"] shouldBe "value1"
            result["key2"] shouldBe "value2"
        }

        @Test
        fun `should flatten nested yaml structure`() {
            // Arrange
            val yaml =
                """
                wowaudit:
                  api_key: secret123
                  base_url: https://api.wowaudit.com
                """.trimIndent()

            // Act
            val result = invokeFlattenYaml(yaml)

            // Assert
            result["wowaudit.api_key"] shouldBe "secret123"
            result["wowaudit.base_url"] shouldBe "https://api.wowaudit.com"
        }

        @Test
        fun `should flatten deeply nested yaml structure`() {
            // Arrange
            val yaml =
                """
                level1:
                  level2:
                    level3:
                      value: deep_value
                """.trimIndent()

            // Act
            val result = invokeFlattenYaml(yaml)

            // Assert
            result["level1.level2.level3.value"] shouldBe "deep_value"
        }

        @Test
        fun `should handle mixed nesting levels`() {
            // Arrange
            val yaml =
                """
                top_level: simple_value
                nested:
                  child: nested_value
                  deeper:
                    leaf: leaf_value
                """.trimIndent()

            // Act
            val result = invokeFlattenYaml(yaml)

            // Assert
            result["top_level"] shouldBe "simple_value"
            result["nested.child"] shouldBe "nested_value"
            result["nested.deeper.leaf"] shouldBe "leaf_value"
        }

        @Test
        fun `should handle numeric values`() {
            // Arrange
            val yaml =
                """
                port: 8080
                timeout: 30.5
                """.trimIndent()

            // Act
            val result = invokeFlattenYaml(yaml)

            // Assert
            result["port"] shouldBe 8080
            result["timeout"] shouldBe 30.5
        }

        @Test
        fun `should handle boolean values`() {
            // Arrange
            val yaml =
                """
                enabled: true
                disabled: false
                """.trimIndent()

            // Act
            val result = invokeFlattenYaml(yaml)

            // Assert
            result["enabled"] shouldBe true
            result["disabled"] shouldBe false
        }

        @Test
        fun `should flatten map with empty prefix`() {
            // Arrange
            val map = mapOf("key" to "value")

            // Act
            val result = invokeFlattenMap(map, "")

            // Assert
            result["key"] shouldBe "value"
        }

        @Test
        fun `should flatten map with non-empty prefix`() {
            // Arrange
            val map = mapOf("key" to "value")

            // Act
            val result = invokeFlattenMap(map, "prefix")

            // Assert
            result["prefix.key"] shouldBe "value"
        }

        @Test
        fun `should handle nested maps recursively`() {
            // Arrange
            val map =
                mapOf(
                    "outer" to
                        mapOf(
                            "inner" to "value",
                        ),
                )

            // Act
            val result = invokeFlattenMap(map, "")

            // Assert
            result["outer.inner"] shouldBe "value"
        }
    }

    @Nested
    inner class DecryptSecretsHandling {
        @Test
        fun `should handle sops command not found gracefully`() {
            // Arrange
            val secretsFile = File(tempDir.toFile(), "secrets.enc.yaml")
            secretsFile.writeText("test: value")

            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns "/path/to/keys.txt"
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns secretsFile.absolutePath

            // Act - should not throw even if sops command fails
            processor.postProcessEnvironment(environment, application)

            // Assert - no secrets added because sops likely failed
            propertySources.get("sops-secrets").shouldBeNull()
        }
    }

    @Nested
    inner class ExceptionHandling {
        @Test
        fun `should handle exceptions during decryption gracefully`() {
            // Arrange
            val secretsFile = File(tempDir.toFile(), "secrets.enc.yaml")
            secretsFile.writeText("invalid yaml content { [ }")

            every { environment.getProperty("SOPS_AGE_KEY_FILE") } returns "/path/to/keys.txt"
            every { environment.getProperty("SOPS_SECRETS_FILE") } returns secretsFile.absolutePath

            // Act - should not throw
            processor.postProcessEnvironment(environment, application)

            // Assert - no secrets added due to error
            propertySources.get("sops-secrets").shouldBeNull()
        }
    }

    @Nested
    inner class PropertySourceCreation {
        @Test
        fun `should create property source with correct name`() {
            // The property source name should be "sops-secrets"
            // This is tested indirectly through other tests that verify
            // propertySources.get("sops-secrets")

            // Verify the constant is used correctly by checking class fields
            val field =
                SopsEnvironmentPostProcessor::class.java
                    .getDeclaredField("PROPERTY_SOURCE_NAME")
            field.isAccessible = true
            val propertySourceName = field.get(null) as String

            propertySourceName shouldBe "sops-secrets"
        }

        @Test
        fun `should use default secrets file path constant`() {
            val field =
                SopsEnvironmentPostProcessor::class.java
                    .getDeclaredField("DEFAULT_SECRETS_FILE")
            field.isAccessible = true
            val defaultSecretsFile = field.get(null) as String

            defaultSecretsFile shouldBe "secrets/secrets.enc.yaml"
        }
    }
}
