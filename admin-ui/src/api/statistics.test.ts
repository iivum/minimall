import { describe, it, expect, vi } from 'vitest'
import { getOverviewMetrics, getDailyMetrics, getQuickSummary, exportData } from './statistics'

vi.mock('@/utils/request', () => ({
  default: {
    get: vi.fn()
  }
}))

describe('statistics API', () => {
  describe('getOverviewMetrics', () => {
    it('should call request.get with correct parameters', async () => {
      const request = await import('@/utils/request')
      const mockGet = request.default.get as ReturnType<typeof vi.fn>
      mockGet.mockResolvedValue({
        totalOrders: 100,
        totalGMV: 5000,
        totalUsers: 25,
        averageOrderValue: 50
      })

      const result = await getOverviewMetrics('2024-01-01', '2024-01-07')

      expect(mockGet).toHaveBeenCalledWith('/api/statistics/overview', {
        params: { startDate: '2024-01-01', endDate: '2024-01-07' }
      })
      expect(result.totalOrders).toBe(100)
    })
  })

  describe('getDailyMetrics', () => {
    it('should call request.get with correct parameters', async () => {
      const request = await import('@/utils/request')
      const mockGet = request.default.get as ReturnType<typeof vi.fn>
      mockGet.mockResolvedValue([
        { date: '2024-01-01', orders: 10, gmv: 500, users: 5 }
      ])

      const result = await getDailyMetrics('2024-01-01', '2024-01-07')

      expect(mockGet).toHaveBeenCalledWith('/api/statistics/daily', {
        params: { startDate: '2024-01-01', endDate: '2024-01-07' }
      })
      expect(result).toHaveLength(1)
    })
  })

  describe('getQuickSummary', () => {
    it('should call request.get without parameters', async () => {
      const request = await import('@/utils/request')
      const mockGet = request.default.get as ReturnType<typeof vi.fn>
      mockGet.mockResolvedValue({
        todayOrders: 15,
        todayGMV: 750,
        todayNewUsers: 5
      })

      const result = await getQuickSummary()

      expect(mockGet).toHaveBeenCalledWith('/api/statistics/summary')
      expect(result.todayOrders).toBe(15)
    })
  })

  describe('exportData', () => {
    it('should call request.get with format parameter', async () => {
      const request = await import('@/utils/request')
      const mockGet = request.default.get as ReturnType<typeof vi.fn>
      mockGet.mockResolvedValue({ success: true })

      await exportData('2024-01-01', '2024-01-07', 'csv')

      expect(mockGet).toHaveBeenCalledWith('/api/statistics/export', {
        params: { startDate: '2024-01-01', endDate: '2024-01-07', format: 'csv' }
      })
    })

    it('should default to csv format', async () => {
      const request = await import('@/utils/request')
      const mockGet = request.default.get as ReturnType<typeof vi.fn>
      mockGet.mockResolvedValue({ success: true })

      await exportData('2024-01-01', '2024-01-07')

      expect(mockGet).toHaveBeenCalledWith('/api/statistics/export', {
        params: { startDate: '2024-01-01', endDate: '2024-01-07', format: 'csv' }
      })
    })
  })
})
