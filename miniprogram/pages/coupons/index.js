// pages/coupons/index.js
const app = getApp()

Page({
  data: {
    availableCoupons: [],
    usedCoupons: [],
    expiredCoupons: [],
    loading: true,
    error: null,
    activeTab: 'available',
  },

  onLoad() {
    this.loadCoupons()
  },

  onShow() {
    this.loadCoupons()
  },

  async loadCoupons() {
    this.setData({ loading: true, error: null })
    try {
      const userId = app.globalData.userId
      if (!userId) {
        this.setData({ loading: false, availableCoupons: [], usedCoupons: [], expiredCoupons: [] })
        return
      }
      const res = await app.request({ url: `/users/${userId}/coupons` })
      this.setData({
        availableCoupons: res?.available || [],
        usedCoupons: res?.used || [],
        expiredCoupons: res?.expired || [],
        loading: false,
      })
    } catch (err) {
      this.setData({ loading: false, error: '加载失败，请重试' })
    }
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
  },

  async claimCoupon() {
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  onRetry() {
    this.loadCoupons()
  },
})