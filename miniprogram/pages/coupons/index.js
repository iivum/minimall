// pages/coupons/index.js
const app = getApp()

Page({
  data: {
    coupons: [],
    activeTab: 'available',
    loading: false,
  },

  onShow() {
    this.loadCoupons()
  },

  loadCoupons() {
    this.setData({ loading: true })
    const coupons = wx.getStorageSync('coupons') || []
    this.setData({
      coupons,
      loading: false,
    })
  },

  onTabChange(e) {
    const { tab } = e.currentTarget.dataset
    this.setData({ activeTab: tab })
  },

  getCoupon(e) {
    const { index } = e.currentTarget.dataset
    const coupons = this.data.coupons
    if (coupons[index].status === 'available') {
      coupons[index].status = 'used'
      wx.setStorageSync('coupons', coupons)
      wx.showToast({ title: '领取成功', icon: 'success' })
      this.loadCoupons()
    }
  },

  goShopping() {
    wx.switchTab({ url: '/pages/index/index' })
  },
})