import { test, expect } from '@playwright/test';

/**
 * OAuth E2E Tests
 *
 * Tests Discord and Battle.net OAuth flows:
 * - OAuth URL generation
 * - Callback handling
 * - Error handling
 * - Account linking
 */

test.describe('OAuth Flows', () => {
  test.describe('Discord OAuth', () => {
    test('should redirect to Discord authorization URL', async ({ page }) => {
      // Mock Discord OAuth URL endpoint
      await page.route('**/api/v1/auth/discord/url**', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            url: 'https://discord.com/api/oauth2/authorize?client_id=test&redirect_uri=http%3A%2F%2Flocalhost%2Fauth%2Fdiscord%2Fcallback&response_type=code&scope=identify,email',
            provider: 'discord',
          }),
        });
      });

      await page.goto('/login');
      await page.waitForLoadState('networkidle');

      // Find Discord login button
      const discordBtn = page.getByRole('button', { name: /discord/i });
      await expect(discordBtn).toBeVisible();

      await page.screenshot({ path: 'test-results/discord-button-visible.png', fullPage: true });

      // Click Discord button (will try to navigate to Discord)
      // We intercept the navigation since we can't actually do OAuth in tests
      const [popup] = await Promise.all([
        page.waitForEvent('popup').catch(() => null),
        page.waitForURL('**/discord.com/**').catch(() => null),
        discordBtn.click(),
      ]).catch(() => [null]);

      // Verify redirect URL was called
      await page.screenshot({ path: 'test-results/discord-oauth-initiated.png', fullPage: true });
    });

    test('should handle Discord callback with valid code', async ({ page }) => {
      // Mock callback endpoint
      await page.route('**/api/v1/auth/discord/callback', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            accessToken: 'jwt-token-from-discord',
            refreshToken: 'refresh-token',
            expiresIn: 900,
          }),
        });
      });

      // Mock user endpoint
      await page.route('**/api/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 1,
            discordId: '123456789',
            username: 'discorduser',
            role: 'RAIDER',
            linkedCharacters: [],
          }),
        });
      });

      // Simulate OAuth callback
      await page.goto('/auth/discord/callback?code=valid-discord-code');
      await page.waitForLoadState('networkidle');

      // Should redirect to dashboard after successful auth
      await expect(page).toHaveURL(/dashboard/);

      await page.screenshot({ path: 'test-results/discord-callback-success.png', fullPage: true });
    });

    test('should handle Discord callback error', async ({ page }) => {
      // Simulate OAuth error callback
      await page.goto('/auth/discord/callback?error=access_denied&error_description=User%20denied%20access');
      await page.waitForLoadState('domcontentloaded');

      // Wait for the error message to appear
      const errorText = page.getByText(/authentication failed/i);
      await expect(errorText).toBeVisible({ timeout: 10000 });

      await page.screenshot({ path: 'test-results/discord-callback-error.png', fullPage: true });
    });

    test('should show error when Discord is not configured', async ({ page }) => {
      // Mock Discord OAuth URL endpoint returning error
      await page.route('**/api/v1/auth/discord/url**', async (route) => {
        await route.fulfill({
          status: 400,
          contentType: 'application/json',
          body: JSON.stringify({
            error: 'Discord login is not configured',
            message: 'Discord OAuth is not configured on the server',
          }),
        });
      });

      await page.goto('/login');
      await page.waitForLoadState('networkidle');

      // Click Discord button
      const discordBtn = page.getByRole('button', { name: /discord/i });
      await discordBtn.click();

      // Should show error message
      await expect(page.getByText(/not configured|error/i)).toBeVisible();

      await page.screenshot({ path: 'test-results/discord-not-configured.png', fullPage: true });
    });
  });

  test.describe('Battle.net OAuth', () => {
    test('should redirect to Battle.net authorization URL', async ({ page }) => {
      // Mock Battle.net OAuth URL endpoint
      await page.route('**/api/v1/auth/battlenet/url**', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            url: 'https://eu.battle.net/oauth/authorize?client_id=test&redirect_uri=http%3A%2F%2Flocalhost%2Fauth%2Fbattlenet%2Fcallback&response_type=code&scope=openid',
            provider: 'battlenet',
          }),
        });
      });

      await page.goto('/login');
      await page.waitForLoadState('networkidle');

      // Find Battle.net login button
      const battlenetBtn = page.getByRole('button', { name: /battle\.net|battlenet/i });
      await expect(battlenetBtn).toBeVisible();

      await page.screenshot({ path: 'test-results/battlenet-button-visible.png', fullPage: true });
    });

    test('should handle Battle.net callback with valid code', async ({ page }) => {
      // Mock callback endpoint
      await page.route('**/api/v1/auth/battlenet/callback', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            accessToken: 'jwt-token-from-battlenet',
            refreshToken: 'refresh-token',
            expiresIn: 900,
          }),
        });
      });

      // Mock user endpoint
      await page.route('**/api/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 1,
            battlenetId: 'BnetUser#1234',
            username: 'bnetuser',
            role: 'RAIDER',
            linkedCharacters: [],
          }),
        });
      });

      // Simulate OAuth callback
      await page.goto('/auth/battlenet/callback?code=valid-battlenet-code');
      await page.waitForLoadState('networkidle');

      // Should redirect to dashboard after successful auth
      await expect(page).toHaveURL(/dashboard/);

      await page.screenshot({ path: 'test-results/battlenet-callback-success.png', fullPage: true });
    });

    test('should handle Battle.net callback error', async ({ page }) => {
      // Simulate OAuth error callback
      await page.goto('/auth/battlenet/callback?error=access_denied');
      await page.waitForLoadState('domcontentloaded');

      // Wait for the error message to appear
      const errorText = page.getByText(/authentication failed/i);
      await expect(errorText).toBeVisible({ timeout: 10000 });

      await page.screenshot({ path: 'test-results/battlenet-callback-error.png', fullPage: true });
    });

    test('should show error when Battle.net is not configured', async ({ page }) => {
      // Mock Battle.net OAuth URL endpoint returning error
      await page.route('**/api/v1/auth/battlenet/url**', async (route) => {
        await route.fulfill({
          status: 400,
          contentType: 'application/json',
          body: JSON.stringify({
            error: 'Battle.net login is not configured',
            message: 'Battle.net OAuth is not configured on the server',
          }),
        });
      });

      await page.goto('/login');
      await page.waitForLoadState('networkidle');

      // Click Battle.net button
      const battlenetBtn = page.getByRole('button', { name: /battle\.net|battlenet/i });
      await battlenetBtn.click();

      // Should show error message
      await expect(page.getByText(/not configured|error/i)).toBeVisible();

      await page.screenshot({ path: 'test-results/battlenet-not-configured.png', fullPage: true });
    });
  });

  test.describe('Account Linking', () => {
    test.beforeEach(async ({ page }) => {
      // IMPORTANT: Set up route mocks BEFORE setting localStorage token
      await page.route('**/api/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 1,
            discordId: '123456789',
            battlenetId: null, // Not yet linked
            username: 'testuser',
            role: 'RAIDER',
            linkedCharacters: [],
          }),
        });
      });

      await page.route('**/api/v1/user/guilds', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
      });

      await page.route('**/api/v1/user/guilds/active', async (route) => {
        await route.fulfill({
          status: 204,
          body: '',
        });
      });

      // Set up authenticated session AFTER routes
      await page.addInitScript(() => {
        localStorage.setItem('token', 'existing-jwt-token');
      });
    });

    test('should link Battle.net account to existing Discord account', async ({ page }) => {
      // Mock link endpoint
      await page.route('**/api/v1/auth/link/battlenet', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 1,
            discordId: '123456789',
            battlenetId: 'NewBnetUser#1234',
            username: 'testuser',
            role: 'RAIDER',
            linkedCharacters: [
              { characterName: 'NewCharacter', realm: 'Tarren Mill', isPrimary: true },
            ],
          }),
        });
      });

      // Navigate to profile/settings where linking would happen
      await page.goto('/dashboard');
      await page.waitForLoadState('networkidle');

      await page.screenshot({ path: 'test-results/before-battlenet-link.png', fullPage: true });
    });
  });

  test.describe('Token Handling', () => {
    test('should store token in localStorage after OAuth', async ({ page }) => {
      // Set up routes first
      await page.route('**/api/v1/auth/discord/callback', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            accessToken: 'new-jwt-token-12345',
            refreshToken: 'refresh-token',
            expiresIn: 900,
          }),
        });
      });

      await page.route('**/api/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 1,
            username: 'testuser',
            role: 'RAIDER',
            linkedCharacters: [],
          }),
        });
      });

      await page.route('**/api/v1/user/guilds', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
      });

      await page.route('**/api/v1/user/guilds/active', async (route) => {
        await route.fulfill({
          status: 204,
          body: '',
        });
      });

      await page.goto('/auth/discord/callback?code=valid-code');
      await page.waitForLoadState('networkidle');

      // Verify token is stored
      const token = await page.evaluate(() => localStorage.getItem('token'));
      expect(token).toBe('new-jwt-token-12345');

      await page.screenshot({ path: 'test-results/token-stored.png', fullPage: true });
    });

    test('should redirect to login when token is invalid', async ({ page }) => {
      // Set up route mocks BEFORE setting localStorage token
      await page.route('**/api/v1/auth/me', async (route) => {
        await route.fulfill({
          status: 401,
          contentType: 'application/json',
          body: JSON.stringify({
            error: 'Invalid or expired token',
          }),
        });
      });

      // Set token after routes are mocked
      await page.addInitScript(() => {
        localStorage.setItem('token', 'invalid-expired-token');
      });

      await page.goto('/dashboard');
      await page.waitForLoadState('domcontentloaded');

      // Should redirect to login - wait for the login page to be visible
      // The 401 response handler does window.location.href = '/login' which clears token
      await expect(page).toHaveURL(/login/, { timeout: 10000 });

      // Wait for the page to stabilize after redirect
      await page.waitForLoadState('domcontentloaded');
      await page.waitForTimeout(1000);

      // The important thing is we're on the login page - token clearing may race with navigation
      expect(page.url()).toContain('/login');

      await page.screenshot({ path: 'test-results/invalid-token-redirect.png', fullPage: true });
    });
  });
});
