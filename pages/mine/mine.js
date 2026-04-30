const { getUserInfo, authLogin, cacheUserInfo, getCachedUserInfo } = require('../../utils/api.js');
const { login } = require('../../utils/auth.js');

Page({
  data: {
    userInfo: null,
    menuList: [
      [
        { id: 1, name: '我的订单', icon: '📋', url: '/pages/order/order' },
        { id: 2, name: '优惠券', icon: '🎫', url: '/pages/coupon/coupon' },
        { id: 3, name: '我的积分', icon: '💰', url: '/pages/points/points' },
        { id: 4, name: '收货地址', icon: '📍', url: '' },
      ],
      [
        { id: 5, name: '分享有礼', icon: '🎁', url: '/pages/share/share' },
        { id: 6, name: '关于我们', icon: 'ℹ️', url: '' },
        { id: 7, name: '联系客服', icon: '📞', url: '' },
      ],
    ],
    isLoggedIn: false,
  },
  onLoad() {
    this.checkLoginStatus();
  },
  onShow() {
    this.checkLoginStatus();
  },
  checkLoginStatus() {
    const cachedUser = getCachedUserInfo();
    if (cachedUser) {
      this.setData({ userInfo: cachedUser, isLoggedIn: true });
    } else {
      this.setData({
        userInfo: {
          avatar: 'https://picsum.photos/200/200?random=60',
          nickname: '微信用户',
        },
        isLoggedIn: false,
      });
    }
  },
  async goLogin() {
    try {
      const code = await login();
      const userInfo = await authLogin(code).catch(async () => {
        return { nickname: '微信用户', avatar: 'https://picsum.photos/200/200?random=60' };
      });
      cacheUserInfo(userInfo);
      this.setData({ userInfo, isLoggedIn: true });
      wx.showToast({ title: '登录成功', icon: 'success' });
    } catch (err) {
      console.error('Login failed:', err);
      wx.showToast({ title: '登录失败', icon: 'none' });
    }
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
