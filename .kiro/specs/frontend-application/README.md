# Frontend Application Spec

## Overview

Full-featured web application for EdgeRush LootMan providing complete guild operations management, raider self-service, and administrative tools. This is a comprehensive application covering all system functionality, not just a dashboard view.

## Current Problem

No user-facing interface exists for FLPS data. Raiders cannot self-service check scores, history, or performance. Officers must manually communicate all information. Discord and WoW addon integrations require the same backend APIs.

## Solution

- React + TypeScript full-stack application
- OAuth2 authentication (Discord + Battle.net)
- Real-time updates via WebSocket/GraphQL subscriptions
- Responsive design for mobile, tablet, and desktop
- Complete admin panel for all guild operations
- Integration-ready backend APIs

## Technology Stack

- **Frontend**: React 18+ / TypeScript / Vite
- **UI Library**: Material-UI or Tailwind CSS + shadcn/ui
- **State**: React Query (server state) + Zustand (client state)
- **API Client**: GraphQL (Apollo) + REST fallback
- **Real-time**: WebSocket via GraphQL subscriptions
- **Charts**: Recharts or Chart.js
- **Testing**: Vitest + React Testing Library + Playwright

## Documents

- **requirements.md** - Full application requirements (expanded from dashboard spec)
- **design.md** - Technical architecture and component design
- **tasks.md** - Implementation tasks
- **backend-gaps.md** - Required backend work before frontend

## Dependencies

**Must Complete Before Frontend:**
1. Discord user linking infrastructure (backend)
2. OAuth2 authentication endpoints (backend)
3. WebSocket real-time infrastructure (backend)
4. User/character mapping tables (database)

## Priority

**High** - Required for transparency, user experience, and as foundation for Discord bot

---

**Status**: 📋 Specification In Progress
