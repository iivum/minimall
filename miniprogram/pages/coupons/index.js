// pages/coupons/index.js
const app = getApp()

Page({
  data: {
    coupons: [],
    loading: true,
  },

  onLoad() {
    this.loadCoupons()
  },

  onShow() {
    this.loadCoupons()
  },

  async loadCoupons() {
    this.setData({ loading: true })
    try {
      const userId = app.globalData.userId
      if (!userId) {
        this.setData({ loading: false, coupons: [] })
        return
      }
      const res = await app.request({ url: `/users/${userId}/coupons` })
      this.setData({
        coupons: res || [],
        loading: false,
      })
    } catch (err) {
      this.setData({ loading: false })
    }
  },
})