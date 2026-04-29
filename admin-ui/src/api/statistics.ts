import request from '@/utils/request'

export interface DailyMetric {
  date: string
  orders: number
  gmv: number
  users: number
}

export interface StatisticsOverview {
  totalOrders: number
  totalGMV: number
  totalUsers: number
  averageOrderValue: number
  ordersChange: number
  gmvChange: number
  usersChange: number
  conversionRate: number
  dailyMetrics: DailyMetric[]
}

export interface QuickSummary {
  todayOrders: number
  todayGMV: number
  todayNewUsers: number
  weekOrders: number
  weekGMV: number
  weekNewUsers: number
  avgOrderValue: number
}

export const getOverviewMetrics = (startDate: string, endDate: string) => {
  return request.get<StatisticsOverview>('/api/statistics/overview', {
    params: { startDate, endDate }
  })
}

export const getDailyMetrics = (startDate: string, endDate: string) => {
  return request.get<DailyMetric[]>('/api/statistics/daily', {
    params: { startDate, endDate }
  })
}

export const getUserGrowth = (startDate: string, endDate: string) => {
  return request.get<any[]>('/api/statistics/users/growth', {
    params: { startDate, endDate }
  })
}

export const getQuickSummary = () => {
  return request.get<QuickSummary>('/api/statistics/summary')
}

export const exportData = (startDate: string, endDate: string, format: 'csv' | 'xlsx' = 'csv') => {
  return request.get('/api/statistics/export', {
    params: { startDate, endDate, format }
  })
}
