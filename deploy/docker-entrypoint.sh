#!/bin/bash
# Docker entrypoint script for EdgeRush LootMan data-sync service
#
# This script handles:
# 1. SOPS secrets decryption (if SOPS_AGE_KEY_FILE is set)
# 2. Exporting decrypted secrets as environment variables
# 3. Starting the application
#
# Environment Variables:
# - SOPS_AGE_KEY_FILE: Path to the age key file (enables secrets decryption)
# - SOPS_SECRETS_FILE: Path to encrypted secrets (default: secrets/secrets.enc.yaml)

set -e

SECRETS_FILE="${SOPS_SECRETS_FILE:-secrets/secrets.enc.yaml}"

# Function to decrypt and export secrets
decrypt_secrets() {
    if [ -z "$SOPS_AGE_KEY_FILE" ]; then
        echo "[entrypoint] SOPS_AGE_KEY_FILE not set, skipping secrets decryption"
        return 0
    fi

    if [ ! -f "$SOPS_AGE_KEY_FILE" ]; then
        echo "[entrypoint] WARNING: Age key file not found at $SOPS_AGE_KEY_FILE"
        return 0
    fi

    if [ ! -f "$SECRETS_FILE" ]; then
        echo "[entrypoint] Secrets file not found at $SECRETS_FILE, skipping decryption"
        return 0
    fi

    # Check if sops is available
    if ! command -v sops &> /dev/null; then
        echo "[entrypoint] WARNING: sops command not found, skipping decryption"
        return 0
    fi

    echo "[entrypoint] Decrypting secrets from $SECRETS_FILE..."

    # Decrypt secrets and export as environment variables
    # Using Python for reliable YAML to env var conversion
    if command -v python3 &> /dev/null; then
        eval "$(sops -d "$SECRETS_FILE" | python3 -c '
import sys
import yaml

def flatten(d, parent_key="", sep="_"):
    items = []
    for k, v in d.items():
        new_key = f"{parent_key}{sep}{k}".upper() if parent_key else k.upper()
        if isinstance(v, dict):
            items.extend(flatten(v, new_key, sep).items())
        else:
            items.append((new_key, str(v)))
    return dict(items)

data = yaml.safe_load(sys.stdin)
if data:
    for key, value in flatten(data).items():
        # Escape special characters for shell
        value = value.replace("\\", "\\\\").replace("\"", "\\\"")
        print(f"export {key}=\"{value}\"")
')"
        echo "[entrypoint] Secrets loaded successfully"
    else
        echo "[entrypoint] WARNING: python3 not found, using direct SOPS integration"
        # Fall back to Spring Boot's SopsEnvironmentPostProcessor
    fi
}

# Decrypt secrets if configured
decrypt_secrets

# Execute the main command
echo "[entrypoint] Starting application: $@"
exec "$@"
