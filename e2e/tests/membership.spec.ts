import { test, expect, request } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

test.describe('会员权益', () => {
  let authToken: string;
  let userId: string;

  test.beforeAll(async () => {
    const response = await request.post(`${BASE_URL}/api/auth/login`, {
      data: { openid: 'e2e-test-member' }
    });
    const body = await response.json();
    authToken = body.token;
    userId = body.userId;
  });

  test('获取会员权益信息', async ({ page }) => {
    await page.goto(`${BASE_URL}/pages/member/index`);

    await page.evaluate((token) => {
      localStorage.setItem('token', token);
    }, authToken);

    await page.reload();

    await expect(page.locator('.member-card')).toBeVisible({ timeout: 10000 });

    const memberTitle = await page.locator('.member-title').textContent();
    expect(memberTitle).toBeTruthy();

    const pointsValue = await page.locator('.points-value').textContent();
    expect(pointsValue).toBeTruthy();
  });

  test('API: 获取会员权益端点返回正确数据', async () => {
    const response = await request.get(`${BASE_URL}/api/membership/benefits`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(body).toHaveProperty('gradeCode');
    expect(body).toHaveProperty('gradeName');
    expect(body).toHaveProperty('discountPercent');
  });

  test('会员等级展示', async ({ page }) => {
    await page.goto(`${BASE_URL}/pages/member/index`);

    await page.evaluate((token) => {
      localStorage.setItem('token', token);
    }, authToken);

    await page.reload();

    const memberCard = page.locator('.member-card');
    await expect(memberCard).toBeVisible();

    const memberTitle = page.locator('.member-title');
    await expect(memberTitle).toBeVisible();
  });
});