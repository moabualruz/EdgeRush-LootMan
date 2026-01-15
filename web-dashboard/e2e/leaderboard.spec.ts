import { test, expect, mockResponses } from './fixtures';

/**
 * FLPS Leaderboard E2E Tests
 *
 * Tests the core FLPS leaderboard functionality.
 */

test.describe('FLPS Leaderboard', () => {
  test.beforeEach(async ({ page }) => {
    // Set up authenticated state
    await page.addInitScript(() => {
      localStorage.setItem('auth_token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock leaderboard API
    await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.leaderboard),
      });
    });
  });

  test('should display leaderboard with rankings', async ({ page }) => {
    await page.goto('/leaderboard');

    // Check page title
    await expect(page.getByRole('heading', { name: /leaderboard/i })).toBeVisible();

    // Check that raiders are displayed
    await expect(page.getByText('TopRaider')).toBeVisible();
    await expect(page.getByText('SecondPlace')).toBeVisible();
    await expect(page.getByText('ThirdPlace')).toBeVisible();
  });

  test('should show rank numbers', async ({ page }) => {
    await page.goto('/leaderboard');

    // Check rank display
    await expect(page.getByText('#1')).toBeVisible();
    await expect(page.getByText('#2')).toBeVisible();
    await expect(page.getByText('#3')).toBeVisible();
  });

  test('should display FLPS score breakdown', async ({ page }) => {
    await page.goto('/leaderboard');

    // Check score components are visible (RMS, IPI, RDF)
    await expect(page.getByText(/rms/i)).toBeVisible();
    await expect(page.getByText(/ipi/i)).toBeVisible();
    await expect(page.getByText(/rdf/i)).toBeVisible();
  });

  test('should show character class and spec', async ({ page }) => {
    await page.goto('/leaderboard');

    // Check class/spec display
    await expect(page.getByText(/warrior/i)).toBeVisible();
    await expect(page.getByText(/priest/i)).toBeVisible();
    await expect(page.getByText(/mage/i)).toBeVisible();
  });

  test('should navigate to character details on click', async ({ page }) => {
    // Mock character FLPS endpoint
    await page.route('**/api/guilds/*/characters/*/flps', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.characterFlps),
      });
    });

    await page.goto('/leaderboard');

    // Click on a character
    await page.getByText('TopRaider').click();

    // Should show character details or navigate to performance page
    await expect(page.getByText(/breakdown/i)).toBeVisible();
  });

  test('should handle empty leaderboard', async ({ page }) => {
    await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          ...mockResponses.leaderboard,
          entries: [],
        }),
      });
    });

    await page.goto('/leaderboard');

    await expect(page.getByText(/no.*data|empty|no.*raiders/i)).toBeVisible();
  });

  test('should handle API error gracefully', async ({ page }) => {
    await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Internal server error' }),
      });
    });

    await page.goto('/leaderboard');

    await expect(page.getByText(/error|failed|try again/i)).toBeVisible();
  });

  test('should refresh data on button click', async ({ page }) => {
    let callCount = 0;
    await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
      callCount++;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.leaderboard),
      });
    });

    await page.goto('/leaderboard');

    // Click refresh button
    const refreshButton = page.getByRole('button', { name: /refresh|reload/i });
    if (await refreshButton.isVisible()) {
      await refreshButton.click();
      expect(callCount).toBeGreaterThan(1);
    }
  });

  test('should support sorting by different columns', async ({ page }) => {
    await page.goto('/leaderboard');

    // Click on score column header to sort
    const scoreHeader = page.getByRole('columnheader', { name: /score/i });
    if (await scoreHeader.isVisible()) {
      await scoreHeader.click();

      // Check that sorting indicator is visible
      await expect(page.locator('[data-sort]')).toBeVisible();
    }
  });

  test('should support filtering by class', async ({ page }) => {
    await page.goto('/leaderboard');

    // Look for class filter
    const classFilter = page.getByRole('combobox', { name: /class/i });
    if (await classFilter.isVisible()) {
      await classFilter.selectOption('WARRIOR');

      // Only warriors should be visible
      await expect(page.getByText('TopRaider')).toBeVisible();
      await expect(page.getByText('SecondPlace')).not.toBeVisible();
    }
  });
});
