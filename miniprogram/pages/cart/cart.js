// pages/cart/cart.js
const app = getApp()

Page({
  data: {
    cartItems: [],
    totalPrice: 0,
    selectedCount: 0,
    allSelected: true,
  },

  onShow() {
    this.loadCart()
  },

  loadCart() {
    const cart = wx.getStorageSync('cart') || []
    const selectedItems = cart.filter(item => item.selected !== false)
    const totalPrice = selectedItems.reduce((sum, item) => sum + item.price * item.quantity, 0)
    const selectedCount = selectedItems.length
    this.setData({
      cartItems: cart,
      totalPrice: totalPrice.toFixed(2),
      selectedCount,
      allSelected: cart.length > 0 && cart.every(item => item.selected !== false),
    })
  },

  toggleSelect(e) {
    const { index } = e.currentTarget.dataset
    const cart = this.data.cartItems
    cart[index].selected = !cart[index].selected
    wx.setStorageSync('cart', cart)
    this.loadCart()
  },

  toggleAllSelect() {
    const cart = this.data.cartItems
    const allSelected = this.data.allSelected
    cart.forEach(item => { item.selected = !allSelected })
    wx.setStorageSync('cart', cart)
    this.loadCart()
  },

  onQuantityChange(e) {
    const { index } = e.currentTarget.dataset
    const { type } = e.currentTarget.dataset
    const cart = this.data.cartItems
    if (type === 'minus') {
      if (cart[index].quantity > 1) cart[index].quantity -= 1
    } else {
      cart[index].quantity += 1
    }
    wx.setStorageSync('cart', cart)
    this.loadCart()
  },

  removeItem(e) {
    const { index } = e.currentTarget.dataset
    const cart = this.data.cartItems
    cart.splice(index, 1)
    wx.setStorageSync('cart', cart)
    this.loadCart()
  },

  checkout() {
    const selectedItems = this.data.cartItems.filter(item => item.selected !== false)
    if (selectedItems.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' })
      return
    }
    const orderItems = selectedItems.map(item => ({
      productId: item.id,
      quantity: item.quantity,
      price: item.price,
    }))
    wx.navigateTo({ url: `/pages/order/create?items=${encodeURIComponent(JSON.stringify(orderItems))}` })
  },

  clearSelected() {
    const cart = this.data.cartItems.filter(item => item.selected === false)
    wx.setStorageSync('cart', cart)
    this.loadCart()
  },
})