// pages/order/list.js
const app = getApp()

Page({
  data: {
    orders: [],
    loading: true,
    status: '',
  },

  onLoad(options) {
    if (options.status) {
      this.setData({ status: options.status })
    }
    this.loadOrders()
  },

  onShow() {
    this.loadOrders()
  },

  async loadOrders() {
    this.setData({ loading: true })
    try {
      const userId = app.globalData.userId
      if (!userId) {
        this.setData({ orders: [], loading: false })
        return
      }
      const orders = await app.request({ url: `/orders/user/${userId}` })
      this.setData({ orders, loading: false })
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/order/detail?id=${id}` })
  },
})