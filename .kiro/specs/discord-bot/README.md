# Discord Bot Spec

## Overview

Discord bot providing automated notifications and interactive commands for FLPS data access and guild management.

## Current Problem

All communication is manual. Officers must copy/paste FLPS data. Raiders have no self-service access to scores. No automated notifications for loot awards or RDF expiry.

## Solution

- Kotlin-based Discord bot using JDA library
- Slash commands for FLPS, loot history, leaderboard, wishlist
- Automated notifications for loot awards, RDF expiry, penalties
- Character linking system
- Admin commands for quick management

## Key Features

- `/flps` - Check FLPS score with detailed breakdown
- `/loot history` - View recent loot awards
- `/leaderboard` - Guild FLPS rankings
- `/wishlist` - Top wishlist items with upgrade values
- `/link` - Link Discord account to WoW character
- Automated loot award notifications
- RDF expiry DM notifications
- Penalty/ban notifications
- Admin commands for behavioral actions and loot bans

## Documents

- **requirements.md** - 12 requirements with acceptance criteria
- **design.md** - Technical design and architecture
- **tasks.md** - 22 major implementation tasks

## Estimated Effort

4-5 weeks for complete implementation

## Priority

**High** - Significantly improves operational efficiency

---

**Status**: ✅ Specification Complete - Ready for Implementation
