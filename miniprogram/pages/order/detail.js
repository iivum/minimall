// pages/order/detail.js
const app = getApp()

Page({
  data: {
    order: null,
    loading: true,
  },

  onLoad(options) {
    const { id } = options
    if (id) {
      this.loadOrder(id)
    }
  },

  async loadOrder(id) {
    this.setData({ loading: true })
    try {
      const order = await app.request({ url: `/orders/${id}` })
      this.setData({ order, loading: false })
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  payOrder() {
    const { order } = this.data
    if (!order || order.status !== 'PENDING') return
    wx.navigateTo({ url: `/pages/pay/pay?orderId=${order.id}&amount=${order.totalAmount}` })
  },

  cancelOrder() {
    wx.showModal({
      title: '确认取消',
      content: '确定要取消该订单吗?',
      success: (res) => {
        if (res.confirm) {
          this.doCancelOrder()
        }
      },
    })
  },

  async doCancelOrder() {
    try {
      await app.request({
        url: `/orders/${this.data.order.id}/status`,
        method: 'PATCH',
        data: { status: 'CANCELLED' },
      })
      wx.showToast({ title: '已取消', icon: 'success' })
      this.loadOrder(this.data.order.id)
    } catch (err) {
      wx.showToast({ title: '取消失败', icon: 'none' })
    }
  },
})