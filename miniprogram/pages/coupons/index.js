// pages/coupons/index.js
const app = getApp()

Page({
  data: {
    activeTab: 'my',
    myCoupons: [],
    newUserCoupons: [],
    loading: false,
  },

  onLoad() {
    this.loadCouponsData()
  },

  onShow() {
    this.loadCouponsData()
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    if (tab === 'my') {
      this.loadMyCoupons()
    } else {
      this.loadNewUserCoupons()
    }
  },

  async loadCouponsData() {
    this.setData({ loading: true })
    try {
      await Promise.all([
        this.loadMyCoupons(),
        this.loadNewUserCoupons(),
      ])
      this.setData({ loading: false })
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  async loadMyCoupons() {
    try {
      const res = await app.request({ url: '/coupons/my' })
      this.setData({ myCoupons: res || [] })
    } catch (err) {
      console.error('loadMyCoupons error:', err)
    }
  },

  async loadNewUserCoupons() {
    try {
      const res = await app.request({ url: '/coupons/new-user' })
      this.setData({ newUserCoupons: res || [] })
    } catch (err) {
      console.error('loadNewUserCoupons error:', err)
    }
  },

  async handleClaim(e) {
    const couponId = e.currentTarget.dataset.id
    this.setData({ loading: true })
    try {
      await app.request({
        url: `/coupons/${couponId}/claim`,
        method: 'POST',
      })
      wx.showToast({ title: '领取成功', icon: 'success' })
      this.loadCouponsData()
    } catch (err) {
      wx.showToast({ title: err.message || '领取失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
})