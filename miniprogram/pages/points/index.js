// pages/points/index.js
const app = getApp()

Page({
  data: {
    points: 0,
    pointsHistory: [],
    loading: false,
  },

  onShow() {
    this.loadPoints()
  },

  loadPoints() {
    this.setData({ loading: true })
    const userInfo = wx.getStorageSync('userInfo')
    if (!userInfo) {
      this.setData({ points: 0, pointsHistory: [], loading: false })
      return
    }
    const points = userInfo.points || 0
    const pointsHistory = userInfo.pointsHistory || []
    this.setData({
      points,
      pointsHistory,
      loading: false,
    })
  },

  onPullDownRefresh() {
    this.loadPoints()
    wx.stopPullDownRefresh()
  },

  goExchange() {
    wx.navigateTo({ url: '/pages/product/list' })
  },
})