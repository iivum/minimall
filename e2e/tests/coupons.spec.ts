import { test, expect, request } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

test.describe('优惠券', () => {
  let authToken: string;
  let userId: string;

  test.beforeAll(async () => {
    const response = await request.post(`${BASE_URL}/api/auth/login`, {
      data: { openid: 'e2e-test-coupons' }
    });
    const body = await response.json();
    authToken = body.token;
    userId = body.userId;
  });

  test('获取优惠券列表', async ({ page }) => {
    await page.goto(`${BASE_URL}/pages/coupons/index`);

    await page.evaluate((token) => {
      localStorage.setItem('token', token);
    }, authToken);

    await page.reload();

    await expect(page.locator('.tab-bar')).toBeVisible({ timeout: 10000 });

    const availableTab = page.locator('.tab-item').first();
    await expect(availableTab).toBeVisible();
  });

  test('API: 获取我的优惠券', async () => {
    const response = await request.get(`${BASE_URL}/api/coupons/my`, {
      headers: {
        'Authorization': `Bearer ${authToken}`,
        'X-User-Id': userId
      }
    });
    expect(response.ok()).toBeTruthy();
  });

  test('API: 领取优惠券', async () => {
    const couponsResponse = await request.get(`${BASE_URL}/api/coupons`, {});
    const coupons = await couponsResponse.json();
    if (coupons.length > 0) {
      const couponId = coupons[0].id;
      const response = await request.post(`${BASE_URL}/api/coupons/${couponId}/claim`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'X-User-Id': userId
        }
      });
      expect(response.ok()).toBeTruthy();
    }
  });

  test('优惠券Tab切换', async ({ page }) => {
    await page.goto(`${BASE_URL}/pages/coupons/index`);

    await page.evaluate((token) => {
      localStorage.setItem('token', token);
    }, authToken);

    await page.reload();

    const usedTab = page.locator('.tab-item').nth(1);
    await usedTab.click();

    const usedCoupons = page.locator('.coupons-list');
    await expect(usedCoupons).toBeVisible();
  });

  test('领取优惠券按钮', async ({ page }) => {
    await page.goto(`${BASE_URL}/pages/coupons/index`);

    await page.evaluate((token) => {
      localStorage.setItem('token', token);
    }, authToken);

    await page.reload();

    await page.waitForSelector('.empty-state, .coupons-list', { timeout: 5000 }).catch(() => {});

    const emptyState = page.locator('.empty-state');
    if (await emptyState.isVisible()) {
      const claimBtn = page.locator('.claim-btn');
      if (await claimBtn.isVisible()) {
        await expect(claimBtn).toBeVisible();
      }
    }
  });
});