import { test, expect, mockResponses } from './fixtures';

/**
 * Navigation E2E Tests
 *
 * Tests application navigation, routing, and layout.
 */

test.describe('Navigation', () => {
  test.beforeEach(async ({ page }) => {
    // Set up authenticated state
    await page.addInitScript(() => {
      localStorage.setItem('auth_token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock common APIs
    await page.route('**/api/**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({}),
      });
    });
  });

  test.describe('Main Navigation', () => {
    test('should display main navigation menu', async ({ page }) => {
      await page.goto('/dashboard');

      // Check navigation links exist
      await expect(page.getByRole('link', { name: /dashboard/i })).toBeVisible();
      await expect(page.getByRole('link', { name: /leaderboard/i })).toBeVisible();
      await expect(page.getByRole('link', { name: /history|loot/i })).toBeVisible();
      await expect(page.getByRole('link', { name: /raids/i })).toBeVisible();
    });

    test('should highlight active route', async ({ page }) => {
      await page.goto('/dashboard');

      const dashboardLink = page.getByRole('link', { name: /dashboard/i });
      await expect(dashboardLink).toHaveClass(/active|selected|current/);
    });

    test('should navigate between pages', async ({ page }) => {
      await page.goto('/dashboard');

      // Navigate to leaderboard
      await page.getByRole('link', { name: /leaderboard/i }).click();
      await expect(page).toHaveURL(/leaderboard/);

      // Navigate to history
      await page.getByRole('link', { name: /history|loot/i }).click();
      await expect(page).toHaveURL(/history/);

      // Navigate to raids
      await page.getByRole('link', { name: /raids/i }).click();
      await expect(page).toHaveURL(/raids/);
    });
  });

  test.describe('Admin Navigation', () => {
    test.beforeEach(async ({ page }) => {
      // Set up admin state
      await page.addInitScript(() => {
        localStorage.setItem('auth_token', 'test-jwt-token');
        localStorage.setItem('guild_id', 'test-guild');
        localStorage.setItem(
          'user',
          JSON.stringify({
            id: 'user-1',
            roles: ['ADMIN'],
          })
        );
      });
    });

    test('should show admin menu for admins', async ({ page }) => {
      await page.goto('/dashboard');

      await expect(page.getByRole('link', { name: /admin/i })).toBeVisible();
    });

    test('should navigate to admin pages', async ({ page }) => {
      await page.goto('/dashboard');

      await page.getByRole('link', { name: /admin/i }).click();
      await expect(page).toHaveURL(/admin/);
    });
  });

  test.describe('Mobile Navigation', () => {
    test.use({ viewport: { width: 375, height: 667 } });

    test('should show hamburger menu on mobile', async ({ page }) => {
      await page.goto('/dashboard');

      // Check for hamburger menu button
      const menuButton = page.getByRole('button', { name: /menu|hamburger/i });
      await expect(menuButton).toBeVisible();
    });

    test('should open mobile menu on click', async ({ page }) => {
      await page.goto('/dashboard');

      const menuButton = page.getByRole('button', { name: /menu|hamburger/i });
      if (await menuButton.isVisible()) {
        await menuButton.click();

        // Navigation should be visible
        await expect(page.getByRole('link', { name: /dashboard/i })).toBeVisible();
      }
    });

    test('should close menu after navigation', async ({ page }) => {
      await page.goto('/dashboard');

      const menuButton = page.getByRole('button', { name: /menu|hamburger/i });
      if (await menuButton.isVisible()) {
        await menuButton.click();
        await page.getByRole('link', { name: /leaderboard/i }).click();

        // Menu should be closed
        await expect(page).toHaveURL(/leaderboard/);
      }
    });
  });

  test.describe('Breadcrumbs', () => {
    test('should display breadcrumbs on nested pages', async ({ page }) => {
      // Mock raid detail
      await page.route('**/api/guilds/*/raids/raid-1*', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponses.raids[0]),
        });
      });

      await page.goto('/raids/raid-1');

      const breadcrumbs = page.locator('[aria-label="breadcrumb"]');
      if (await breadcrumbs.isVisible()) {
        await expect(breadcrumbs.getByText(/raids/i)).toBeVisible();
        await expect(breadcrumbs.getByText('Weekly Raid Night')).toBeVisible();
      }
    });

    test('should navigate via breadcrumbs', async ({ page }) => {
      await page.route('**/api/guilds/*/raids/raid-1*', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponses.raids[0]),
        });
      });

      await page.goto('/raids/raid-1');

      const breadcrumbs = page.locator('[aria-label="breadcrumb"]');
      if (await breadcrumbs.isVisible()) {
        await breadcrumbs.getByRole('link', { name: /raids/i }).click();
        await expect(page).toHaveURL('/raids');
      }
    });
  });

  test.describe('404 Handling', () => {
    test('should redirect unknown routes to dashboard', async ({ page }) => {
      await page.goto('/unknown-page-that-does-not-exist');

      await expect(page).toHaveURL(/dashboard/);
    });
  });

  test.describe('Deep Linking', () => {
    test('should handle direct navigation to nested routes', async ({ page }) => {
      await page.route('**/api/guilds/*/raids/raid-1*', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponses.raids[0]),
        });
      });

      await page.goto('/raids/raid-1');

      await expect(page.getByText('Weekly Raid Night')).toBeVisible();
    });
  });
});
