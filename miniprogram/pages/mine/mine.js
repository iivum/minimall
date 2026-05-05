// pages/mine/mine.js
const { request } = require('../../utils/request.js');
const { login } = require('../../utils/auth.js');

Page({
  data: {
    userInfo: null,
    memberGrade: null,
    isLoggedIn: false,
    menuList: [
      [
        { id: 1, name: '我的订单', icon: '📋', url: '/pages/order/order' },
        { id: 2, name: '优惠券', icon: '🎫', url: '' },
        { id: 3, name: '收货地址', icon: '📍', url: '' },
        { id: 4, name: '收藏', icon: '⭐', url: '' },
      ],
      [
        { id: 5, name: '关于我们', icon: 'ℹ️', url: '' },
        { id: 6, name: '联系客服', icon: '📞', url: '' },
        { id: 7, name: '设置', icon: '⚙️', url: '' },
      ],
    ],
  },

  onLoad() {
    this.checkLoginStatus();
  },

  onShow() {
    if (this.data.isLoggedIn) {
      this.loadMemberGrade();
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (token && userInfo) {
      this.setData({
        isLoggedIn: true,
        userInfo: userInfo,
      });
      this.loadMemberGrade();
    }
  },

  async loadMemberGrade() {
    try {
      const res = await request({ url: '/api/membership/benefits' });
      if (res.data) {
        this.setData({ memberGrade: res.data });
      }
    } catch (err) {
      console.error('Load member grade error:', err);
    }
  },

  goLogin() {
    wx.showModal({
      title: '登录',
      content: '确定要登录吗？',
      confirmText: '确定',
      success: async (res) => {
        if (res.confirm) {
          try {
            // Demo login with username: demo, password: demo123
            const result = await request({
              url: '/api/users/login',
              method: 'POST',
              data: { username: 'demo', password: 'demo123' },
            });

            if (result.data && result.data.token) {
              wx.setStorageSync('token', result.data.token);
              wx.setStorageSync('userInfo', result.data.user);
              this.setData({
                isLoggedIn: true,
                userInfo: result.data.user,
              });
              wx.showToast({ title: '登录成功', icon: 'success' });
            }
          } catch (err) {
            wx.showToast({ title: err.message || '登录失败', icon: 'none' });
          }
        }
      },
    });
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          this.setData({
            isLoggedIn: false,
            userInfo: null,
          });
          wx.showToast({ title: '已退出登录', icon: 'none' });
        }
      },
    });
  },

  goMenuItem(e) {
    const url = e.currentTarget.dataset.url;
    if (url) {
      wx.navigateTo({ url });
    } else {
      wx.showToast({ title: '功能开发中', icon: 'none' });
    }
  },
});
