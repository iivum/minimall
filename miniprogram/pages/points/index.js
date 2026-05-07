// pages/points/index.js
const app = getApp()

Page({
  data: {
    points: 0,
    transactions: [],
    loading: true,
  },

  onLoad() {
    this.loadPoints()
  },

  onShow() {
    this.loadPoints()
  },

  async loadPoints() {
    this.setData({ loading: true })
    try {
      const userId = app.globalData.userId
      if (!userId) {
        this.setData({ loading: false, transactions: [] })
        return
      }
      const [pointsRes, transactionsRes] = await Promise.all([
        app.request({ url: `/users/${userId}/points` }),
        app.request({ url: `/users/${userId}/points/transactions` }),
      ])
      this.setData({
        points: pointsRes?.points || 0,
        transactions: transactionsRes || [],
        loading: false,
      })
    } catch (err) {
      this.setData({ loading: false })
    }
  },
})