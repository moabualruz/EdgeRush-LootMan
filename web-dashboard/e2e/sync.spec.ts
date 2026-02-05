import { test, expect } from './fixtures';

/**
 * Sync Operations E2E Tests
 *
 * Tests sync history viewing, filtering, manual triggering, and log viewing.
 */

test.describe('Sync Operations', () => {
  test.beforeEach(async ({ page }) => {
    // Set up authenticated state
    await page.addInitScript(() => {
      localStorage.setItem('token', 'test-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });

    // Debug requests
    page.on('request', request => console.log('>>', request.method(), request.url()));
    page.on('requestfailed', request => console.log('>> FAILED', request.method(), request.url(), request.failure()?.errorText));

    // Mock auth user
    await page.route('**/v1/auth/me', async (route) => {
       await route.fulfill({ status: 200, body: JSON.stringify({ id: 'user-1', guildId: 'test-guild', role: 'ADMIN' }) });
    });

    // Mock guild context
    await page.route('**/v1/user/guilds*', async (route) => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify([{ id: 'test-guild', isActive: true, permissions: ['ADMIN'] }])
      });
    });

    // Mock characters (prevent networkidle hang)
    await page.route('**/v1/user/characters*', async (route) => {
        await route.fulfill({
            status: 200,
            body: JSON.stringify([])
        });
    });

    // Mock Sync History (Default)
    await page.route('**/api/sync-runs*', async (route) => {
      // Avoid matching sub-paths if strict, but 'sync-runs*' covers 'sync-runs/source' too?
      // Wait, '**/api/sync-runs*' matches '.../sync-runs/source/...'?
      // Yes. If I have specific mocks for source/status, I need proper ordering or exclusion.
      // Playwright uses "first matching route".
      // So detailed mocks must be defined BEFORE generic ones? Or verify request url?
      // Or make this generic mock NOT match subpaths.
      // Axios params: ?page=...
      
      const url = route.request().url();
      if (url.includes('/source/') || url.includes('/status/') || url.includes('/logs')) {
         return route.continue();
      }

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              id: 101,
              source: 'WoWAudit',
              status: 'COMPLETED',
              startedAt: new Date(Date.now() - 3600000).toISOString(),
              completedAt: new Date(Date.now() - 3595000).toISOString(),
              message: 'Sync completed successfully'
            },
            {
              id: 102,
              source: 'WarcraftLogs',
              status: 'FAILED',
              startedAt: new Date(Date.now() - 7200000).toISOString(),
              completedAt: new Date(Date.now() - 7199000).toISOString(),
              message: 'API Rate Limit'
            }
          ],
          page: 0,
          size: 20,
          totalElements: 2,
          totalPages: 1
        })
      });
    });
  });

  test('should display sync history', async ({ page }) => {
    await page.goto('/admin/sync');
    console.log('>> Visited /admin/sync');

    // Debugging: Check potential states
    if (await page.locator('.animate-spin').count() > 0) console.log('>> Page is still loading');
    if (await page.locator('.alert-error').count() > 0) {
        console.log('>> Page shows error alert');
        console.log(await page.locator('.alert-error').textContent());
    }

    try {
        await expect(page.getByRole('heading', { name: /sync history/i })).toBeVisible({ timeout: 2000 });
        
        // Scope to the list container to avoid filter buttons
        const list = page.locator('.space-y-4'); 
        // Note: Tailwind class 'space-y-4' is used for the list container in SyncHistoryPage.vue
        
        await expect(list.getByText('WoWAudit')).toBeVisible({ timeout: 2000 });
        // 'WarcraftLogs' might appear in filter too
        await expect(list.getByText('WarcraftLogs')).toBeVisible({ timeout: 2000 });
        
        await expect(list.getByText('COMPLETED', { exact: true })).toBeVisible({ timeout: 2000 });
        await expect(list.getByText('FAILED', { exact: true })).toBeVisible({ timeout: 2000 });
    } catch (e) {
        console.log('>> TEST FAILED. DUMPING BODY TEXT:');
        console.log(await page.locator('body').innerText());
        console.log('>> Error:', e);
        throw e;
    }
  });

  test('should filter by source', async ({ page }) => {
    // Mock filtered response
    await page.route('**/api/sync-runs/source/WoWAudit*', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          content: [
            {
              id: 101,
              source: 'WoWAudit',
              status: 'COMPLETED',
              startedAt: new Date().toISOString(),
              completedAt: new Date().toISOString(),
              message: 'Filtered result'
            }
          ],
          totalElements: 1,
          totalPages: 1
        })
      });
    });

    await page.goto('/admin/sync');
    
    // Click filter button (Scoped to Filter Card)
    // The filter card has unique text "Source:"
    const filterCard = page.locator('.card', { hasText: 'Source:' });
    await filterCard.getByRole('button', { name: 'WoWAudit', exact: true }).click();
    
    // Verify
    await expect(page.getByText('Filtered result')).toBeVisible();
  });

  test('should trigger manual sync', async ({ page }) => {
    // Mock trigger response
    await page.route('**/api/sync/trigger/WoWAudit', async (route) => {
        if (route.request().method() === 'POST') {
            await route.fulfill({
                status: 200,
                body: JSON.stringify({
                    id: 103,
                    source: 'WoWAudit',
                    status: 'RUNNING',
                    startedAt: new Date().toISOString(),
                    message: 'Manual sync started'
                })
            });
        }
    });

    await page.goto('/admin/sync');
    
    // Click Sync WoWAudit button (Manual Trigger)
    // Use regex to be safe or specific locator if needed. Regex /sync wowaudit/i is good.
    await page.getByRole('button', { name: /sync wowaudit/i }).click();

    await expect(page.locator('.alert-error')).not.toBeVisible();
  });

  test('should view sync logs', async ({ page }) => {
    // Mock logs
    await page.route('**/api/sync-runs/101/logs', async (route) => {
        await route.fulfill({
            status: 200,
            body: JSON.stringify([
                { timestamp: new Date().toISOString(), level: 'INFO', message: 'Starting sync...' },
                { timestamp: new Date().toISOString(), level: 'INFO', message: 'Fetching data...' }
            ])
        });
    });

    await page.goto('/admin/sync');
    
    const list = page.locator('.space-y-4');
    // Click on a row to open logs. Use .first() to pick the first row.
    await list.locator('.card').first().click();

    // Check for Modal Header instead of role=dialog
    await expect(page.getByRole('heading', { name: 'Sync Logs' })).toBeVisible();
    await expect(page.getByText('Starting sync...')).toBeVisible();
  });
});
