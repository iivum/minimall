// pages/member/index.js
const app = getApp()

Page({
  data: {
    userInfo: null,
    benefits: null,
    points: 0,
    loading: true,
  },

  onLoad() {
    this.loadMemberInfo()
  },

  onShow() {
    this.loadMemberInfo()
  },

  async loadMemberInfo() {
    this.setData({ loading: true })
    try {
      const userId = app.globalData.userId
      if (!userId) {
        this.setData({ loading: false, userInfo: null })
        return
      }
      const [userRes, benefitsRes] = await Promise.all([
        app.request({ url: `/users/${userId}` }),
        app.request({ url: '/membership/benefits' }),
      ])
      this.setData({
        userInfo: userRes,
        benefits: benefitsRes,
        points: benefitsRes?.points || 0,
        loading: false,
      })
    } catch (err) {
      this.setData({ loading: false })
    }
  },

  goToOrders() {
    wx.switchTab({ url: '/pages/order/list' })
  },

  goToCoupons() {
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  goToPoints() {
    wx.navigateTo({ url: '/pages/points/index' })
  },

  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗?',
      success: (res) => {
        if (res.confirm) {
          app.logout()
          wx.switchTab({ url: '/pages/index/index' })
        }
      },
    })
  },
})