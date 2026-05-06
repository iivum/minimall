// pages/product/detail.js
const app = getApp()

Page({
  data: {
    product: null,
    quantity: 1,
    loading: true,
  },

  onLoad(options) {
    const { id } = options
    this.loadProduct(id)
  },

  async loadProduct(id) {
    this.setData({ loading: true })
    try {
      const product = await app.request({ url: `/products/${id}` })
      this.setData({ product, loading: false })
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  onQuantityChange(e) {
    const { value } = e.detail
    this.setData({ quantity: parseInt(value) || 1 })
  },

  addToCart() {
    const { product, quantity } = this.data
    if (!product) return
    const cart = wx.getStorageSync('cart') || []
    const exist = cart.find(item => item.id === product.id)
    if (exist) {
      exist.quantity += quantity
    } else {
      cart.push({ ...product, quantity })
    }
    wx.setStorageSync('cart', cart)
    wx.showToast({ title: '已加入购物车', icon: 'success' })
  },

  goToCart() {
    wx.switchTab({ url: '/pages/cart/cart' })
  },

  buyNow() {
    const { product, quantity } = this.data
    if (!product) return
    const cart = [{ ...product, quantity }]
    wx.setStorageSync('cart', cart)
    wx.switchTab({ url: '/pages/cart/cart' })
  },
})