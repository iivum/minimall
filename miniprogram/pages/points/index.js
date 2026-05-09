// pages/points/index.js
const app = getApp()

Page({
  data: {
    points: 0,
    records: [],
    loading: true,
    error: null,
    hasMore: false,
    page: 1,
    pageSize: 20,
  },

  onLoad() {
    this.loadPointsAndRecords()
  },

  onShow() {
    this.loadPointsAndRecords()
  },

  async loadPointsAndRecords() {
    this.setData({ loading: true, error: null })
    try {
      const userId = app.globalData.userId
      if (!userId) {
        this.setData({ loading: false, points: 0, records: [] })
        return
      }
      const [pointsRes, recordsRes] = await Promise.all([
        app.request({ url: `/users/${userId}/points` }),
        app.request({ url: `/users/${userId}/points/records?page=${this.data.page}&pageSize=${this.data.pageSize}` }),
      ])
      this.setData({
        points: pointsRes?.balance || 0,
        records: recordsRes?.records || [],
        hasMore: recordsRes?.hasMore || false,
        loading: false,
      })
    } catch (err) {
      this.setData({ loading: false, error: '加载失败，请重试' })
    }
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMoreRecords()
    }
  },

  async loadMoreRecords() {
    const nextPage = this.data.page + 1
    try {
      const userId = app.globalData.userId
      const recordsRes = await app.request({
        url: `/users/${userId}/points/records?page=${nextPage}&pageSize=${this.data.pageSize}`,
      })
      this.setData({
        records: [...this.data.records, ...(recordsRes?.records || [])],
        hasMore: recordsRes?.hasMore || false,
        page: nextPage,
      })
    } catch (err) {
      wx.showToast({ title: '加载更多失败', icon: 'none' })
    }
  },

  onRetry() {
    this.setData({ page: 1 })
    this.loadPointsAndRecords()
  },
})