// pages/coupons/index.js
const app = getApp()

Page({
  data: {
    availableCoupons: [],
    usedCoupons: [],
    expiredCoupons: [],
    displayCoupons: [],
    activeTab: 'available',
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
        this.setData({ loading: false })
        return
      }
      const res = await app.request({
        url: '/coupons/my',
        header: { 'X-User-Id': userId }
      })
      const now = new Date()
      const available = []
      const used = []
      const expired = []

      res.forEach(coupon => {
        const validUntil = new Date(coupon.validUntil)
        if (!coupon.isActive) {
          used.push(coupon)
        } else if (validUntil < now) {
          expired.push(coupon)
        } else {
          available.push(coupon)
        }
      })

      this.setData({
        availableCoupons: available,
        usedCoupons: used,
        expiredCoupons: expired,
        displayCoupons: available,
        loading: false,
      })
    } catch (err) {
      console.error('加载优惠券失败', err)
      this.setData({ loading: false })
    }
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    let displayCoupons = []
    if (tab === 'available') {
      displayCoupons = this.data.availableCoupons
    } else if (tab === 'used') {
      displayCoupons = this.data.usedCoupons
    } else {
      displayCoupons = this.data.expiredCoupons
    }
    this.setData({
      activeTab: tab,
      displayCoupons: displayCoupons,
    })
  },

  useCoupon(e) {
    const couponId = e.currentTarget.dataset.id
    wx.showToast({ title: '优惠券已使用', icon: 'success' })
  },
})