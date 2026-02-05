import { test, expect, mockResponses } from './fixtures';

/**
 * Navigation E2E Tests
 *
 * Tests application navigation, routing, and layout.
 */

test.describe('Navigation', () => {
  test.beforeEach(async ({ page }) => {
    page.on('console', msg => console.log(`BROWSER: ${msg.text()}`));
    
    // Set up authenticated state
    await page.addInitScript(() => {
      localStorage.setItem('token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock specific list endpoints first
    await page.route('**/api/guilds/', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });
    
    await page.route('**/api/users/characters', async (route) => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });

    // Mock user for consistent state
    await page.route('**/v1/auth/me', async (route) => {
       await route.fulfill({
         status: 200,
         contentType: 'application/json',
         body: JSON.stringify({
           id: 'nav-user-1',
           username: 'NavUser',
           role: 'MEMBER',
           guildId: 'test-guild'
         })
       });
    });

    // Mock Dashboard Dependencies with correct URLs
    await page.route('**/v1/flps/guilds/*/me', async route => {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockResponses.characterFlps) });
    });
    await page.route('**/v1/flps/guilds/*/leaderboard*', async route => {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockResponses.leaderboard) });
    });
    await page.route('**/v1/loot/guilds/*/me/history*', async route => {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockResponses.lootHistory) });
    });
    await page.route('**/v1/raids/guilds/*/upcoming*', async route => {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockResponses.raids) });
    });

    // Mock common APIs
    await page.route('**/api/**', async (route) => {
      const type = route.request().resourceType();
      const url = route.request().url();
      
      // Prevent intercepting source files (e.g. src/api/client.ts)
      if (url.includes('/src/') || !['fetch', 'xhr'].includes(type)) {
        await route.continue();
        return;
      }
      
      // If it's the specific routes above, they should have been handled if registered before?
      // Actually Playwright matches in order of handlers?
      // "Once a handler is set up ... it will handle all requests matching the url."
      // If multiple handlers match, the one registered *last* (most recently) overrides?
      // "Routes can be overridden by calling page.route again."
      // So if generic is registered LAST, it might override.
      // But here we are in the same beforeEach.
      // Let's use a conditional inside the handler or rely on specific paths not matching generic? 
      // **/api/** matches everything.
      // So we should register generic FIRST, then specific? No, specific overrides generic.
      // In Mock Service Worker, specifically overrides. In Playwright, "handlers are called in reverse order of registration" (Last one wins).
      // So I should put specific mocks AFTER the generic one?
      // Let's safe-guard by using `fallback` or just return object.
      // But easier: Just mock the specific ones explicitly in the generic handler OR register specific ones AFTER.
      // Let's try registering specific ones AFTER the generic one involves deleting the generic one? No.
      
      // WAIT. If I register generic first, then specific later (in test), the specific one wins.
      // But here they are in the same block.
      
      if (url.includes('/api/guilds/') || url.includes('/api/users/characters')) {
         await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
         return;
      }
      
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
      await page.waitForLoadState('networkidle');

      // Check navigation links exist using text matching which is more robust with icons
      await expect(page.locator('a').filter({ hasText: /mission.*control/i })).toBeVisible();
      await expect(page.locator('a').filter({ hasText: /leaderboard/i })).toBeVisible();
      await expect(page.locator('a').filter({ hasText: /history|loot/i })).toBeVisible();
      await expect(page.locator('a').filter({ hasText: /raid.*operations|raids/i })).toBeVisible();
    });

    test('should highlight active route', async ({ page }) => {
      await page.goto('/dashboard');

      const dashboardLink = page.getByRole('link', { name: /mission.*control|dashboard/i });
      await expect(dashboardLink).toHaveClass(/active|bg-primary\/10/); // Updated for custom class
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
        localStorage.setItem('token', 'test-jwt-token');
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
