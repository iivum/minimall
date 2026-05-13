import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 3 : 1,
  workers: 1,
  reporter: [
    ['html', { outputFolder: 'results/html-report' }],
    ['json', { outputFile: 'results/test-results.json' }],
    ['junit', { outputFile: 'results/junit.xml' }],
    ['list']
  ],
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:8080',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'on-first-retry',
    actionTimeout: 15000,
    navigationTimeout: 30000
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ],
  timeout: 60000,
  expect: {
    timeout: 10000
  },
  webServer: process.env.CI ? undefined : undefined
});