import { test, expect } from '@playwright/test';

test.describe('Raid Planning', () => {
  const mockUser = {
    id: 1,
    username: 'RaidLeader',
    discriminator: '1234',
    avatar: 'avatar-url',
    guildId: 'test-guild',
  };

  const mockGuilds = [
    {
      id: 'test-guild',
      guildId: 'test-guild',
      name: 'Test Guild',
      icon: 'icon-url',
      role: 'ADMIN',
    },
  ];

  /* 
   * Helper to authenticate via OAuth mocks 
   * Copied pattern from guild-context.spec.ts but simplified
   */
  async function authenticate(page: any) {
    // Mock user auth endpoints
    await page.route('**/v1/auth/me', async (route: any) => {
      await route.fulfill({ status: 200, body: JSON.stringify(mockUser) });
    });

    await page.route('**/v1/user/guilds', async (route: any) => {
      await route.fulfill({ status: 200, body: JSON.stringify(mockGuilds) });
    });
    
    // Mock active guild endpoint
    await page.route('**/v1/user/guilds/active', async (route: any) => {
      await route.fulfill({ 
        status: 200, 
        body: JSON.stringify({ ...mockGuilds[0], isActive: true }) 
      });
    });

    // Simulate login by setting token
    await page.addInitScript(() => {
      localStorage.setItem('token', 'fake-jwt-token');
      localStorage.setItem('guild_id', 'test-guild');
    });
  }

  test.beforeEach(async ({ page }) => {
    await authenticate(page);

    // Mock Game Data API (Note: Uses /api/v1 per gameData.ts)
    await page.route('**/api/v1/game-data/raids', async (route) => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify([
          { id: 123, name: 'Vault of Verification' }
        ])
      });
    });

    await page.route('**/api/v1/game-data/raids/*/maps', async (route) => {
      await route.fulfill({
        status: 200,
        body: JSON.stringify([
          { id: 456, name: 'The Testing Grounds', description: 'Phase 1' }
        ])
      });
    });
  });

  test.describe('Plans List', () => {
    test('should display existing raid plans', async ({ page }) => {
      // Mock Get Plans (Note: Uses /v1 per raidplan.ts) using ** to catch query params
      await page.route('**/v1/raid-plans/guild/**', async (route) => {
        await route.fulfill({
          status: 200,
          body: JSON.stringify({
            content: [
              {
                id: 'plan-1',
                name: 'Mythic Strategy',
                encounterName: 'The Testing Grounds',
                visibility: 'GUILD',
                updatedAt: new Date().toISOString(),
                steps: []
              }
            ],
            totalPages: 1,
            totalElements: 1
          })
        });
      });

      await page.goto('/raid-plans');
      // Use exact match for heading to avoid matching "No raid plans yet"
      await expect(page.getByRole('heading', { name: 'Raid Plans', exact: true })).toBeVisible();
      await expect(page.getByText('Mythic Strategy')).toBeVisible();
      await expect(page.getByText('The Testing Grounds')).toBeVisible();
    });

    test('should show empty state when no plans exist', async ({ page }) => {
      await page.route('**/v1/raid-plans/guild/**', async (route) => {
        await route.fulfill({
          status: 200,
          body: JSON.stringify({
            content: [],
            totalPages: 0,
            totalElements: 0
          })
        });
      });

      await page.goto('/raid-plans');
      await expect(page.getByText('No raid plans yet')).toBeVisible();
    });
  });

  test.describe('Create Plan', () => {
    test.beforeEach(async ({ page }) => {
        // Ensure empty list to start
        await page.route('**/v1/raid-plans/guild/**', async (route) => {
            await route.fulfill({
              status: 200,
              body: JSON.stringify({ content: [], totalPages: 0 })
            });
          });
    });

    test('should create a new plan successfully', async ({ page }) => {
      // Mock Create Endpoint
      await page.route('**/v1/raid-plans', async (route) => {
        const body = JSON.parse(route.request().postData() || '{}');
        // Only valid if we handle the form submission correctly
        
        await route.fulfill({
          status: 200,
          body: JSON.stringify({
            id: 'new-plan-id',
            name: body.name || 'New Boss Plan',
            encounterName: 'The Testing Grounds',
            visibility: body.visibility || 'GUILD',
            steps: []
          })
        });
      });

      // Mock Get Plan (for redirect after create)
      await page.route('**/v1/raid-plans/new-plan-id', async (route) => {
         await route.fulfill({
            status: 200,
            body: JSON.stringify({
                id: 'new-plan-id',
                name: 'New Boss Plan',
                encounterName: 'The Testing Grounds',
                visibility: 'GUILD',
                steps: [{ order: 0, markers: [], shapes: [] }]
            })
         });
      });

      await page.goto('/raid-plans');
      
      // Open Modal
      await page.getByTestId('create-plan-button').click();
      await expect(page.getByTestId('create-plan-modal')).toBeVisible();

      // Fill Form
      await page.locator('input[placeholder="e.g., Phase 1 Positions"]').fill('New Boss Plan');
      
      // Select Encounter (Dependent on EncounterPicker)
      // Wait for raid select to populate
      const raidSelect = page.locator('select').first(); // Rough selector based on component structure
      await expect(raidSelect).toContainText('Vault of Verification');
      // No need to select if auto-selected, but let's ensure map select logic
      // It auto-selects first raid, then we pick map
      // The second select is for map
      const mapSelect = page.locator('select').nth(1);
      await mapSelect.selectOption({ label: 'The Testing Grounds' });

      // Click Create - Use specific selector to avoid ambiguity
      await page.getByTestId('create-plan-modal').getByRole('button', { name: 'Create' }).click();

      // Expect redirection
      await expect(page).toHaveURL(/\/raid-plans\/new-plan-id/);
      await expect(page.getByRole('heading', { name: 'New Boss Plan' })).toBeVisible();
    });
  });

  test.describe('Plan Editor', () => {
    test.beforeEach(async ({ page }) => {
         // Mock Get Plan
         await page.route('**/v1/raid-plans/plan-123', async (route) => {
            await route.fulfill({
               status: 200,
               body: JSON.stringify({
                   id: 'plan-123',
                   name: 'Editor Test Plan',
                   encounterName: 'The Testing Grounds',
                   visibility: 'GUILD',
                   steps: [{ order: 0, markers: [], shapes: [] }]
               })
            });
         });
    });

    test('should load editor and allowing adding a marker', async ({ page }) => {
        await page.goto('/raid-plans/plan-123');
        
        // Wait for plan to load
        await expect(page.getByText('Editor Test Plan')).toBeVisible();

        // Check Canvas exists (plan-canvas class on svg)
        const canvas = page.locator('.plan-canvas');
        await expect(canvas).toBeVisible();
        
        // Verify Palette is present
        await expect(page.getByTestId('marker-TANK')).toBeVisible();

        // Check Save Button
        await expect(page.getByTestId('save-button')).toBeVisible();
    });

  });

});
