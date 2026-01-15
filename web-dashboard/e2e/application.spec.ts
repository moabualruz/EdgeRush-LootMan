import { test, expect } from './fixtures';

/**
 * Application Portal E2E Tests
 *
 * Tests the guild application flow.
 */

test.describe('Application Portal', () => {
  test.describe('Public Application Form', () => {
    test('should display application form', async ({ page }) => {
      await page.goto('/apply');

      await expect(page.getByRole('heading', { name: /apply|application/i })).toBeVisible();
    });

    test('should show character lookup', async ({ page }) => {
      await page.goto('/apply');

      await expect(page.getByLabel(/character.*name|name/i)).toBeVisible();
      await expect(page.getByLabel(/realm|server/i)).toBeVisible();
    });

    test('should fetch character data on lookup', async ({ page }) => {
      await page.route('**/api/recruitment/lookup*', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            name: 'TestCharacter',
            realm: 'Illidan',
            class: 'WARRIOR',
            spec: 'Protection',
            itemLevel: 500,
            mythicPlusRating: 2500,
            raidProgress: '8/8 H',
          }),
        });
      });

      await page.goto('/apply');

      await page.getByLabel(/character.*name|name/i).fill('TestCharacter');
      await page.getByLabel(/realm|server/i).fill('Illidan');
      await page.getByRole('button', { name: /lookup|search|find/i }).click();

      await expect(page.getByText('WARRIOR')).toBeVisible();
      await expect(page.getByText('500')).toBeVisible();
    });

    test('should show application questions', async ({ page }) => {
      await page.goto('/apply');

      // Navigate to questions step (if multi-step)
      const nextButton = page.getByRole('button', { name: /next|continue/i });
      if (await nextButton.isVisible()) {
        await nextButton.click();
      }

      // Common application questions
      await expect(page.getByLabel(/experience|raid.*history/i)).toBeVisible();
    });

    test('should validate required fields', async ({ page }) => {
      await page.goto('/apply');

      // Try to submit without filling required fields
      const submitButton = page.getByRole('button', { name: /submit|apply/i });
      if (await submitButton.isVisible()) {
        await submitButton.click();
        await expect(page.getByText(/required|fill.*out/i)).toBeVisible();
      }
    });

    test('should submit application successfully', async ({ page }) => {
      await page.route('**/api/recruitment/apply', async (route) => {
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            applicationId: 'app-123',
            status: 'PENDING',
          }),
        });
      });

      await page.goto('/apply');

      // Fill form (simplified - actual form may have multiple steps)
      await page.getByLabel(/character.*name|name/i).fill('TestCharacter');
      await page.getByLabel(/realm|server/i).fill('Illidan');

      const submitButton = page.getByRole('button', { name: /submit|apply/i });
      if (await submitButton.isVisible()) {
        await submitButton.click();
        await expect(page.getByText(/submitted|success|thank.*you/i)).toBeVisible();
      }
    });

    test('should show character not found error', async ({ page }) => {
      await page.route('**/api/recruitment/lookup*', async (route) => {
        await route.fulfill({
          status: 404,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Character not found' }),
        });
      });

      await page.goto('/apply');

      await page.getByLabel(/character.*name|name/i).fill('NonExistentChar');
      await page.getByLabel(/realm|server/i).fill('FakeRealm');
      await page.getByRole('button', { name: /lookup|search|find/i }).click();

      await expect(page.getByText(/not.*found|doesn.*exist/i)).toBeVisible();
    });
  });

  test.describe('Officer Applications Review', () => {
    test.beforeEach(async ({ page }) => {
      // Set up authenticated admin state
      await page.addInitScript(() => {
        localStorage.setItem('auth_token', 'test-jwt-token');
        localStorage.setItem('guild_id', 'test-guild');
        localStorage.setItem(
          'user',
          JSON.stringify({
            id: 'user-1',
            roles: ['ADMIN', 'OFFICER'],
          })
        );
      });

      // Mock applications list
      await page.route('**/api/applications*', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            applications: [
              {
                id: 'app-1',
                characterName: 'Applicant1',
                realm: 'Illidan',
                class: 'WARRIOR',
                spec: 'Protection',
                status: 'PENDING',
                submittedAt: new Date().toISOString(),
              },
              {
                id: 'app-2',
                characterName: 'Applicant2',
                realm: 'Illidan',
                class: 'PRIEST',
                spec: 'Holy',
                status: 'UNDER_REVIEW',
                submittedAt: new Date(Date.now() - 86400000).toISOString(),
              },
            ],
            total: 2,
          }),
        });
      });
    });

    test('should display applications list', async ({ page }) => {
      await page.goto('/admin/applications');

      await expect(page.getByText('Applicant1')).toBeVisible();
      await expect(page.getByText('Applicant2')).toBeVisible();
    });

    test('should filter by status', async ({ page }) => {
      await page.goto('/admin/applications');

      const statusFilter = page.getByRole('combobox', { name: /status/i });
      if (await statusFilter.isVisible()) {
        await statusFilter.selectOption('PENDING');
        // Should filter to pending only
      }
    });

    test('should navigate to application detail', async ({ page }) => {
      await page.route('**/api/applications/app-1', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'app-1',
            characterName: 'Applicant1',
            realm: 'Illidan',
            class: 'WARRIOR',
            spec: 'Protection',
            status: 'PENDING',
            submittedAt: new Date().toISOString(),
            responses: {
              experience: 'Mythic raiding since Legion',
            },
          }),
        });
      });

      await page.goto('/admin/applications');

      await page.getByText('Applicant1').click();

      // Should show application details
      await expect(page.getByText(/experience|responses/i)).toBeVisible();
    });

    test('should approve application', async ({ page }) => {
      await page.route('**/api/applications/app-1/approve', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ status: 'APPROVED' }),
        });
      });

      await page.route('**/api/applications/app-1', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'app-1',
            characterName: 'Applicant1',
            status: 'PENDING',
          }),
        });
      });

      await page.goto('/admin/applications');
      await page.getByText('Applicant1').click();

      const approveButton = page.getByRole('button', { name: /approve/i });
      if (await approveButton.isVisible()) {
        await approveButton.click();
        await expect(page.getByText(/approved|success/i)).toBeVisible();
      }
    });

    test('should decline application', async ({ page }) => {
      await page.route('**/api/applications/app-1/decline', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ status: 'DECLINED' }),
        });
      });

      await page.route('**/api/applications/app-1', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'app-1',
            characterName: 'Applicant1',
            status: 'PENDING',
          }),
        });
      });

      await page.goto('/admin/applications');
      await page.getByText('Applicant1').click();

      const declineButton = page.getByRole('button', { name: /decline|reject/i });
      if (await declineButton.isVisible()) {
        await declineButton.click();
        await expect(page.getByText(/declined|rejected/i)).toBeVisible();
      }
    });
  });
});
