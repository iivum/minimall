import { test, expect, request } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

test.describe('积分系统', () => {
  let authToken: string;
  let userId: string;

  test.beforeAll(async () => {
    const response = await request.post(`${BASE_URL}/api/auth/login`, {
      data: { openid: 'e2e-test-points' }
    });
    const body = await response.json();
    authToken = body.token;
    userId = body.userId;
  });

  test('获取积分账户信息', async ({ page }) => {
    await page.goto(`${BASE_URL}/pages/points/index`);

    await page.evaluate((token) => {
      localStorage.setItem('token', token);
    }, authToken);

    await page.reload();

    await expect(page.locator('.points-card')).toBeVisible({ timeout: 10000 });

    const pointsBalance = await page.locator('.points-balance').textContent();
    expect(pointsBalance).toBeTruthy();
  });

  test('API: 获取积分账户端点返回正确数据', async () => {
    const response = await request.get(`${BASE_URL}/api/points/account`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(body).toHaveProperty('userId');
    expect(body).toHaveProperty('balance');
  });

  test('API: 获取积分历史记录', async () => {
    const response = await request.get(`${BASE_URL}/api/points/history`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(Array.isArray(body)).toBeTruthy();
  });

  test('签到获取积分', async () => {
    const response = await request.post(`${BASE_URL}/api/points/sign-in`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(body).toHaveProperty('balance');
  });

  test('积分记录列表展示', async ({ page }) => {
    await page.goto(`${BASE_URL}/pages/points/index`);

    await page.evaluate((token) => {
      localStorage.setItem('token', token);
    }, authToken);

    await page.reload();

    const recordsSection = page.locator('.records-section');
    await expect(recordsSection).toBeVisible();
  });
});