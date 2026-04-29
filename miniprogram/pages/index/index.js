// pages/index/index.js
const { request } = require('../../utils/request.js');

Page({
  data: {
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
      wx.showLoading({ title: '加载中...' });

      // Load categories
      const categoriesRes = await request({ url: '/api/categories' });
      this.setData({ categoryList: categoriesRes.data || [] });

      // Load products for flash sale (first 3)
      const productsRes = await request({
        url: '/api/products',
        data: { limit: 3, offset: 0 },
      });
      this.setData({
        'flashSale.goods': productsRes.data || [],
      });

      // Load all products for recommendations
      const allProductsRes = await request({
        url: '/api/products',
        data: { limit: 6, offset: 0 },
      });
      this.setData({ recommendGoods: allProductsRes.data || [] });

      wx.hideLoading();
    } catch (err) {
      wx.hideLoading();
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
