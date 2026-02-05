import { test, expect, mockResponses } from './fixtures';

/**
 * Authentication E2E Tests
 *
 * Tests login flow, session management, and auth guards.
 */

test.describe('Authentication', () => {
  test.describe('Login Page', () => {
    test('should display login form', async ({ page }) => {
      page.on('console', msg => console.log(`BROWSER CONSOLE: ${msg.text()}`));
      await page.goto('/login');

      await expect(page.getByRole('heading', { name: /welcome back/i })).toBeVisible();
      await expect(page.getByLabel(/identity/i)).toBeVisible();
      await expect(page.getByLabel(/password/i)).toBeVisible();
      await expect(page.getByRole('button', { name: /sign in/i })).toBeVisible();
    });

    test('should disable sign in button for empty form', async ({ page }) => {
      await page.goto('/login');
      const submitButton = page.getByRole('button', { name: /sign in/i });
      await expect(submitButton).toBeDisabled();
    });

    test('should show error for invalid credentials', async ({ page }) => {
      await page.route('**/v1/auth/login', async (route) => {
        await route.fulfill({
          status: 401,
          contentType: 'application/json',
          body: JSON.stringify({ message: 'Invalid credentials' }), // Axios error usually message
        });
      });

      await page.goto('/login');
      await page.getByLabel(/identity/i).fill('wronguser');
      await page.getByLabel(/password/i).fill('wrongpass');
      await page.getByRole('button', { name: /sign in/i }).click();

      await expect(page.getByText(/invalid|failed/i)).toBeVisible();
    });

    test('should redirect to dashboard on successful login', async ({ page }) => {
      await page.route('**/v1/auth/login', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            accessToken: 'test-jwt-token',
            refreshToken: 'refresh-token',
            expiresIn: 3600
          }),
        });
      });

      // Also mock /me call for after login
      await page.route('**/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 200,
          body: JSON.stringify({
            id: 'user-1',
            username: 'testuser',
            role: 'MEMBER'
          })
        });
      });

      // Mock guilds call to prevent error
      await page.route('**/api/guilds/', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
      });

      await page.goto('/login');
      await page.getByLabel(/identity/i).fill('testuser');
      await page.getByLabel(/password/i).fill('testpass123');
      await page.getByRole('button', { name: /sign in/i }).click();

      await expect(page).toHaveURL(/dashboard/);
    });

    test('should redirect authenticated users away from login', async ({ page }) => {
      // Mock the user check
      await page.route('**/api/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'user-1',
            username: 'testuser',
            guildId: 'test-guild',
            role: 'MEMBER'
          }),
        });
      });

      // Set up authenticated state with correct keys
      await page.addInitScript(() => {
        localStorage.setItem('token', 'test-jwt-token');
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
        localStorage.setItem('token', 'test-jwt-token');
        localStorage.setItem('guild_id', 'test-guild');
      });

      // Mock the user check 
      await page.route('**/api/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 200,
          body: JSON.stringify({
            id: 'user-1', 
            username: 'testuser',
            guildId: 'test-guild',
            role: 'MEMBER'
          })
        });
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
        localStorage.setItem('token', 'test-jwt-token');
        localStorage.setItem('guild_id', 'test-guild');
      });

      // Mock user as non-admin
      await page.route('**/api/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 200,
          body: JSON.stringify({
            id: 'user-1',
            username: 'testuser',
            role: 'MEMBER' // Non-admin
          })
        });
      });

      await page.goto('/admin');

      await expect(page).toHaveURL(/dashboard/);
    });
  });

  test.describe('Logout', () => {
    test('should clear auth state and redirect to login', async ({ page }) => {
      await page.addInitScript(() => {
        localStorage.setItem('token', 'test-jwt-token');
        localStorage.setItem('guild_id', 'test-guild');
      });

      // Mock user for dashboard access
      await page.route('**/api/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 200,
          body: JSON.stringify({
            id: 'user-1',
            username: 'testuser',
            role: 'MEMBER'
          })
        });
      });

      // Mock guilds
      await page.route('**/api/guilds/', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
      });

      await page.goto('/dashboard');

      // Click logout button (adjust selector based on actual UI)
      await page.getByRole('button', { name: /logout|sign out/i }).click();

      await expect(page).toHaveURL(/login/);

      // Verify storage is cleared
      const token = await page.evaluate(() => localStorage.getItem('token'));
      expect(token).toBeNull();
    });
  });
});
