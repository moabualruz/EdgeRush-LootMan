import { test, expect, mockResponses } from './fixtures';

/**
 * Authentication E2E Tests
 *
 * Tests login flow, session management, and auth guards.
 */

test.describe('Authentication', () => {
  test.describe('Login Page', () => {
    test('should display login form', async ({ page }) => {
      await page.goto('/login');

      await expect(page.getByRole('heading', { name: /login/i })).toBeVisible();
      await expect(page.getByLabel(/username/i)).toBeVisible();
      await expect(page.getByLabel(/password/i)).toBeVisible();
      await expect(page.getByRole('button', { name: /sign in/i })).toBeVisible();
    });

    test('should show validation errors for empty form', async ({ page }) => {
      await page.goto('/login');

      await page.getByRole('button', { name: /sign in/i }).click();

      await expect(page.getByText(/required/i)).toBeVisible();
    });

    test('should show error for invalid credentials', async ({ page }) => {
      await page.route('**/api/auth/login', async (route) => {
        await route.fulfill({
          status: 401,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Invalid credentials' }),
        });
      });

      await page.goto('/login');
      await page.getByLabel(/username/i).fill('wronguser');
      await page.getByLabel(/password/i).fill('wrongpass');
      await page.getByRole('button', { name: /sign in/i }).click();

      await expect(page.getByText(/invalid/i)).toBeVisible();
    });

    test('should redirect to dashboard on successful login', async ({ page }) => {
      await page.route('**/api/auth/login', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            token: 'test-jwt-token',
            user: {
              id: 'user-1',
              username: 'testuser',
              guildId: 'test-guild',
              roles: ['MEMBER'],
            },
          }),
        });
      });

      await page.goto('/login');
      await page.getByLabel(/username/i).fill('testuser');
      await page.getByLabel(/password/i).fill('testpass123');
      await page.getByRole('button', { name: /sign in/i }).click();

      await expect(page).toHaveURL(/dashboard/);
    });

    test('should redirect authenticated users away from login', async ({ page }) => {
      // Set up authenticated state
      await page.addInitScript(() => {
        localStorage.setItem('auth_token', 'test-jwt-token');
        localStorage.setItem('guild_id', 'test-guild');
      });

      await page.goto('/login');

      await expect(page).toHaveURL(/dashboard/);
    });
  });

  test.describe('Auth Guards', () => {
    test('should redirect unauthenticated users to login', async ({ page }) => {
      await page.goto('/dashboard');

      await expect(page).toHaveURL(/login/);
    });

    test('should allow authenticated users to access protected routes', async ({ page }) => {
      await page.addInitScript(() => {
        localStorage.setItem('auth_token', 'test-jwt-token');
        localStorage.setItem('guild_id', 'test-guild');
      });

      // Mock the necessary API calls
      await page.route('**/api/guilds/*/flps/leaderboard', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockResponses.leaderboard),
        });
      });

      await page.goto('/leaderboard');

      await expect(page).toHaveURL(/leaderboard/);
    });

    test('should redirect non-admin users from admin routes', async ({ page }) => {
      await page.addInitScript(() => {
        localStorage.setItem('auth_token', 'test-jwt-token');
        localStorage.setItem('guild_id', 'test-guild');
        localStorage.setItem(
          'user',
          JSON.stringify({ roles: ['MEMBER'] })
        );
      });

      await page.goto('/admin');

      await expect(page).toHaveURL(/dashboard/);
    });
  });

  test.describe('Logout', () => {
    test('should clear auth state and redirect to login', async ({ page }) => {
      await page.addInitScript(() => {
        localStorage.setItem('auth_token', 'test-jwt-token');
        localStorage.setItem('guild_id', 'test-guild');
      });

      await page.goto('/dashboard');

      // Click logout button (adjust selector based on actual UI)
      await page.getByRole('button', { name: /logout|sign out/i }).click();

      await expect(page).toHaveURL(/login/);

      // Verify storage is cleared
      const token = await page.evaluate(() => localStorage.getItem('auth_token'));
      expect(token).toBeNull();
    });
  });
});
