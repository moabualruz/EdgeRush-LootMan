#!/bin/bash
# Setup script for SOPS + age secrets management
# Run this once to initialize secrets for the project

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
SECRETS_DIR="$PROJECT_ROOT/secrets"
KEYS_DIR="$HOME/.config/sops/age"

echo "=== EdgeRush LootMan Secrets Setup ==="
echo ""

# Check for required tools
check_tool() {
    if ! command -v "$1" &> /dev/null; then
        echo "Error: $1 is not installed."
        echo "Please install it first:"
        case "$1" in
            age)
                echo "  - macOS: brew install age"
                echo "  - Linux: apt install age / pacman -S age"
                echo "  - Windows: scoop install age / choco install age"
                ;;
            sops)
                echo "  - macOS: brew install sops"
                echo "  - Linux: Download from https://github.com/getsops/sops/releases"
                echo "  - Windows: scoop install sops / choco install sops"
                ;;
        esac
        exit 1
    fi
}

check_tool age
check_tool sops

echo "✓ age and sops are installed"
echo ""

# Create directories
mkdir -p "$SECRETS_DIR"
mkdir -p "$KEYS_DIR"

# Generate age key if not exists
KEY_FILE="$KEYS_DIR/keys.txt"
if [ -f "$KEY_FILE" ]; then
    echo "Age key already exists at: $KEY_FILE"
else
    echo "Generating new age key..."
    age-keygen -o "$KEY_FILE"
    chmod 600 "$KEY_FILE"
    echo "✓ Generated age key at: $KEY_FILE"
fi

# Extract public key
PUBLIC_KEY=$(grep "public key:" "$KEY_FILE" | cut -d: -f2 | tr -d ' ')
echo ""
echo "Your age public key: $PUBLIC_KEY"
echo ""

# Update .sops.yaml with the public key
SOPS_CONFIG="$PROJECT_ROOT/.sops.yaml"
if [ -f "$SOPS_CONFIG" ]; then
    echo "Updating .sops.yaml with your public key..."
    # Use sed to replace the placeholder
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s/age1xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx/$PUBLIC_KEY/g" "$SOPS_CONFIG"
    else
        sed -i "s/age1xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx/$PUBLIC_KEY/g" "$SOPS_CONFIG"
    fi
    echo "✓ Updated .sops.yaml"
fi

# Create initial secrets file if not exists
SECRETS_FILE="$SECRETS_DIR/secrets.enc.yaml"
SECRETS_TEMPLATE="$SECRETS_DIR/secrets.template.yaml"

if [ ! -f "$SECRETS_FILE" ]; then
    echo ""
    echo "Creating secrets template..."

    cat > "$SECRETS_TEMPLATE" << 'EOF'
# EdgeRush LootMan Secrets
# Edit this file and save, then encrypt with: sops -e secrets.template.yaml > secrets.enc.yaml

# WoWAudit Integration
wowaudit:
  api_key: "your-wowaudit-api-key-here"
  guild_uri: "https://wowaudit.com/REGION/REALM/GUILD/profile"

# Warcraft Logs Integration
warcraft_logs:
  client_id: "your-warcraft-logs-client-id"
  client_secret: "your-warcraft-logs-client-secret"

# Database (for production)
postgres:
  host: "localhost"
  port: 5432
  database: "edgerush"
  user: "edgerush"
  password: "your-secure-password-here"
EOF

    echo "✓ Created secrets template at: $SECRETS_TEMPLATE"
    echo ""
    echo "Next steps:"
    echo "1. Edit $SECRETS_TEMPLATE with your actual secrets"
    echo "2. Encrypt it: sops -e $SECRETS_TEMPLATE > $SECRETS_FILE"
    echo "3. Delete the unencrypted template: rm $SECRETS_TEMPLATE"
    echo "4. To edit later: sops $SECRETS_FILE"
else
    echo "Secrets file already exists at: $SECRETS_FILE"
    echo "To edit: sops $SECRETS_FILE"
fi

echo ""
echo "=== Setup Complete ==="
echo ""
echo "Environment variable to set:"
echo "  export SOPS_AGE_KEY_FILE=$KEY_FILE"
echo ""
echo "Add this to your shell profile (~/.bashrc, ~/.zshrc, etc.)"
