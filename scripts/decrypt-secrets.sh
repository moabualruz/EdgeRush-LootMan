#!/bin/bash
# Decrypt secrets for CI/CD or local development
# Usage: ./scripts/decrypt-secrets.sh [output-format]
#   output-format: yaml (default), env, json

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
SECRETS_FILE="$PROJECT_ROOT/secrets/secrets.enc.yaml"
OUTPUT_FORMAT="${1:-yaml}"

# Check if secrets file exists
if [ ! -f "$SECRETS_FILE" ]; then
    echo "Error: Secrets file not found at $SECRETS_FILE"
    echo "Run ./scripts/setup-secrets.sh first"
    exit 1
fi

# Check for sops
if ! command -v sops &> /dev/null; then
    echo "Error: sops is not installed"
    exit 1
fi

case "$OUTPUT_FORMAT" in
    yaml)
        sops -d "$SECRETS_FILE"
        ;;
    json)
        sops -d --output-type json "$SECRETS_FILE"
        ;;
    env)
        # Convert to environment variables format
        sops -d "$SECRETS_FILE" | python3 -c "
import sys, yaml
def flatten(d, prefix=''):
    items = []
    for k, v in d.items():
        key = f'{prefix}_{k}'.upper() if prefix else k.upper()
        if isinstance(v, dict):
            items.extend(flatten(v, key))
        else:
            items.append(f'{key}={v}')
    return items

data = yaml.safe_load(sys.stdin)
print('\n'.join(flatten(data)))
"
        ;;
    *)
        echo "Unknown format: $OUTPUT_FORMAT"
        echo "Valid formats: yaml, json, env"
        exit 1
        ;;
esac
