// pages/index/index.js
const app = getApp()

Page({
  data: {
    products: [],
    loading: true,
    searchKey: '',
  },

  onLoad() {
    this.loadProducts()
  },

  onShow() {
    this.checkLoginAndLoad()
  },

  onPullDownRefresh() {
    this.loadProducts().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  checkLoginAndLoad() {
    if (!app.globalData.userId) {
      wx.login({
        success: (res) => {
          if (res.code) {
            app.login(res.code).catch(() => {}).finally(() => {
              this.loadProducts()
            })
          }
        },
      })
    } else {
      this.loadProducts()
    }
  },

  async loadProducts() {
    this.setData({ loading: true })
    try {
      const products = await app.request({ url: '/products' })
      this.setData({ products, loading: false })
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  onSearch(e) {
    const key = e.detail.value.trim()
    if (!key) {
      this.loadProducts()
      return
    }
    app.request({ url: `/products/search?name=${encodeURIComponent(key)}` })
      .then(products => this.setData({ products }))
      .catch(() => wx.showToast({ title: '搜索失败', icon: 'none' }))
  },

  goToProduct(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/product/detail?id=${id}` })
  },

  addToCart(e) {
    const { id } = e.currentTarget.dataset
    const product = this.data.products.find(p => p.id === id)
    if (!product) return
    const cart = wx.getStorageSync('cart') || []
    const exist = cart.find(item => item.id === id)
    if (exist) {
      exist.quantity += 1
    } else {
      cart.push({ ...product, quantity: 1 })
    }
    wx.setStorageSync('cart', cart)
    wx.showToast({ title: '已加入购物车', icon: 'success' })
  },

  goToCart() {
    wx.switchTab({ url: '/pages/cart/cart' })
  },
})