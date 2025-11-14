# Raidbots Integration Spec

## Overview

Integration with Raidbots API to provide accurate gear upgrade simulations for IPI (Item Priority Index) calculation, replacing wishlist percentage estimates with actual simulation data.

## Current Problem

Upgrade values are currently estimated using wishlist percentages, which is less accurate than simulation-based calculations. This impacts IPI accuracy (45% of IPI weight).

## Solution

- Integrate Raidbots Droptimizer API
- Generate SimulationCraft profiles from character data
- Submit and process simulation requests
- Calculate normalized upgrade values from simulation results
- Cache results for performance

## Key Features

- Async simulation processing with queue management
- Guild-specific simulation parameters
- Automatic simulation scheduling
- Fallback to wishlist percentages when unavailable
- Comprehensive caching strategy

## Documents

- **requirements.md** - 12 requirements with acceptance criteria
- **design.md** - Technical design and architecture
- **tasks.md** - 14 major implementation tasks

## Estimated Effort

3-4 weeks for complete implementation

## Priority

**Critical** - Required for accurate IPI calculations

---

**Status**: ✅ Specification Complete - Ready for Implementation
