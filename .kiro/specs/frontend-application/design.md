# Design Document - Frontend Application

## Overview

Full-featured React + TypeScript web application providing comprehensive guild operations management, raider self-service, and administrative tools for EdgeRush LootMan.

---

## Technology Stack

### Frontend Core
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.x | UI framework |
| TypeScript | 5.x | Type safety |
| Vite | 5.x | Build tool |
| React Router | 6.x | Routing |

### State Management
| Technology | Purpose |
|------------|---------|
| TanStack Query (React Query) | Server state, caching, mutations |
| Zustand | Client state (UI, preferences) |

### UI Framework
| Technology | Purpose |
|------------|---------|
| Tailwind CSS | Utility-first styling |
| shadcn/ui | Component library |
| Radix UI | Accessible primitives |
| Lucide React | Icons |

### Data Layer
| Technology | Purpose |
|------------|---------|
| GraphQL (Apollo Client) | Primary API communication |
| Axios | REST API fallback |
| WebSocket | Real-time updates |

### Charts & Visualization
| Technology | Purpose |
|------------|---------|
| Recharts | Charts and graphs |
| React Table (TanStack Table) | Data tables |

### Testing
| Technology | Purpose |
|------------|---------|
| Vitest | Unit testing |
| React Testing Library | Component testing |
| Playwright | E2E testing |
| MSW | API mocking |

---

## Application Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    React Application                          │
│                                                               │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                      Pages                              │  │
│  │  Dashboard | Leaderboard | History | Wishlist | Admin   │  │
│  │  Performance | Attendance | Raids | Teams | Settings    │  │
│  └─────────────────────────┬──────────────────────────────┘  │
│                            │                                  │
│  ┌─────────────────────────▼──────────────────────────────┐  │
│  │                   Components                            │  │
│  │  Layout | ScoreCard | DataTable | Charts | Forms | ...  │  │
│  └─────────────────────────┬──────────────────────────────┘  │
│                            │                                  │
│  ┌─────────────────────────▼──────────────────────────────┐  │
│  │                    Hooks Layer                          │  │
│  │  useAuth | useFlps | useLoot | useRaider | useRealtime │  │
│  └─────────────────────────┬──────────────────────────────┘  │
│                            │                                  │
│  ┌─────────────────────────▼──────────────────────────────┐  │
│  │                   Services Layer                        │  │
│  │  API Client | Auth Service | WebSocket | Cache          │  │
│  └─────────────────────────┬──────────────────────────────┘  │
│                            │                                  │
└────────────────────────────┼──────────────────────────────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
        ┌─────────┐   ┌───────────┐   ┌───────────┐
        │ GraphQL │   │   REST    │   │ WebSocket │
        │   API   │   │   API     │   │  Events   │
        └─────────┘   └───────────┘   └───────────┘
