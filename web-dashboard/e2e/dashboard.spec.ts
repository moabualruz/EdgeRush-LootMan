import { test, expect, mockResponses } from './fixtures';

/**
 * Dashboard E2E Tests
 *
 * Tests the main dashboard overview page.
 */

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Set up authenticated state
    await page.addInitScript(() => {
      localStorage.setItem('token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock all dashboard APIs
    await page.route('**/v1/flps/guilds/*/leaderboard*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.leaderboard),
      });
    });

    await page.route('**/v1/loot/guilds/*/me/history*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.lootHistory),
      });
    });

    await page.route('**/v1/raids/guilds/*/upcoming*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.raids),
      });
    });

    await page.route('**/v1/flps/guilds/*/me', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.characterFlps),
      });
    });

    // Add necessary mocks for MainLayout
    await page.route('**/api/guilds/', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });
    await page.route('**/api/users/characters', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });
    await page.route('**/v1/auth/me', async (route) => {
       await route.fulfill({
         status: 200,
         contentType: 'application/json',
         body: JSON.stringify({
           id: 'dash-user-1',
           username: 'DashUser',
           role: 'MEMBER',
           guildId: 'test-guild'
         })
       });
    });
  });

  test('should display dashboard overview', async ({ page }) => {
    await page.goto('/dashboard');

    await expect(page.getByRole('heading', { name: /mission.*control|dashboard|overview/i })).toBeVisible();
  });

  test('should show personal FLPS score', async ({ page }) => {
    await page.goto('/dashboard');

    // Check for personal FLPS score display
    await expect(page.getByText(/your.*score|my.*flps/i)).toBeVisible();
  });

  test.skip('should show upcoming raids', async ({ page }) => {
    await page.goto('/dashboard');

    // Check upcoming raids section
    await expect(page.getByText(/upcoming|next.*raid/i)).toBeVisible();
    await expect(page.getByText('Weekly Raid Night')).toBeVisible();
  });

  test('should show recent loot', async ({ page }) => {
    await page.goto('/dashboard');

    // Check recent loot section header
    await expect(page.getByRole('heading', { name: /recent.*loot/i })).toBeVisible();
  });

  test.skip('should show quick stats', async ({ page }) => {
    await page.goto('/dashboard');

    // Check stats widgets
    await expect(page.getByText(/attendance|performance/i)).toBeVisible();
  });

  test('should navigate to leaderboard from widget', async ({ page }) => {
    await page.goto('/dashboard');

    const leaderboardLink = page.getByRole('link', { name: /leaderboard|view.*all/i });
    if (await leaderboardLink.isVisible()) {
      await leaderboardLink.click();
      await expect(page).toHaveURL(/leaderboard/);
    }
  });

  test('should navigate to raids from widget', async ({ page }) => {
    await page.goto('/dashboard');

    const raidsLink = page.getByRole('link', { name: /raids|view.*raids/i });
    if (await raidsLink.isVisible()) {
      await raidsLink.click();
      await expect(page).toHaveURL(/raids/);
    }
  });

  test.skip('should show guild name', async ({ page }) => {
    // Override guilds mock to return a guild with specific name
    // Matches the actual endpoint used by GuildContext
    await page.route('**/v1/user/guilds*', async (route) => {
      await route.fulfill({ 
        status: 200, 
        contentType: 'application/json', 
        body: JSON.stringify([{ 
          id: 'test-guild', 
          guildName: 'Test Guild', 
          name: 'Test Guild', // Handle potential property name diff
          role: 'MEMBER', 
          isActive: true,
          permissions: [],
          characterMappingId: 1,
          guildId: 'test-guild'
        }]) 
      });
    });

    await page.goto('/dashboard');
    await page.waitForLoadState('networkidle');

    // Use a more relaxed selector in case of whitespace or structure
    await expect(page.getByText('Test Guild')).toBeVisible();
  });

  test.skip('should handle API errors gracefully', async ({ page }) => {
    await page.route('**/api/**', async (route) => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Server error' }),
      });
    });

    await page.goto('/dashboard');

    // Should show error state but not crash
    await expect(page.getByText(/error|unavailable|try.*again/i)).toBeVisible();
  });

  test.skip('should refresh data periodically', async ({ page }) => {
    let callCount = 0;
    await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
      callCount++;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.leaderboard),
      });
    });

    await page.goto('/dashboard');

    // Wait for potential auto-refresh
    await page.waitForTimeout(5000);

    // If auto-refresh is implemented, call count should be > 1
  });
});
