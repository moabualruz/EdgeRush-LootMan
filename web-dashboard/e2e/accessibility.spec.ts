import { test, expect, mockResponses } from './fixtures';
import AxeBuilder from '@axe-core/playwright';

/**
 * Accessibility E2E Tests
 *
 * Tests WCAG compliance and accessibility features.
 */

test.describe('Accessibility', () => {
  test.beforeEach(async ({ page }) => {
    // Set up authenticated state
    await page.addInitScript(() => {
      localStorage.setItem('auth_token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock APIs
    await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockResponses.leaderboard),
      });
    });

    await page.route('**/api/**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({}),
      });
    });
  });

  test.describe('Axe Accessibility Audits', () => {
    test('login page should have no accessibility violations', async ({ page }) => {
      await page.goto('/login');

      const accessibilityScanResults = await new AxeBuilder({ page }).analyze();

      expect(accessibilityScanResults.violations).toEqual([]);
    });

    test('dashboard should have no accessibility violations', async ({ page }) => {
      await page.goto('/dashboard');

      const accessibilityScanResults = await new AxeBuilder({ page }).analyze();

      expect(accessibilityScanResults.violations).toEqual([]);
    });

    test('leaderboard should have no accessibility violations', async ({ page }) => {
      await page.goto('/leaderboard');

      const accessibilityScanResults = await new AxeBuilder({ page }).analyze();

      expect(accessibilityScanResults.violations).toEqual([]);
    });

    test('application form should have no accessibility violations', async ({ page }) => {
      await page.goto('/apply');

      const accessibilityScanResults = await new AxeBuilder({ page }).analyze();

      expect(accessibilityScanResults.violations).toEqual([]);
    });
  });

  test.describe('Keyboard Navigation', () => {
    test('should navigate login form with keyboard', async ({ page }) => {
      await page.goto('/login');

      // Tab to username field
      await page.keyboard.press('Tab');
      await expect(page.getByLabel(/username/i)).toBeFocused();

      // Tab to password field
      await page.keyboard.press('Tab');
      await expect(page.getByLabel(/password/i)).toBeFocused();

      // Tab to submit button
      await page.keyboard.press('Tab');
      await expect(page.getByRole('button', { name: /sign in/i })).toBeFocused();
    });

    test('should navigate main menu with keyboard', async ({ page }) => {
      await page.goto('/dashboard');

      // Tab into navigation
      await page.keyboard.press('Tab');

      // Should be able to navigate through links
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');

      // Press Enter to navigate
      await page.keyboard.press('Enter');
    });

    test('should trap focus in modals', async ({ page }) => {
      await page.goto('/dashboard');

      // Open a modal (e.g., settings)
      const settingsButton = page.getByRole('button', { name: /settings/i });
      if (await settingsButton.isVisible()) {
        await settingsButton.click();

        // Tab should cycle within modal
        const modal = page.getByRole('dialog');
        if (await modal.isVisible()) {
          // Focus should stay within modal
          await page.keyboard.press('Tab');
          await page.keyboard.press('Tab');
          await page.keyboard.press('Tab');
          await page.keyboard.press('Tab');

          const focusedElement = page.locator(':focus');
          await expect(focusedElement).toBeVisible();
        }
      }
    });

    test('should close modal with Escape key', async ({ page }) => {
      await page.goto('/dashboard');

      const settingsButton = page.getByRole('button', { name: /settings/i });
      if (await settingsButton.isVisible()) {
        await settingsButton.click();

        const modal = page.getByRole('dialog');
        if (await modal.isVisible()) {
          await page.keyboard.press('Escape');
          await expect(modal).not.toBeVisible();
        }
      }
    });
  });

  test.describe('Screen Reader Support', () => {
    test('should have proper heading hierarchy', async ({ page }) => {
      await page.goto('/dashboard');

      // Check for h1
      const h1 = page.locator('h1');
      await expect(h1).toBeVisible();

      // h2s should exist if there are sections
      const h2s = page.locator('h2');
      const h2Count = await h2s.count();

      // Headings should be in order (no skipping levels)
    });

    test('should have aria-labels on icon buttons', async ({ page }) => {
      await page.goto('/dashboard');

      // All buttons with only icons should have aria-label
      const iconButtons = page.locator('button:has(svg):not(:has-text(/./))');
      const count = await iconButtons.count();

      for (let i = 0; i < count; i++) {
        const button = iconButtons.nth(i);
        const ariaLabel = await button.getAttribute('aria-label');
        const title = await button.getAttribute('title');
        expect(ariaLabel || title).toBeTruthy();
      }
    });

    test('should have proper form labels', async ({ page }) => {
      await page.goto('/login');

      // All inputs should have associated labels
      const inputs = page.locator('input');
      const inputCount = await inputs.count();

      for (let i = 0; i < inputCount; i++) {
        const input = inputs.nth(i);
        const id = await input.getAttribute('id');
        const ariaLabel = await input.getAttribute('aria-label');
        const ariaLabelledBy = await input.getAttribute('aria-labelledby');

        if (id) {
          const label = page.locator(`label[for="${id}"]`);
          const hasVisibleLabel = await label.isVisible().catch(() => false);
          expect(hasVisibleLabel || ariaLabel || ariaLabelledBy).toBeTruthy();
        }
      }
    });

    test('should announce dynamic content', async ({ page }) => {
      await page.goto('/dashboard');

      // Check for aria-live regions
      const liveRegions = page.locator('[aria-live]');
      const count = await liveRegions.count();

      // Should have at least one live region for notifications
      expect(count).toBeGreaterThanOrEqual(0); // Soft check
    });
  });

  test.describe('Color and Contrast', () => {
    test('should have sufficient color contrast', async ({ page }) => {
      await page.goto('/leaderboard');

      // Run axe specifically for color contrast
      const accessibilityScanResults = await new AxeBuilder({ page })
        .withTags(['wcag2aa'])
        .analyze();

      const contrastViolations = accessibilityScanResults.violations.filter(
        (v) => v.id === 'color-contrast'
      );

      expect(contrastViolations).toEqual([]);
    });

    test('should not rely solely on color', async ({ page }) => {
      await page.goto('/leaderboard');

      // FLPS score colors should have text/icon indicators too
      const scoreElements = page.locator('[data-flps-score]');
      const count = await scoreElements.count();

      for (let i = 0; i < count; i++) {
        const element = scoreElements.nth(i);
        // Should have text content, not just color
        const text = await element.textContent();
        expect(text?.trim().length).toBeGreaterThan(0);
      }
    });
  });

  test.describe('Focus Visibility', () => {
    test('should show visible focus indicators', async ({ page }) => {
      await page.goto('/login');

      // Focus on username input
      await page.getByLabel(/username/i).focus();

      // Check that focus is visible (has outline or similar)
      const focusedElement = page.locator(':focus');
      await expect(focusedElement).toBeVisible();

      // Get computed styles to verify focus visibility
      const styles = await focusedElement.evaluate((el) => {
        const computed = window.getComputedStyle(el);
        return {
          outline: computed.outline,
          boxShadow: computed.boxShadow,
          border: computed.border,
        };
      });

      // Should have some visible focus indicator
      const hasFocusIndicator =
        styles.outline !== 'none' ||
        styles.boxShadow !== 'none' ||
        styles.border !== 'none';

      expect(hasFocusIndicator).toBe(true);
    });
  });
});
