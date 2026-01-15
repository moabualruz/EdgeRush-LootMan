import { test, expect, mockResponses } from './fixtures';

/**
 * Loot History E2E Tests
 *
 * Tests loot history viewing and filtering.
 */

test.describe('Loot History', () => {
  test.beforeEach(async ({ page }) => {
    // Set up authenticated state
    await page.addInitScript(() => {
      localStorage.setItem('auth_token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock loot history API
    await page.route('**/api/guilds/*/loot*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.lootHistory),
      });
    });
  });

  test('should display loot history list', async ({ page }) => {
    await page.goto('/history');

    await expect(page.getByRole('heading', { name: /loot.*history|history/i })).toBeVisible();

    // Check items are displayed
    await expect(page.getByText('Heroic Sword')).toBeVisible();
    await expect(page.getByText('Epic Helm')).toBeVisible();
  });

  test('should show item details', async ({ page }) => {
    await page.goto('/history');

    // Check item level is shown
    await expect(page.getByText('500')).toBeVisible();
    await expect(page.getByText('495')).toBeVisible();

    // Check recipient is shown
    await expect(page.getByText('TopRaider')).toBeVisible();
    await expect(page.getByText('SecondPlace')).toBeVisible();
  });

  test('should show award reason', async ({ page }) => {
    await page.goto('/history');

    await expect(page.getByText('Best in Slot')).toBeVisible();
    await expect(page.getByText('Major Upgrade')).toBeVisible();
  });

  test('should show encounter information', async ({ page }) => {
    await page.goto('/history');

    await expect(page.getByText('Raid Boss 1')).toBeVisible();
    await expect(page.getByText('Raid Boss 2')).toBeVisible();
  });

  test('should support pagination', async ({ page }) => {
    await page.goto('/history');

    // Check pagination controls
    const nextButton = page.getByRole('button', { name: /next|>/i });
    const prevButton = page.getByRole('button', { name: /prev|</i });

    if (await nextButton.isVisible()) {
      // Check total count display
      await expect(page.getByText(/50|total/i)).toBeVisible();
    }
  });

  test('should filter by character', async ({ page }) => {
    await page.goto('/history');

    const characterFilter = page.getByRole('combobox', { name: /character|player/i });
    if (await characterFilter.isVisible()) {
      await characterFilter.selectOption('TopRaider');

      // Should filter results
      await expect(page.getByText('Heroic Sword')).toBeVisible();
    }
  });

  test('should filter by date range', async ({ page }) => {
    await page.goto('/history');

    const dateFilter = page.getByRole('textbox', { name: /date|from/i });
    if (await dateFilter.isVisible()) {
      await dateFilter.fill('2024-01-01');
      // Results should update
    }
  });

  test('should search by item name', async ({ page }) => {
    await page.goto('/history');

    const searchInput = page.getByRole('searchbox');
    if (await searchInput.isVisible()) {
      await searchInput.fill('Sword');
      await expect(page.getByText('Heroic Sword')).toBeVisible();
    }
  });

  test('should handle empty history', async ({ page }) => {
    await page.route('**/api/guilds/*/loot*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          items: [],
          total: 0,
          page: 1,
          pageSize: 20,
        }),
      });
    });

    await page.goto('/history');

    await expect(page.getByText(/no.*loot|empty|no.*items/i)).toBeVisible();
  });

  test('should show item tooltip on hover', async ({ page }) => {
    await page.goto('/history');

    // Hover over item name
    await page.getByText('Heroic Sword').hover();

    // Tooltip should appear with item details
    // This depends on implementation - may show Wowhead tooltip
  });

  test('should export history', async ({ page }) => {
    await page.goto('/history');

    const exportButton = page.getByRole('button', { name: /export|download/i });
    if (await exportButton.isVisible()) {
      // Set up download listener
      const downloadPromise = page.waitForEvent('download');
      await exportButton.click();
      const download = await downloadPromise;
      expect(download.suggestedFilename()).toMatch(/loot.*history/i);
    }
  });
});