```

---

## Project Structure

```
src/
├── app/                          # Application shell
│   ├── App.tsx                   # Root component
│   ├── Router.tsx                # Route configuration
│   └── providers/                # Context providers
│       ├── AuthProvider.tsx
│       ├── QueryProvider.tsx
│       └── ThemeProvider.tsx
│
├── pages/                        # Page components (routes)
│   ├── auth/
│   │   ├── LoginPage.tsx
│   │   └── CallbackPage.tsx
│   ├── dashboard/
│   │   └── DashboardPage.tsx
│   ├── leaderboard/
│   │   └── LeaderboardPage.tsx
│   ├── history/
│   │   └── LootHistoryPage.tsx
│   ├── wishlist/
│   │   └── WishlistPage.tsx
│   ├── performance/
│   │   └── PerformancePage.tsx
│   ├── attendance/
│   │   └── AttendancePage.tsx
│   ├── raids/
│   │   ├── RaidsPage.tsx
│   │   └── RaidDetailPage.tsx
│   ├── admin/
│   │   ├── AdminPage.tsx
│   │   ├── ConfigPage.tsx
│   │   ├── BehavioralActionsPage.tsx
│   │   ├── LootBansPage.tsx
│   │   └── UsersPage.tsx
│   ├── lootcouncil/
│   │   └── LootCouncilPage.tsx
│   └── settings/
│       └── SettingsPage.tsx
│
├── components/                   # Reusable components
│   ├── layout/
│   │   ├── AppShell.tsx
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   └── Footer.tsx
│   ├── common/
│   │   ├── LoadingSpinner.tsx
│   │   ├── ErrorBoundary.tsx
│   │   ├── EmptyState.tsx
│   │   └── ConfirmDialog.tsx
│   ├── flps/
│   │   ├── FlpsScoreCard.tsx
│   │   ├── FlpsBreakdown.tsx
│   │   └── FlpsTrend.tsx
│   ├── loot/
│   │   ├── LootHistoryTable.tsx
│   │   ├── LootAwardCard.tsx
│   │   └── RdfCountdown.tsx
│   ├── raider/
│   │   ├── RaiderCard.tsx
│   │   ├── RaiderAvatar.tsx
│   │   └── RaiderSelector.tsx
│   ├── leaderboard/
│   │   ├── LeaderboardTable.tsx
│   │   └── LeaderboardFilters.tsx
│   ├── wishlist/
│   │   ├── WishlistTable.tsx
│   │   └── ItemCard.tsx
│   ├── performance/
│   │   ├── PerformanceChart.tsx
│   │   └── MetricCard.tsx
│   ├── admin/
│   │   ├── ConfigForm.tsx
│   │   ├── ActionForm.tsx
│   │   └── BanForm.tsx
│   └── charts/
│       ├── FlpsDistributionChart.tsx
│       ├── AttendanceChart.tsx
│       └── TrendChart.tsx
│
├── hooks/                        # Custom React hooks
│   ├── useAuth.ts
│   ├── useCurrentUser.ts
│   ├── useFlps.ts
│   ├── useRaider.ts
│   ├── useLootHistory.ts
│   ├── useLeaderboard.ts
│   ├── useWishlist.ts
│   ├── usePerformance.ts
│   ├── useAttendance.ts
│   ├── useRaids.ts
│   ├── useBehavioralActions.ts
│   ├── useLootBans.ts
│   ├── useRealtime.ts
│   └── usePreferences.ts
│
├── services/                     # API and external services
│   ├── api/
│   │   ├── client.ts             # Axios instance
│   │   ├── graphql.ts            # Apollo client
│   │   └── types.ts              # API types
│   ├── auth/
│   │   ├── authService.ts
│   │   └── tokenStorage.ts
│   ├── websocket/
│   │   └── realtimeService.ts
│   └── storage/
│       └── localStorage.ts
│
├── graphql/                      # GraphQL queries/mutations
│   ├── queries/
│   │   ├── flps.graphql
│   │   ├── raider.graphql
│   │   ├── loot.graphql
│   │   └── guild.graphql
│   ├── mutations/
│   │   ├── loot.graphql
│   │   └── admin.graphql
│   └── subscriptions/
│       └── loot.graphql
│
├── types/                        # TypeScript types
│   ├── flps.ts
│   ├── raider.ts
│   ├── loot.ts
│   ├── guild.ts
│   └── user.ts
│
├── utils/                        # Utility functions
│   ├── formatters.ts
│   ├── validators.ts
│   ├── classColors.ts
│   └── constants.ts
│
├── styles/                       # Global styles
│   ├── globals.css
│   └── theme.ts
│
└── tests/                        # Test utilities
    ├── setup.ts
    ├── mocks/
    │   └── handlers.ts
    └── utils/
        └── render.tsx
```

---

## Key Components

### FlpsScoreCard

Primary component for displaying FLPS scores.

```tsx
interface FlpsScoreCardProps {
  score: FlpsScore;
  showBreakdown?: boolean;
  variant?: 'compact' | 'full';
  onExpand?: () => void;
}

function FlpsScoreCard({ score, showBreakdown, variant }: FlpsScoreCardProps) {
  const percentile = useFlpsPercentile(score.value);
  const color = getScoreColor(percentile);

  return (
    <Card>
      <CardHeader>
        <CardTitle>FLPS Score</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="text-4xl font-bold" style={{ color }}>
          {score.value.toFixed(3)}
        </div>
        {showBreakdown && <FlpsBreakdown breakdown={score.breakdown} />}
      </CardContent>
    </Card>
  );
}
```

### LeaderboardTable

Interactive sortable table for guild leaderboard.

```tsx
interface LeaderboardTableProps {
  guildId: string;
  filters: LeaderboardFilters;
  currentUserId?: string;
}

