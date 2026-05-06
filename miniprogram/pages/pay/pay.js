// pages/pay/pay.js
const app = getApp()

Page({
  data: {
    orderId: '',
    amount: 0,
    loading: false,
    payParams: null,
  },

  onLoad(options) {
    const { orderId, amount } = options
    this.setData({ orderId, amount: parseFloat(amount) })
  },

  async requestPay() {
    this.setData({ loading: true })
    try {
      const openid = wx.getStorageSync('openid') || app.globalData.openid
      const res = await app.request({
        url: `/pay/create/${this.data.orderId}?openid=${openid}`,
        method: 'POST',
      })
      this.setData({ payParams: res, loading: false })
      this.doPay()
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '获取支付信息失败', icon: 'none' })
    }
  },

  async doPay() {
    const { payParams } = this.data
    if (!payParams) return
    try {
      await wx.requestPayment({
        timeStamp: payParams.timeStamp,
        nonceStr: payParams.nonceStr,
        package: payParams.package,
        signType: payParams.signType,
        paySign: payParams.paySign,
      })
      wx.showToast({ title: '支付成功', icon: 'success' })
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/order/list' })
      }, 1500)
    } catch (err) {
      if (err.errMsg !== 'requestPayment:fail cancel') {
        wx.showToast({ title: '支付失败', icon: 'none' })
      }
    }
  },

  onPayTap() {
    this.requestPay()
  },
})