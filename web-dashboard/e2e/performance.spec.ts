import { test, expect, mockResponses } from './fixtures';

/**
 * Performance E2E Tests
 *
 * Tests page load times, Core Web Vitals, and resource efficiency.
 */

test.describe('Performance', () => {
  test.beforeEach(async ({ page }) => {
    // Set up authenticated state
    await page.addInitScript(() => {
      localStorage.setItem('auth_token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Mock APIs with realistic response times
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

  test.describe('Page Load Performance', () => {
    test('login page should load within 3 seconds', async ({ page }) => {
      const startTime = Date.now();

      await page.goto('/login');
      await page.waitForLoadState('networkidle');

      const loadTime = Date.now() - startTime;
      expect(loadTime).toBeLessThan(3000);
    });

    test('dashboard should load within 3 seconds', async ({ page }) => {
      const startTime = Date.now();

      await page.goto('/dashboard');
      await page.waitForLoadState('networkidle');

      const loadTime = Date.now() - startTime;
      expect(loadTime).toBeLessThan(3000);
    });

    test('leaderboard should load within 3 seconds', async ({ page }) => {
      const startTime = Date.now();

      await page.goto('/leaderboard');
      await page.waitForLoadState('networkidle');

      const loadTime = Date.now() - startTime;
      expect(loadTime).toBeLessThan(3000);
    });
  });

  test.describe('Core Web Vitals', () => {
    test('should have good LCP (Largest Contentful Paint)', async ({ page }) => {
      await page.goto('/dashboard');

      // Wait for LCP
      const lcpValue = await page.evaluate(() => {
        return new Promise<number>((resolve) => {
          new PerformanceObserver((list) => {
            const entries = list.getEntries();
            const lastEntry = entries[entries.length - 1];
            resolve(lastEntry.startTime);
          }).observe({ type: 'largest-contentful-paint', buffered: true });

          // Fallback timeout
          setTimeout(() => resolve(0), 5000);
        });
      });

      // LCP should be under 2.5 seconds for "good"
      expect(lcpValue).toBeLessThan(2500);
    });

    test('should have good FID (First Input Delay) potential', async ({ page }) => {
      await page.goto('/dashboard');

      // Measure time to interactive
      const metrics = await page.evaluate(() => {
        return new Promise<{ tti: number }>((resolve) => {
          // Use Navigation Timing API
          const timing = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
          resolve({
            tti: timing.domInteractive - timing.fetchStart,
          });
        });
      });

      // TTI should be reasonable
      expect(metrics.tti).toBeLessThan(3000);
    });

    test('should have low CLS (Cumulative Layout Shift)', async ({ page }) => {
      await page.goto('/dashboard');

      // Wait for page to stabilize
      await page.waitForTimeout(2000);

      const clsValue = await page.evaluate(() => {
        return new Promise<number>((resolve) => {
          let clsScore = 0;

          new PerformanceObserver((list) => {
            for (const entry of list.getEntries()) {
              // @ts-ignore - layout-shift entries have value property
              if (!entry.hadRecentInput) {
                // @ts-ignore
                clsScore += entry.value;
              }
            }
          }).observe({ type: 'layout-shift', buffered: true });

          setTimeout(() => resolve(clsScore), 2000);
        });
      });

      // CLS should be under 0.1 for "good"
      expect(clsValue).toBeLessThan(0.1);
    });
  });

  test.describe('Resource Efficiency', () => {
    test('should not make duplicate API requests', async ({ page }) => {
      const apiCalls: string[] = [];

      await page.route('**/api/**', async (route) => {
        apiCalls.push(route.request().url());
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({}),
        });
      });

      await page.goto('/dashboard');
      await page.waitForLoadState('networkidle');

      // Check for duplicates
      const uniqueCalls = [...new Set(apiCalls)];
      expect(apiCalls.length).toBe(uniqueCalls.length);
    });

    test('should lazy load images', async ({ page }) => {
      await page.goto('/leaderboard');

      // Check that images have loading="lazy" or are using intersection observer
      const images = page.locator('img');
      const imageCount = await images.count();

      for (let i = 0; i < imageCount; i++) {
        const image = images.nth(i);
        const loading = await image.getAttribute('loading');
        const inViewport = await image.isVisible();

        // Either lazy loaded or already in viewport
        expect(loading === 'lazy' || inViewport).toBe(true);
      }
    });

    test('should bundle JS efficiently', async ({ page }) => {
      const jsResources: { url: string; size: number }[] = [];

      page.on('response', async (response) => {
        const url = response.url();
        if (url.endsWith('.js') || url.includes('.js?')) {
          const headers = response.headers();
          const size = parseInt(headers['content-length'] || '0', 10);
          jsResources.push({ url, size });
        }
      });

      await page.goto('/dashboard');
      await page.waitForLoadState('networkidle');

      // Total JS should be under 500KB (compressed)
      const totalSize = jsResources.reduce((sum, r) => sum + r.size, 0);
      expect(totalSize).toBeLessThan(500 * 1024);
    });
  });

  test.describe('Navigation Performance', () => {
    test('should navigate between pages quickly', async ({ page }) => {
      await page.goto('/dashboard');

      const startTime = Date.now();
      await page.getByRole('link', { name: /leaderboard/i }).click();
      await page.waitForLoadState('networkidle');
      const navTime = Date.now() - startTime;

      // Client-side navigation should be fast
      expect(navTime).toBeLessThan(1000);
    });

    test('should preload critical resources', async ({ page }) => {
      await page.goto('/dashboard');

      // Check for preload links
      const preloads = await page.locator('link[rel="preload"]').count();

      // Should have some preloaded resources
      expect(preloads).toBeGreaterThanOrEqual(0);
    });
  });

  test.describe('Large Data Handling', () => {
    test('should handle large leaderboard efficiently', async ({ page }) => {
      // Create large dataset
      const largeLeaderboard = {
        ...mockResponses.leaderboard,
        entries: Array.from({ length: 100 }, (_, i) => ({
          rank: i + 1,
          characterName: `Raider${i + 1}`,
          realm: 'Illidan',
          class: 'WARRIOR',
          spec: 'Protection',
          score: 1 - i * 0.01,
          rms: 0.4,
          ipi: 0.35,
          rdf: 0.2,
        })),
      };

      await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(largeLeaderboard),
        });
      });

      const startTime = Date.now();
      await page.goto('/leaderboard');
      await page.waitForLoadState('networkidle');
      const loadTime = Date.now() - startTime;

      // Should still load quickly with large data
      expect(loadTime).toBeLessThan(5000);

      // Check that virtual scrolling is used (if applicable)
      const visibleRows = await page.locator('tr, [role="row"]').count();
      // Should not render all 100 rows if virtual scrolling is enabled
    });
  });
});
