// pages/points/index.js
const app = getApp()

Page({
  data: {
    account: {
      balance: 0,
      totalEarned: 0,
      totalSpent: 0,
    },
    history: [],
    isSignedToday: false,
    loading: false,
  },

  onLoad() {
    this.loadPointsData()
  },

  onShow() {
    this.loadPointsData()
  },

  async loadPointsData() {
    this.setData({ loading: true })
    try {
      const userId = app.globalData.userId
      if (!userId) {
        wx.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      const [accountRes, historyRes] = await Promise.all([
        app.request({ url: '/points/account' }),
        app.request({ url: '/points/history' }),
      ])
      this.setData({
        account: accountRes,
        history: historyRes || [],
        loading: false,
      })
      this.checkSignStatus()
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  checkSignStatus() {
    const today = new Date().toDateString()
    const lastSignDate = wx.getStorageSync('lastSignDate')
    this.setData({ isSignedToday: lastSignDate === today })
  },

  async handleSignIn() {
    if (this.data.isSignedToday) return
    this.setData({ loading: true })
    try {
      const res = await app.request({
        url: '/points/sign-in',
        method: 'POST',
      })
      this.setData({
        account: res,
        isSignedToday: true,
        loading: false,
      })
      wx.setStorageSync('lastSignDate', new Date().toDateString())
      wx.showToast({ title: '签到成功', icon: 'success' })
      this.loadPointsData()
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: err.message || '签到失败', icon: 'none' })
    }
  },

  async handleShare() {
    try {
      const shareId = `share_${Date.now()}`
      const res = await app.request({
        url: `/points/earn/share/${shareId}`,
        method: 'POST',
      })
      this.setData({ account: res })
      wx.showToast({ title: '分享链接已生成', icon: 'success' })
    } catch (err) {
      wx.showToast({ title: err.message || '生成失败', icon: 'none' })
    }
  },
})