function LeaderboardTable({ guildId, filters, currentUserId }: LeaderboardTableProps) {
  const { data, isLoading } = useLeaderboard(guildId, filters);

  const columns = useMemo(() => [
    { id: 'rank', header: '#', cell: RankCell },
    { id: 'raider', header: 'Raider', cell: RaiderCell },
    { id: 'flps', header: 'FLPS', cell: FlpsCell },
    { id: 'rms', header: 'RMS', cell: ScoreCell },
    { id: 'ipi', header: 'IPI', cell: ScoreCell },
    { id: 'rdf', header: 'RDF', cell: RdfCell },
  ], []);

  return (
    <DataTable
      data={data}
      columns={columns}
      highlightRow={(row) => row.raiderId === currentUserId}
    />
  );
}
```

### LootCouncilView

Real-time loot council decision support interface.

```tsx
interface LootCouncilViewProps {
  itemId: string;
  guildId: string;
}

function LootCouncilView({ itemId, guildId }: LootCouncilViewProps) {
  const { eligibleRaiders, recommendation } = useEligibleRaiders(itemId, guildId);
  const { subscribe } = useRealtime();

  useEffect(() => {
    return subscribe('flpsUpdated', handleFlpsUpdate);
  }, []);

  return (
    <div className="grid grid-cols-3 gap-4">
      <Card className="col-span-2">
        <CardHeader>Eligible Raiders</CardHeader>
        <CardContent>
          <EligibleRaidersTable raiders={eligibleRaiders} />
        </CardContent>
      </Card>
      <Card>
        <CardHeader>Recommendation</CardHeader>
        <CardContent>
          <RecommendationCard recommendation={recommendation} />
        </CardContent>
      </Card>
    </div>
  );
}
```

---

## State Management

### Server State (TanStack Query)

```tsx
// hooks/useFlps.ts
export function useFlpsScore(raiderId: string) {
  return useQuery({
    queryKey: ['flps', raiderId],
    queryFn: () => flpsApi.getScore(raiderId),
    staleTime: 30_000, // 30 seconds
  });
}

export function useLeaderboard(guildId: string, filters: LeaderboardFilters) {
  return useQuery({
    queryKey: ['leaderboard', guildId, filters],
    queryFn: () => flpsApi.getLeaderboard(guildId, filters),
  });
}
```

### Client State (Zustand)

```tsx
// stores/uiStore.ts
interface UIState {
  sidebarCollapsed: boolean;
  theme: 'light' | 'dark' | 'system';
  leaderboardFilters: LeaderboardFilters;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setTheme: (theme: string) => void;
  setLeaderboardFilters: (filters: LeaderboardFilters) => void;
}

export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      sidebarCollapsed: false,
      theme: 'system',
      leaderboardFilters: defaultFilters,
      setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),
      setTheme: (theme) => set({ theme }),
      setLeaderboardFilters: (filters) => set({ leaderboardFilters: filters }),
    }),
    { name: 'lootman-ui' }
  )
);
```

---

## Authentication Flow

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Login Page    │────▶│  OAuth Provider │────▶│  Callback Page  │
│                 │     │  (Discord/BNet) │     │                 │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
                                                         │
                                                         ▼
                                                ┌─────────────────┐
                                                │  Backend Auth   │
                                                │  Exchange Code  │
                                                └────────┬────────┘
                                                         │
                                                         ▼
                                                ┌─────────────────┐
                                                │  JWT + Refresh  │
                                                │  Token Stored   │
                                                └────────┬────────┘
                                                         │
                                                         ▼
                                                ┌─────────────────┐
                                                │  Redirect to    │
                                                │  Dashboard      │
                                                └─────────────────┘
```

### Auth Service

