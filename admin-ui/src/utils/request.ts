import { ElMessage } from 'element-plus'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  headers?: Record<string, string>
  body?: any
  params?: Record<string, string>
}

export default async function request<T = any>(url: string, options: RequestOptions = {}): Promise<{ data: T }> {
  const { method = 'GET', headers = {}, body, params } = options

  let queryString = ''
  if (params) {
    queryString = '?' + new URLSearchParams(params).toString()
  }

  const fullUrl = `${BASE_URL}${url}${queryString}`

  const config: RequestInit = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
  }

  if (body && method !== 'GET') {
    config.body = JSON.stringify(body)
  }

  try {
    const response = await fetch(fullUrl, config)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const data = await response.json()
    return { data }
  } catch (error) {
    console.error('Request failed:', error)
    throw error
  }
}
