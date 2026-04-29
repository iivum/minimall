Page({
  data: {
    goods: {
      id: 1,
      name: '经典款连衣裙 2024新款',
      price: '299.00',
      originalPrice: '599.00',
      images: [
        'https://picsum.photos/750/750?random=30',
        'https://picsum.photos/750/750?random=31',
        'https://picsum.photos/750/750?random=32',
      ],
      description: '优质面料，舒适透气，时尚百搭，适合多种场合穿着。',
      stock: 100,
    },
    selectedCount: 1,
    currentImageIndex: 0,
  },
  onLoad(e) {
    if (e.id) {
      this.setData({ 'goods.id': e.id });
    }
  },
  swiperChange(e) {
    this.setData({ currentImageIndex: e.detail.current });
  },
  addCount() {
    this.setData({ selectedCount: this.data.selectedCount + 1 });
  },
  reduceCount() {
    if (this.data.selectedCount > 1) {
      this.setData({ selectedCount: this.data.selectedCount - 1 });
    }
  },
  addToCart() {
    wx.showToast({ title: '已加入购物车', icon: 'success' });
  },
  buyNow() {
    wx.showToast({ title: '正在跳转支付...', icon: 'none' });
  },
});