```tsx
// services/auth/authService.ts
class AuthService {
  async getDiscordOAuthUrl(): Promise<string> {
    const response = await api.get('/api/v1/auth/discord/url');
    return response.data.url;
  }

  async exchangeDiscordCode(code: string): Promise<AuthTokens> {
    const response = await api.post('/api/v1/auth/discord/callback', { code });
    this.storeTokens(response.data);
    return response.data;
  }

  async refreshToken(): Promise<AuthTokens> {
    const refreshToken = this.getRefreshToken();
    const response = await api.post('/api/v1/auth/refresh', { refreshToken });
    this.storeTokens(response.data);
    return response.data;
  }

  async getCurrentUser(): Promise<User> {
    const response = await api.get('/api/v1/auth/me');
    return response.data;
  }

  logout(): void {
    this.clearTokens();
    window.location.href = '/login';
  }
}
```

---

## Real-time Updates

### WebSocket Service

```tsx
// services/websocket/realtimeService.ts
class RealtimeService {
  private socket: WebSocket | null = null;
  private subscribers: Map<string, Set<Function>> = new Map();

  connect(): void {
    this.socket = new WebSocket('wss://api.example.com/ws/events');

    this.socket.onmessage = (event) => {
      const message = JSON.parse(event.data);
      this.notifySubscribers(message.type, message.payload);
    };

    this.socket.onclose = () => {
      setTimeout(() => this.connect(), 5000); // Reconnect
    };
  }

  subscribe(eventType: string, callback: Function): () => void {
    if (!this.subscribers.has(eventType)) {
      this.subscribers.set(eventType, new Set());
    }
    this.subscribers.get(eventType)!.add(callback);

    return () => {
      this.subscribers.get(eventType)?.delete(callback);
    };
  }

  private notifySubscribers(eventType: string, payload: any): void {
    this.subscribers.get(eventType)?.forEach(cb => cb(payload));
  }
}
```

### Using Real-time Hook

```tsx
// hooks/useRealtime.ts
export function useRealtime() {
  const queryClient = useQueryClient();
  const realtimeService = useRealtimeService();

  const subscribe = useCallback((event: string, handler: Function) => {
    return realtimeService.subscribe(event, handler);
  }, []);

  // Auto-invalidate queries on certain events
  useEffect(() => {
    const unsubFlps = realtimeService.subscribe('flpsUpdated', () => {
      queryClient.invalidateQueries({ queryKey: ['flps'] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] });
    });

    const unsubLoot = realtimeService.subscribe('lootAwarded', () => {
      queryClient.invalidateQueries({ queryKey: ['lootHistory'] });
    });

    return () => {
      unsubFlps();
      unsubLoot();
    };
  }, []);

  return { subscribe };
}
```

---

## Routing

```tsx
// app/Router.tsx
const router = createBrowserRouter([
  {
    path: '/',
    element: <AppShell />,
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'leaderboard', element: <LeaderboardPage /> },
      { path: 'history', element: <LootHistoryPage /> },
      { path: 'wishlist', element: <WishlistPage /> },
      { path: 'performance', element: <PerformancePage /> },
      { path: 'attendance', element: <AttendancePage /> },
      { path: 'raids', element: <RaidsPage /> },
      { path: 'raids/:raidId', element: <RaidDetailPage /> },
      { path: 'loot-council', element: <LootCouncilPage /> },
      { path: 'settings', element: <SettingsPage /> },
      {
        path: 'admin',
        element: <AdminGuard><Outlet /></AdminGuard>,
        children: [
          { index: true, element: <AdminPage /> },
          { path: 'config', element: <ConfigPage /> },
          { path: 'behavioral-actions', element: <BehavioralActionsPage /> },
          { path: 'loot-bans', element: <LootBansPage /> },
          { path: 'users', element: <UsersPage /> },
        ],
      },
    ],
  },
  { path: 'login', element: <LoginPage /> },
  { path: 'auth/callback', element: <CallbackPage /> },
  { path: '*', element: <NotFoundPage /> },
]);
```

---

## API Integration

### GraphQL Client Setup

