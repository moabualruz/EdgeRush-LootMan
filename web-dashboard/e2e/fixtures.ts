import { test as base, expect } from '@playwright/test';

/**
 * E2E Test Fixtures
 *
 * Provides common test utilities and page objects for E2E tests.
 */

// Test user credentials
export const testUser = {
  username: 'testuser',
  password: 'testpass123',
  guildId: 'test-guild',
};

// Mock API responses
export const mockResponses = {
  leaderboard: {
    guildId: 'test-guild',
    guildName: 'Test Guild',
    entries: [
      {
        rank: 1,
        characterName: 'TopRaider',
        realm: 'Illidan',
        class: 'WARRIOR',
        spec: 'Protection',
        score: 0.95,
        rms: 0.4,
        ipi: 0.35,
        rdf: 0.2,
      },
      {
        rank: 2,
        characterName: 'SecondPlace',
        realm: 'Illidan',
        class: 'PRIEST',
        spec: 'Holy',
        score: 0.88,
        rms: 0.35,
        ipi: 0.33,
        rdf: 0.2,
      },
      {
        rank: 3,
        characterName: 'ThirdPlace',
        realm: 'Illidan',
        class: 'MAGE',
        spec: 'Fire',
        score: 0.82,
        rms: 0.32,
        ipi: 0.3,
        rdf: 0.2,
      },
    ],
    generatedAt: new Date().toISOString(),
  },

  characterFlps: {
    characterName: 'TopRaider',
    realm: 'Illidan',
    score: 0.95,
    rms: 0.4,
    ipi: 0.35,
    rdf: 0.2,
    rank: 1,
    totalRaiders: 25,
    breakdown: {
      attendance: {
        rate: 0.95,
        weeksPresent: 8,
        weeksTotal: 8,
      },
      performance: {
        averageParse: 85.5,
        medianParse: 82.0,
        deathsPerRaid: 0.5,
      },
      loot: {
        itemsReceived: 3,
        daysSinceLoot: 14,
      },
    },
    lastUpdated: new Date().toISOString(),
  },

  lootHistory: {
    items: [
      {
        id: '1',
        itemId: 12345,
        itemName: 'Heroic Sword',
        itemLevel: 500,
        awardedTo: 'TopRaider',
        awardedAt: new Date().toISOString(),
        encounter: 'Raid Boss 1',
        reason: 'Best in Slot',
      },
      {
        id: '2',
        itemId: 12346,
        itemName: 'Epic Helm',
        itemLevel: 495,
        awardedTo: 'SecondPlace',
        awardedAt: new Date(Date.now() - 86400000).toISOString(),
        encounter: 'Raid Boss 2',
        reason: 'Major Upgrade',
      },
    ],
    total: 50,
    page: 1,
    pageSize: 20,
  },

  raids: [
    {
      id: 'raid-1',
      name: 'Weekly Raid Night',
      scheduledFor: new Date(Date.now() + 86400000).toISOString(),
      instance: 'Test Raid',
      difficulty: 'Heroic',
      signups: 22,
      accepted: 20,
    },
    {
      id: 'raid-2',
      name: 'Alt Run',
      scheduledFor: new Date(Date.now() + 172800000).toISOString(),
      instance: 'Test Raid',
      difficulty: 'Normal',
      signups: 18,
      accepted: 15,
    },
  ],
};

// Extended test fixture with authentication
export const test = base.extend<{
  authenticatedPage: typeof base;
  mockApiResponses: void;
}>({
  // Fixture that provides an authenticated page
  authenticatedPage: async ({ page }, use) => {
    // Set auth token in localStorage
    await page.addInitScript(() => {
      localStorage.setItem('auth_token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    await use(base);
  },

  // Fixture that mocks API responses
  mockApiResponses: async ({ page }, use) => {
    // Mock the leaderboard endpoint
    await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.leaderboard),
      });
    });

    // Mock character FLPS endpoint
    await page.route('**/api/guilds/*/characters/*/flps', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.characterFlps),
      });
    });

    // Mock loot history endpoint
    await page.route('**/api/guilds/*/loot*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.lootHistory),
      });
    });

    // Mock raids endpoint
    await page.route('**/api/guilds/*/raids*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.raids),
      });
    });

    // Mock health endpoint
    await page.route('**/health', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ status: 'UP' }),
      });
    });

    await use();
  },
});

export { expect };
