package com.edgerush.datasync.config

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.StringReader

/**
 * Spring Boot EnvironmentPostProcessor that decrypts SOPS-encrypted secrets
 * and adds them to the application's property sources.
 *
 * This processor runs early in the Spring Boot startup lifecycle, before beans
 * are created, allowing encrypted secrets to be used in configuration.
 *
 * ## Usage
 *
 * Set the following environment variables:
 * - `SOPS_AGE_KEY_FILE`: Path to the age key file (e.g., ~/.config/sops/age/keys.txt)
 * - `SOPS_SECRETS_FILE`: (Optional) Path to encrypted secrets file (default: secrets/secrets.enc.yaml)
 *
 * ## How it works
 *
 * 1. Checks if SOPS_AGE_KEY_FILE environment variable is set
 * 2. If set, runs `sops -d <secrets-file>` to decrypt the secrets
 * 3. Parses the decrypted YAML and flattens nested keys (e.g., `wowaudit.api_key`)
 * 4. Adds decrypted values to Spring's environment with high precedence
 *
 * ## Fallback
 *
 * If SOPS is not configured or decryption fails, the processor logs a warning
 * and continues without adding secrets. The application can then fall back to
 * other configuration sources (environment variables, application.yaml, etc.).
 */
class SopsEnvironmentPostProcessor : EnvironmentPostProcessor {

    companion object {
        private val logger = LoggerFactory.getLogger(SopsEnvironmentPostProcessor::class.java)
        private const val PROPERTY_SOURCE_NAME = "sops-secrets"
        private const val DEFAULT_SECRETS_FILE = "secrets/secrets.enc.yaml"
    }

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication
    ) {
        val ageKeyFile = environment.getProperty("SOPS_AGE_KEY_FILE")
            ?: System.getenv("SOPS_AGE_KEY_FILE")

        if (ageKeyFile.isNullOrBlank()) {
            logger.debug("SOPS_AGE_KEY_FILE not set, skipping secrets decryption")
            return
        }

        val secretsFile = environment.getProperty("SOPS_SECRETS_FILE")
            ?: System.getenv("SOPS_SECRETS_FILE")
            ?: DEFAULT_SECRETS_FILE

        if (!File(secretsFile).exists()) {
            logger.debug("Secrets file not found at $secretsFile, skipping SOPS decryption")
            return
        }

        try {
            val decryptedYaml = decryptSecrets(secretsFile, ageKeyFile)
            if (decryptedYaml != null) {
                val properties = flattenYaml(decryptedYaml)
                val propertySource = MapPropertySource(PROPERTY_SOURCE_NAME, properties)
                environment.propertySources.addFirst(propertySource)
                logger.info("Loaded ${properties.size} secrets from SOPS-encrypted file")
            }
        } catch (e: Exception) {
            logger.warn("Failed to decrypt SOPS secrets: ${e.message}. Falling back to other configuration sources.")
        }
    }

    /**
     * Decrypts a SOPS-encrypted file using the sops CLI.
     *
     * @param secretsFile Path to the encrypted secrets file
     * @param ageKeyFile Path to the age key file
     * @return Decrypted YAML content, or null if decryption fails
     */
    private fun decryptSecrets(secretsFile: String, ageKeyFile: String): String? {
        val processBuilder = ProcessBuilder("sops", "-d", secretsFile)
            .apply {
                environment()["SOPS_AGE_KEY_FILE"] = ageKeyFile
            }
            .redirectErrorStream(true)

        val process = processBuilder.start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        return if (exitCode == 0) {
            output
        } else {
            logger.warn("SOPS decryption failed with exit code $exitCode: $output")
            null
        }
    }

    /**
     * Flattens a YAML document into a map of dot-separated property names.
     *
     * Example:
     * ```yaml
     * wowaudit:
     *   api_key: "secret"
     * ```
     * Becomes: `{"wowaudit.api_key": "secret"}`
     *
     * @param yamlContent The YAML content to flatten
     * @return Map of flattened property names to values
     */
    @Suppress("UNCHECKED_CAST")
    private fun flattenYaml(yamlContent: String): Map<String, Any> {
        val yaml = Yaml()
        val parsed = yaml.load<Map<String, Any>>(StringReader(yamlContent))
        return flattenMap(parsed, "")
    }

    private fun flattenMap(map: Map<String, Any>, prefix: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        for ((key, value) in map) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"

            when (value) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    result.putAll(flattenMap(value as Map<String, Any>, fullKey))
                }
                else -> {
                    result[fullKey] = value
                }
            }
        }

        return result
    }
}
