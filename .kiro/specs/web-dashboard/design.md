# Design Document - Web Dashboard

## Overview

Web-based dashboard providing raider and administrative interfaces for FLPS transparency and guild management.

## Technology Stack

**Frontend**: React + TypeScript + Vite
**UI Library**: Material-UI or Tailwind CSS
**State Management**: React Query + Zustand
**Authentication**: OAuth2 (Discord + Battle.net)
**Real-time**: WebSocket (Socket.IO or native)
**Charts**: Recharts or Chart.js

**Backend**: Existing Spring Boot REST API (enhanced)

## Architecture

```
┌─────────────────────────────────────────┐
│         React Frontend (SPA)            │
│  ┌───────────────────────────────────┐  │
│  │  Pages: Dashboard, Leaderboard,   │  │
│  │  History, Admin, Config           │  │
│  └───────────────┬───────────────────┘  │
│  ┌───────────────▼───────────────────┐  │
│  │  Components: ScoreCard, Table,    │  │
│  │  Chart, Modal, Form               │  │
│  └───────────────┬───────────────────┘  │
│  ┌───────────────▼───────────────────┐  │
│  │  Services: API Client, Auth,      │  │
│  │  WebSocket, Cache                 │  │
│  └───────────────┬───────────────────┘  │
└──────────────────┼─────────────────────┘
                   │ HTTPS/WSS
┌──────────────────▼─────────────────────┐
│      Spring Boot Backend (API)         │
│  ┌───────────────────────────────────┐  │
│  │  Controllers: FLPS, Guild, Auth   │  │
│  └───────────────┬───────────────────┘  │
│  ┌───────────────▼───────────────────┐  │
│  │  Services: Existing + WebSocket   │  │
│  └───────────────┬───────────────────┘  │
│  ┌───────────────▼───────────────────┐  │
│  │  Security: OAuth2, JWT, RBAC      │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

## Key Components

### Frontend Pages

1. **Dashboard** (`/dashboard`)
   - Personal FLPS score card
   - Component breakdown (RMS, IPI, RDF)
   - Recent loot history
   - Active penalties/bonuses

2. **Leaderboard** (`/leaderboard`)
   - Sortable table of all raiders
   - Filters: role, class, eligibility
   - Comparison view

3. **Loot History** (`/history`)
   - Personal and guild-wide loot awards
   - Filters: date, tier, character
   - RDF tracking

4. **Wishlist** (`/wishlist`)
   - Personal wishlist with upgrade values
   - Simulation status
   - Item details

5. **Performance** (`/performance`)
   - Warcraft Logs metrics
   - Performance trends
   - MAS breakdown

6. **Admin Panel** (`/admin`)
   - Guild configuration
   - Behavioral actions
   - Loot bans
   - User management

### Backend Enhancements

```kotlin
// New controllers
@RestController
@RequestMapping("/api/dashboard")
class DashboardController

@RestController
@RequestMapping("/api/auth")
class AuthController

// WebSocket support
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig

@Controller
class FlpsWebSocketController
```

### Database Schema

```sql
-- V0018__add_dashboard_tables.sql

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    discord_id VARCHAR(255) UNIQUE,
    battlenet_id VARCHAR(255) UNIQUE,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_login TIMESTAMP
);

CREATE TABLE user_character_mappings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(user_id, character_name, character_realm)
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(255),
    changes TEXT,
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## API Endpoints

```
# Authentication
POST   /api/auth/discord/callback
POST   /api/auth/battlenet/callback
POST   /api/auth/refresh
POST   /api/auth/logout
GET    /api/auth/me

# Dashboard
GET    /api/dashboard/me
GET    /api/dashboard/leaderboard
GET    /api/dashboard/history
GET    /api/dashboard/wishlist
GET    /api/dashboard/performance

# Admin
GET    /api/admin/users
POST   /api/admin/users/{userId}/role
POST   /api/admin/behavioral-actions
PUT    /api/admin/behavioral-actions/{id}
DELETE /api/admin/behavioral-actions/{id}
POST   /api/admin/loot-bans
PUT    /api/admin/loot-bans/{id}
DELETE /api/admin/loot-bans/{id}

# WebSocket
/ws/flps - Real-time FLPS updates
```

## Configuration

```yaml
dashboard:
  frontend:
    url: ${DASHBOARD_URL:http://localhost:3000}
  
  oauth:
    discord:
      client-id: ${DISCORD_CLIENT_ID}
      client-secret: ${DISCORD_CLIENT_SECRET}
      redirect-uri: ${DISCORD_REDIRECT_URI}
    battlenet:
      client-id: ${BATTLENET_CLIENT_ID}
      client-secret: ${BATTLENET_CLIENT_SECRET}
      redirect-uri: ${BATTLENET_REDIRECT_URI}
  
  jwt:
    secret: ${JWT_SECRET}
    expiration-hours: 24
    refresh-expiration-days: 30
  
  websocket:
    enabled: true
    heartbeat-interval-seconds: 30
```
