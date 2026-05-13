import { test, expect, request } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

test.describe('客服消息', () => {
  let authToken: string;
  let userId: string;
  let openid: string = 'e2e-test-customer-service';

  test.beforeAll(async () => {
    const response = await request.post(`${BASE_URL}/api/auth/login`, {
      data: { openid }
    });
    const body = await response.json();
    authToken = body.token;
    userId = body.userId;
  });

  test('API: 发送客服文本消息', async () => {
    const response = await request.post(`${BASE_URL}/api/customer-service/receive`, {
      params: {
        openid,
        content: '测试消息',
        type: 'TEXT'
      }
    });
    expect(response.ok()).toBeTruthy();
  });

  test('API: 发送客服图片消息', async () => {
    const response = await request.post(`${BASE_URL}/api/customer-service/receive`, {
      params: {
        openid,
        content: 'test-image-url.jpg',
        type: 'IMAGE'
      }
    });
    expect(response.ok()).toBeTruthy();
  });

  test('API: 获取客服消息历史', async () => {
    const response = await request.get(`${BASE_URL}/api/customer-service/messages/${openid}`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(Array.isArray(body)).toBeTruthy();
  });

  test('API: 获取待处理消息数量', async () => {
    const response = await request.get(`${BASE_URL}/api/customer-service/stats/pending-count`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(body).toHaveProperty('count');
  });

  test('API: 标记消息为已读', async () => {
    const messagesResponse = await request.get(`${BASE_URL}/api/customer-service/messages/${openid}`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    const messages = await messagesResponse.json();
    if (messages.length > 0) {
      const messageId = messages[0].id;
      const response = await request.post(`${BASE_URL}/api/customer-service/${messageId}/read`, {
        headers: { 'Authorization': `Bearer ${authToken}` }
      });
      expect(response.ok()).toBeTruthy();
    }
  });

  test('API: 获取所有待处理消息', async () => {
    const response = await request.get(`${BASE_URL}/api/customer-service/pending`, {
      headers: { 'Authorization': `Bearer ${authToken}` }
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(Array.isArray(body)).toBeTruthy();
  });
});