```tsx
// services/api/graphql.ts
const httpLink = createHttpLink({
  uri: '/graphql',
});

const authLink = setContext((_, { headers }) => {
  const token = tokenStorage.getAccessToken();
  return {
    headers: {
      ...headers,
      authorization: token ? `Bearer ${token}` : '',
    },
  };
});

const wsLink = new GraphQLWsLink(
  createClient({
    url: 'ws://localhost:8080/subscriptions',
    connectionParams: {
      authorization: tokenStorage.getAccessToken(),
    },
  })
);

const splitLink = split(
  ({ query }) => {
    const definition = getMainDefinition(query);
    return definition.kind === 'OperationDefinition' && definition.operation === 'subscription';
  },
  wsLink,
  authLink.concat(httpLink)
);

export const apolloClient = new ApolloClient({
  link: splitLink,
  cache: new InMemoryCache(),
});
```

### REST API Fallback

```tsx
// services/api/client.ts
const apiClient = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
});

apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      try {
        await authService.refreshToken();
        return apiClient.request(error.config);
      } catch {
        authService.logout();
      }
    }
    return Promise.reject(error);
  }
);
```

---

## Testing Strategy

### Unit Tests (Vitest)

```tsx
// tests/components/FlpsScoreCard.test.tsx
describe('FlpsScoreCard', () => {
  it('displays FLPS score with correct formatting', () => {
    const score = { value: 0.872, breakdown: mockBreakdown };
    render(<FlpsScoreCard score={score} />);

    expect(screen.getByText('0.872')).toBeInTheDocument();
  });

  it('shows breakdown when expanded', () => {
    const score = { value: 0.872, breakdown: mockBreakdown };
    render(<FlpsScoreCard score={score} showBreakdown />);

    expect(screen.getByText('RMS')).toBeInTheDocument();
    expect(screen.getByText('IPI')).toBeInTheDocument();
    expect(screen.getByText('RDF')).toBeInTheDocument();
  });
});
```

### Integration Tests (Playwright)

```tsx
// tests/e2e/dashboard.spec.ts
test.describe('Dashboard', () => {
  test('displays user FLPS score', async ({ page }) => {
    await page.goto('/dashboard');

    await expect(page.getByTestId('flps-score')).toBeVisible();
    await expect(page.getByTestId('flps-breakdown')).toBeVisible();
  });

  test('updates in real-time when score changes', async ({ page }) => {
    await page.goto('/dashboard');
    const initialScore = await page.getByTestId('flps-score').textContent();

    // Simulate score update via WebSocket
    await page.evaluate(() => {
      window.dispatchEvent(new CustomEvent('flpsUpdated', { detail: { value: 0.900 } }));
    });

    await expect(page.getByTestId('flps-score')).not.toHaveText(initialScore);
  });
});
```

---

## Performance Considerations

1. **Code Splitting**: Lazy load routes and heavy components
2. **Caching**: TanStack Query caching with appropriate stale times
3. **Optimistic Updates**: Immediate UI updates for mutations
4. **Virtualization**: Use react-window for large lists
5. **Image Optimization**: Lazy load item icons, use WebP format
6. **Bundle Size**: Monitor and split large dependencies

```tsx
// Lazy loading routes
const DashboardPage = lazy(() => import('./pages/dashboard/DashboardPage'));
const AdminPage = lazy(() => import('./pages/admin/AdminPage'));

// Virtualized list for large data
function LargeRaiderList({ raiders }: { raiders: Raider[] }) {
  return (
    <FixedSizeList
      height={600}
      itemCount={raiders.length}
      itemSize={50}
      width="100%"
    >
      {({ index, style }) => (
        <RaiderRow raider={raiders[index]} style={style} />
      )}
    </FixedSizeList>
  );
}
```

---

## Deployment Configuration

```yaml
# docker-compose.yml
services:
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "3000:80"
    environment:
      - VITE_API_URL=${API_URL}
      - VITE_WS_URL=${WS_URL}
    depends_on:
      - backend

# Dockerfile
FROM node:20-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

---

## Environment Variables

```env
# Frontend (.env)
VITE_API_URL=http://localhost:8080
VITE_GRAPHQL_URL=http://localhost:8080/graphql
VITE_WS_URL=ws://localhost:8080/subscriptions
VITE_DISCORD_CLIENT_ID=your-discord-client-id
VITE_BATTLENET_CLIENT_ID=your-battlenet-client-id
```
