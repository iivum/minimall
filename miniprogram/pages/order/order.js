// pages/order/order.js
const { request } = require('../../utils/request.js');

Page({
  data: {
    currentTab: 0,
    tabs: ['全部', '待付款', '待发货', '待收货', '已完成'],
    orderList: [],
    statusMap: {
      '全部': null,
      '待付款': '待付款',
      '待发货': '待发货',
      '待收货': '待收货',
      '已完成': '已完成',
    },
  },

  onLoad() {
    this.loadOrders();
  },

  onShow() {
    if (this.data.orderList.length > 0) {
      this.loadOrders();
    }
  },

  async loadOrders() {
    try {
      wx.showLoading({ title: '加载中...' });
      const status = this.data.statusMap[this.data.tabs[this.data.currentTab]];
      const data = status ? { status } : {};

      const result = await request({
        url: '/api/orders',
        method: 'GET',
        data,
      });

      this.setData({ orderList: result.data || [] });
      wx.hideLoading();
    } catch (err) {
      wx.hideLoading();
      console.error('Load orders error:', err);
      if (err.message !== '未登录') {
        wx.showToast({ title: '加载失败', icon: 'none' });
      }
    }
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ currentTab: index });
    this.loadOrders();
  },

  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}` });
  },

  payOrder(e) {
    wx.showToast({ title: '支付功能开发中', icon: 'none' });
  },
});
