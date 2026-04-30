const { request, requestWithLoading } = require('../../utils/request.js');
const { getPointsInfo, getPointsHistory, redeemPoints } = require('../../utils/api.js');

Page({
  data: {
    pointsInfo: null,
    historyList: [],
    isLoading: false,
    isRedeeming: false,
    activeTab: 'all',
    showRedeemModal: false,
    selectedReward: null,
    rewardOptions: [
      { id: 'reward_1', name: '5元优惠券', desc: '无门槛使用', requiredPoints: 500 },
      { id: 'reward_2', name: '10元优惠券', desc: '满50元使用', requiredPoints: 1000 },
      { id: 'reward_3', name: '20元优惠券', desc: '满100元使用', requiredPoints: 2000 },
    ],
  },

  onLoad() {
    this.loadPointsInfo();
    this.loadPointsHistory();
  },

  onShow() {
    this.loadPointsInfo();
  },

  onPullDownRefresh() {
    this.loadPointsInfo();
    this.loadPointsHistory();
    wx.stopPullDownRefresh();
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ activeTab: tab });
    this.loadPointsHistory();
  },

  async loadPointsInfo() {
    try {
      const result = await request({
        url: '/api/points/info',
        method: 'GET',
      });

      this.setData({
        pointsInfo: result,
      });
    } catch (err) {
      console.error('Failed to load points info:', err);
      this.setData({
        pointsInfo: {
          balance: 0,
          totalEarned: 0,
          totalSpent: 0,
          level: 0,
          levelName: '普通会员',
          nextLevelPoints: 1000,
          levelProgress: 0,
        }
      });
    }
  },

  async loadPointsHistory() {
    this.setData({ isLoading: true });

    try {
      const result = await request({
        url: '/api/points/history',
        method: 'GET',
        data: { type: this.data.activeTab === 'all' ? '' : this.data.activeTab },
      });

      this.setData({
        historyList: result.list || [],
        isLoading: false,
      });
    } catch (err) {
      console.error('Failed to load points history:', err);
      this.setData({
        historyList: [],
        isLoading: false,
      });
    }
  },

  showRedeemModal() {
    if (!this.data.pointsInfo || this.data.pointsInfo.balance <= 0) {
      wx.showToast({ title: '积分不足', icon: 'none' });
      return;
    }
    this.setData({ showRedeemModal: true });
  },

  hideRedeemModal() {
    this.setData({ showRedeemModal: false, selectedReward: null });
  },

  selectReward(e) {
    const reward = e.currentTarget.dataset.reward;
    if (reward.requiredPoints > this.data.pointsInfo.balance) {
      wx.showToast({ title: '积分不足', icon: 'none' });
      return;
    }
    this.setData({ selectedReward: reward });
  },

  async confirmRedeem() {
    if (!this.data.selectedReward) {
      wx.showToast({ title: '请选择奖励', icon: 'none' });
      return;
    }

    this.setData({ isRedeeming: true });

    try {
      const result = await requestWithLoading({
        url: '/api/points/redeem',
        method: 'POST',
        data: {
          rewardId: this.data.selectedReward.id,
          points: this.data.selectedReward.requiredPoints,
        },
      });

      wx.showModal({
        title: '兑换成功',
        content: `恭喜您成功兑换${this.data.selectedReward.name}`,
        showCancel: false,
        success: () => {
          this.hideRedeemModal();
          this.loadPointsInfo();
          this.loadPointsHistory();
        },
      });
    } catch (err) {
      wx.showToast({ title: '兑换失败', icon: 'none' });
    } finally {
      this.setData({ isRedeeming: false });
    }
  },

  goCoupon() {
    wx.navigateTo({ url: '/pages/coupon/coupon' });
  },

  getHistoryTypeText(type) {
    const typeMap = {
      earn: '获得',
      spend: '消耗',
      expire: '过期',
      reward: '奖励',
    };
    return typeMap[type] || '';
  },

  getHistoryChangeText(item) {
    if (item.type === 'spend' || item.type === 'expire') {
      return `-${item.points}`;
    }
    return `+${item.points}`;
  },

  formatDate(timestamp) {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  },
});
