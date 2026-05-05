// pages/index/index.js
const { request } = require('../../utils/request.js');

Page({
  data: {
    loading: true,
    bannerList: [],
    categoryList: [],
    flashSale: {
      title: '限时秒杀',
      endTime: '12:00:00',
      goods: [],
    },
    recommendGoods: [],
  },

  onLoad() {
    this.loadHomeData();
  },

  async loadHomeData() {
    try {
      const [categoriesRes, productsRes] = await Promise.all([
        request({ url: '/api/categories' }),
        request({ url: '/api/products', data: { limit: 9, offset: 0 } }),
      ]);

      const allProducts = productsRes.data || [];
      this.setData({
        loading: false,
        categoryList: categoriesRes.data || [],
        'flashSale.goods': allProducts.slice(0, 3),
        recommendGoods: allProducts.slice(3, 9),
      });
    } catch (err) {
      this.setData({ loading: false });
      console.error('Load home data error:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  goCategory(e) {
    const { id } = e.currentTarget.dataset;
    wx.switchTab({ url: '/pages/category/category' });
  },

  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}` });
  },

  goSearch() {
    wx.showToast({ title: '搜索功能开发中', icon: 'none' });
  },
});
