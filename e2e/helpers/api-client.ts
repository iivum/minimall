import { request } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

export interface AuthToken {
  token: string;
  userId: string;
}

export async function login(openid: string = 'test-user-openid'): Promise<AuthToken> {
  const response = await request.post(`${BASE_URL}/api/auth/login`, {
    data: { openid }
  });
  const body = await response.json();
  if (!response.ok()) {
    throw new Error(`Login failed: ${response.status()} - ${JSON.stringify(body)}`);
  }
  return { token: body.token, userId: body.userId };
}

export async function getAuthHeaders(token: string): Promise<Record<string, string>> {
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };
}