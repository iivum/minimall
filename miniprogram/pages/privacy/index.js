// pages/privacy/index.js
Page({
  data: {
    loading: true,
  },

  onLoad() {
    this.loadPrivacyPolicy()
  },

  async loadPrivacyPolicy() {
    this.setData({ loading: true })
    try {
      const res = await getApp().request({ url: '/content/privacy' })
      this.setData({ content: res.content || '', loading: false })
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },
})