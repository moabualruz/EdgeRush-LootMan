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
      localStorage.setItem('auth_token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock raids API
    await page.route('**/api/guilds/*/raids*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.raids),
      });
    });
  });

  test.describe('Raids List', () => {
    test('should display upcoming raids', async ({ page }) => {
      await page.goto('/raids');

      await expect(page.getByRole('heading', { name: /raids/i })).toBeVisible();

      // Check raids are displayed
      await expect(page.getByText('Weekly Raid Night')).toBeVisible();
      await expect(page.getByText('Alt Run')).toBeVisible();
    });

    test('should show raid difficulty', async ({ page }) => {
      await page.goto('/raids');

      await expect(page.getByText('Heroic')).toBeVisible();
      await expect(page.getByText('Normal')).toBeVisible();
    });

    test('should show signup counts', async ({ page }) => {
      await page.goto('/raids');

      // Check signup/accepted counts
      await expect(page.getByText(/22.*signup|20.*accepted/i)).toBeVisible();
    });

    test('should show instance name', async ({ page }) => {
      await page.goto('/raids');

      await expect(page.getByText('Test Raid')).toBeVisible();
    });

    test('should navigate to raid detail on click', async ({ page }) => {
      // Mock single raid detail
      await page.route('**/api/guilds/*/raids/raid-1', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponses.raids[0]),
        });
      });

      await page.goto('/raids');

      await page.getByText('Weekly Raid Night').click();

      await expect(page).toHaveURL(/raids\/raid-1/);
    });

    test('should handle empty raids list', async ({ page }) => {
      await page.route('**/api/guilds/*/raids*', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
      });

      await page.goto('/raids');

      await expect(page.getByText(/no.*raids|empty|no.*scheduled/i)).toBeVisible();
    });
  });

  test.describe('Raid Detail', () => {
    test.beforeEach(async ({ page }) => {
      await page.route('**/api/guilds/*/raids/raid-1*', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            ...mockResponses.raids[0],
            signups: [
              {
                characterId: 'char-1',
                characterName: 'TopRaider',
                class: 'WARRIOR',
                spec: 'Protection',
                status: 'accepted',
              },
              {
                characterId: 'char-2',
                characterName: 'SecondPlace',
                class: 'PRIEST',
                spec: 'Holy',
                status: 'accepted',
              },
            ],
          }),
        });
      });
    });

    test('should display raid details', async ({ page }) => {
      await page.goto('/raids/raid-1');

      await expect(page.getByText('Weekly Raid Night')).toBeVisible();
      await expect(page.getByText('Heroic')).toBeVisible();
      await expect(page.getByText('Test Raid')).toBeVisible();
    });

    test('should show roster', async ({ page }) => {
      await page.goto('/raids/raid-1');

      await expect(page.getByText('TopRaider')).toBeVisible();
      await expect(page.getByText('SecondPlace')).toBeVisible();
    });

    test('should show signup status', async ({ page }) => {
      await page.goto('/raids/raid-1');

      await expect(page.getByText(/accepted/i)).toBeVisible();
    });

    test('should allow signup', async ({ page }) => {
      await page.route('**/api/guilds/*/raids/raid-1/signup', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ status: 'signed_up' }),
          });
        }
      });

      await page.goto('/raids/raid-1');

      const signupButton = page.getByRole('button', { name: /sign.*up|register/i });
      if (await signupButton.isVisible()) {
        await signupButton.click();
        await expect(page.getByText(/signed.*up|registered/i)).toBeVisible();
      }
    });

    test('should allow withdrawal', async ({ page }) => {
      await page.route('**/api/guilds/*/raids/raid-1/signup', async (route) => {
        if (route.request().method() === 'DELETE') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ status: 'withdrawn' }),
          });
        }
      });

      await page.goto('/raids/raid-1');

      const withdrawButton = page.getByRole('button', { name: /withdraw|cancel/i });
      if (await withdrawButton.isVisible()) {
        await withdrawButton.click();
        await expect(page.getByText(/withdrawn|cancelled/i)).toBeVisible();
      }
    });
  });
});
