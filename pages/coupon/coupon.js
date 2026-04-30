const { request, requestWithLoading } = require('../../utils/request.js');
const { getCouponList, getNewUserCoupon, claimCoupon } = require('../../utils/api.js');

Page({
  data: {
    couponList: [],
    newUserCoupon: null,
    activeTab: 'available',
    isLoading: false,
    isClaiming: false,
  },

  onLoad(options) {
    if (options.from === 'share') {
      this.setData({ activeTab: 'share' });
    }
    this.loadCouponList();
    this.loadNewUserCoupon();
  },

  onShow() {
    this.loadCouponList();
    this.loadNewUserCoupon();
  },

  onPullDownRefresh() {
    this.loadCouponList();
    this.loadNewUserCoupon();
    wx.stopPullDownRefresh();
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ activeTab: tab });
    this.loadCouponList();
  },

  async loadCouponList() {
    this.setData({ isLoading: true });

    try {
      const statusMap = {
        available: 'available',
        used: 'used',
        expired: 'expired',
      };

      const result = await request({
        url: '/api/coupon/list',
        method: 'GET',
        data: { status: statusMap[this.data.activeTab] },
      });

      this.setData({
        couponList: result.list || [],
        isLoading: false,
      });
    } catch (err) {
      console.error('Failed to load coupon list:', err);
      this.setData({
        couponList: [],
        isLoading: false,
      });
    }
  },

  async loadNewUserCoupon() {
    try {
      const result = await request({
        url: '/api/coupon/new-user',
        method: 'GET',
      });

      this.setData({
        newUserCoupon: result.coupon,
      });
    } catch (err) {
      console.error('Failed to load new user coupon:', err);
      this.setData({
        newUserCoupon: {
          id: 'new_user_coupon_001',
          name: '新人专属礼包',
          desc: '满100减20元优惠券',
          amount: 20,
          minAmount: 100,
          status: 'available',
          expireTime: '2026-12-31',
          isNewUser: true,
        }
      });
    }
  },

  async claimNewUserCoupon() {
    if (!this.data.newUserCoupon || this.data.newUserCoupon.status !== 'available') {
      wx.showToast({ title: '暂无可领取', icon: 'none' });
      return;
    }

    this.setData({ isClaiming: true });

    try {
      const result = await requestWithLoading({
        url: '/api/coupon/claim',
        method: 'POST',
        data: { couponId: this.data.newUserCoupon.id },
      });

      wx.showModal({
        title: '领取成功',
        content: `恭喜获得${this.data.newUserCoupon.amount}元优惠券`,
        showCancel: false,
        success: () => {
          this.loadNewUserCoupon();
          this.loadCouponList();
        },
      });
    } catch (err) {
      wx.showToast({ title: '领取失败', icon: 'none' });
    } finally {
      this.setData({ isClaiming: false });
    }
  },

  async useCoupon(e) {
    const coupon = e.currentTarget.dataset.coupon;
    if (!coupon || coupon.status !== 'available') return;

    wx.showModal({
      title: '使用优惠券',
      content: `优惠券面额${coupon.amount}元，是否前往下单？`,
      success: (res) => {
        if (res.confirm) {
          wx.switchTab({ url: '/pages/index/index' });
        }
      },
    });
  },

  goShare() {
    wx.navigateTo({ url: '/pages/share/share' });
  },

  getCouponStatusText(coupon) {
    if (coupon.isNewUser && coupon.status === 'available') {
      return '待领取';
    }
    const statusMap = {
      available: '待使用',
      used: '已使用',
      expired: '已过期',
    };
    return statusMap[coupon.status] || '';
  },

  isCouponExpiringSoon(coupon) {
    if (!coupon.expireTime || coupon.status !== 'available') return false;
    const now = new Date();
    const expire = new Date(coupon.expireTime);
    const daysLeft = (expire - now) / (1000 * 60 * 60 * 24);
    return daysLeft <= 3;
  },
});
