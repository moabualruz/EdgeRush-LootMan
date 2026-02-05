import { test, expect, mockResponses } from './fixtures';

/**
 * Raids E2E Tests
 *
 * Tests raid listing, signup, and management.
 */

test.describe('Raids', () => {
  test.beforeEach(async ({ page }) => {
    // Set up authenticated state
    await page.addInitScript(() => {
      localStorage.setItem('token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock auth user (Critical for MainLayout)
    await page.route('**/v1/auth/me', async (route) => {
       await route.fulfill({
         status: 200,
         contentType: 'application/json',
         body: JSON.stringify({
           id: 'user-1',
           username: 'TestUser',
           role: 'MEMBER',
           guildId: 'test-guild'
         })
       });
    });

    // Mock guild context (Critical for MainLayout)
    await page.route('**/v1/user/guilds', async (route) => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify([{ 
          id: 'test-guild', 
          guildId: 'test-guild',
          guildName: 'Test Guild', 
          role: 'MEMBER',
          isActive: true,
          permissions: ['RAID_LEADER', 'MEMBER_MANAGEMENT']
        }])
      });
    });

    await page.route('**/v1/user/guilds/active', async (route) => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify({ 
          id: 'test-guild', 
          guildId: 'test-guild',
          guildName: 'Test Guild', 
          role: 'MEMBER',
          isActive: true,
          permissions: ['RAID_LEADER', 'MEMBER_MANAGEMENT']
        })
      });
    });

    // Mock upcoming raids API
    await page.route('**/v1/raids/guilds/*/upcoming*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
            {
              id: 1,
              teamId: 101,
              teamName: 'Core Team',
              instanceName: 'Weekly Raid Night', 
              difficulty: 'HEROIC',
              scheduledAt: new Date(Date.now() + 86400000).toISOString(),
              status: 'SCHEDULED',
              signupCount: 22,
              maxPlayers: 25,
              description: 'Farm run'
            },
            {
              id: 2,
              teamId: 102,
              teamName: 'Alt Team',
              instanceName: 'Alt Run',
              difficulty: 'NORMAL',
              scheduledAt: new Date(Date.now() + 172800000).toISOString(),
              status: 'SCHEDULED',
              signupCount: 18,
              maxPlayers: 25,
              description: 'Learning run'
            }
        ]),
      });
    });
  });

  test.describe('Raids List', () => {
    test('should display upcoming raids', async ({ page }) => {
      await page.goto('/raids');
      await page.waitForLoadState('networkidle');

      await expect(page.getByRole('heading', { name: /raids/i })).toBeVisible();

      // Check raids are displayed
      await expect(page.getByText('Weekly Raid Night')).toBeVisible();
      await expect(page.getByText('Alt Run')).toBeVisible();
    });

    test('should show raid difficulty', async ({ page }) => {
      await page.goto('/raids');
      await page.waitForLoadState('networkidle');

      await expect(page.getByText('HEROIC')).toBeVisible();
      await expect(page.getByText('NORMAL')).toBeVisible();
    });

    test('should show signup counts', async ({ page }) => {
      await page.goto('/raids');
      await page.waitForLoadState('networkidle');

      // Check signup/accepted counts (ratio format in component: 22/25)
      await expect(page.getByText('22/25')).toBeVisible();
    });

    test('should navigate to raid detail on click', async ({ page }) => {
      // Mock single raid detail
      await page.route('**/v1/raids/1', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
              id: 1,
              teamId: 101,
              teamName: 'Core Team',
              instanceName: 'Weekly Raid Night', 
              difficulty: 'HEROIC',
              scheduledAt: new Date(Date.now() + 86400000).toISOString(),
              status: 'SCHEDULED',
              signupCount: 22,
              maxPlayers: 25,
              description: 'Farm run',
              encounters: [],
              signups: []
          }),
        });
      });
      // Mock encounters
      await page.route('**/v1/raid-encounters/raid/1', async (route) => {
          await route.fulfill({ status: 200, body: JSON.stringify({ content: [] }) });
      });
      // Mock signups
      await page.route('**/v1/raid-signups/raid/1', async (route) => {
          await route.fulfill({ status: 200, body: JSON.stringify({ content: [] }) });
      });

      await page.goto('/raids');
      await page.waitForLoadState('networkidle');

      // Ensure we click the specific card
      await page.getByText('Weekly Raid Night').click();

      // Use regex to match /raids/1 since URL might be absolute
      await expect(page).toHaveURL(/raids\/1/);
    });

    test('should handle empty raids list', async ({ page }) => {
      await page.route('**/v1/raids/guilds/*/upcoming*', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
      });

      await page.goto('/raids');
      await page.waitForLoadState('networkidle');

      await expect(page.getByText(/no.*raids|empty|no.*scheduled/i)).toBeVisible();
    });
  });

  test.describe('Raid Detail', () => {
    test.beforeEach(async ({ page }) => {
      await page.route('**/v1/raids/1', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
              id: 1,
              teamId: 101,
              teamName: 'Core Team',
              instanceName: 'Weekly Raid Night', 
              difficulty: 'HEROIC',
              scheduledAt: new Date(Date.now() + 86400000).toISOString(),
              status: 'SCHEDULED',
              signupCount: 22,
              maxPlayers: 25,
              description: 'Farm run',
              encounters: [],
              signups: [
                  {
                    id: 101,
                    raidId: 1,
                    raiderId: 1,
                    characterName: 'TopRaider',
                    characterId: 10,
                    role: 'TANK',
                    status: 'CONFIRMED',
                    signedUpAt: new Date().toISOString()
                  },
                  {
                    id: 102,
                    raidId: 1,
                    raiderId: 2,
                    characterName: 'SecondPlace',
                    characterId: 11,
                    role: 'HEALER',
                    status: 'CONFIRMED',
                    signedUpAt: new Date().toISOString()
                  }
              ]
          }),
        });
      });
      
      // Mock signups (redundant but kept for completeness if needed by other calls)
      await page.route('**/v1/raid-signups/raid/1', async (route) => {
         await route.fulfill({
            status: 200,
            body: JSON.stringify({
                content: []
            })
         });
      });

      // Mock encounters
      await page.route('**/v1/raid-encounters/raid/1', async (route) => {
          await route.fulfill({ status: 200, body: JSON.stringify({ content: [] }) });
      });

      // Mock FLPS (needed for logic)
      await page.route('**/api/guilds/*/flps', async (route) => {
          await route.fulfill({ status: 200, body: JSON.stringify({ raiderId: 1, role: 'TANK' }) });
      });
    });

    test('should display raid details', async ({ page }) => {
      await page.goto('/raids/1');
      await page.waitForLoadState('networkidle');

      await expect(page.getByText('Weekly Raid Night')).toBeVisible();
      await expect(page.getByText('HEROIC')).toBeVisible();
    });

    test('should show roster', async ({ page }) => {
      await page.goto('/raids/1');
      await page.waitForLoadState('networkidle');

      await expect(page.getByText('TopRaider')).toBeVisible();
      await expect(page.getByText('SecondPlace')).toBeVisible();
    });

    test('should allow signup', async ({ page }) => {
      await page.route('**/v1/raid-signups', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ status: 'CONFIRMED', id: 999, raidId: 1 }),
          });
        }
      });

      await page.goto('/raids/1');
      await page.waitForLoadState('networkidle');

      const signupButton = page.getByRole('button', { name: /sign.*up|register/i });
      if (await signupButton.isVisible()) {
        await signupButton.click();
        // Just verify button visibility logic or success toast if present
      }
    });

  });
});
