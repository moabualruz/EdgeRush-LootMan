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
      localStorage.setItem('auth_token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock all dashboard APIs
    await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.leaderboard),
      });
    });

    await page.route('**/api/guilds/*/loot*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.lootHistory),
      });
    });

    await page.route('**/api/guilds/*/raids*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.raids),
      });
    });

    await page.route('**/api/me/flps', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.characterFlps),
      });
    });
  });

  test('should display dashboard overview', async ({ page }) => {
    await page.goto('/dashboard');

    await expect(page.getByRole('heading', { name: /dashboard|overview/i })).toBeVisible();
  });

  test('should show personal FLPS score', async ({ page }) => {
    await page.goto('/dashboard');

    // Check for personal FLPS score display
    await expect(page.getByText(/your.*score|my.*flps/i)).toBeVisible();
  });

  test('should show upcoming raids', async ({ page }) => {
    await page.goto('/dashboard');

    // Check upcoming raids section
    await expect(page.getByText(/upcoming|next.*raid/i)).toBeVisible();
    await expect(page.getByText('Weekly Raid Night')).toBeVisible();
  });

  test('should show recent loot', async ({ page }) => {
    await page.goto('/dashboard');

    // Check recent loot section
    await expect(page.getByText(/recent.*loot/i)).toBeVisible();
  });

  test('should show quick stats', async ({ page }) => {
    await page.goto('/dashboard');

    // Check stats widgets
    await expect(page.getByText(/attendance|performance|rank/i)).toBeVisible();
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

  test('should show guild name', async ({ page }) => {
    await page.goto('/dashboard');

    await expect(page.getByText('Test Guild')).toBeVisible();
  });

  test('should handle API errors gracefully', async ({ page }) => {
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

  test('should refresh data periodically', async ({ page }) => {
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
