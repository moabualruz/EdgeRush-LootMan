#!/bin/bash

# EdgeRush LootMan WoW Addon Test Runner
# Requires: luarocks, busted

set -e

echo "======================================"
echo "EdgeRush LootMan - WoW Addon Tests"
echo "======================================"

# Check for busted
if ! command -v busted &> /dev/null; then
    echo "Error: busted is not installed"
    echo "Install with: luarocks install busted"
    exit 1
fi

# Run tests
echo ""
echo "Running unit tests..."
busted --verbose --pattern="_spec" spec/

echo ""
echo "======================================"
echo "All tests passed!"
echo "======================================"
