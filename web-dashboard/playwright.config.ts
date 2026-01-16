import { defineConfig, devices } from '@playwright/test';

/**
 * EdgeRush LootMan E2E Test Configuration
 *
 * Run with: npx playwright test
 * Run UI mode: npx playwright test --ui
 * Run specific test: npx playwright test e2e/login.spec.ts
 *
 * Tests run headless by default. Use --headed flag for headed mode.
 * Screenshots, traces, and videos are captured for all tests.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 1,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['json', { outputFile: 'playwright-report/results.json' }],
    ['list'],
  ],
  outputDir: 'test-results',
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost',
    // Run headless by default
    headless: true,
    // Capture evidence for all tests
    trace: 'on',
    screenshot: 'on',
    video: 'on',
    // Better logging
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    // Mobile viewports
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] },
    },
  ],

  // Web server configuration
  // Use the live docker-compose environment (nginx on port 80)
  // Start docker-compose before running tests: docker-compose up -d
  webServer: process.env.CI
    ? {
        command: 'npm run dev',
        url: 'http://localhost:3000',
        reuseExistingServer: false,
        timeout: 120000,
      }
    : undefined, // Use existing docker-compose environment
});
