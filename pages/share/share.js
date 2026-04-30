const { request, requestWithLoading } = require('../../utils/request.js');
const { getSharePoster, getShareStatistics, claimShareReward } = require('../../utils/api.js');

Page({
  data: {
    shareConfig: null,
    statistics: null,
    posterUrl: '',
    isGenerating: false,
    isClaiming: false,
    canClaim: false,
    rewardInfo: null,
  },

  onLoad() {
    this.loadShareConfig();
    this.loadShareStatistics();
  },

  onShow() {
    this.loadShareStatistics();
  },

  async loadShareConfig() {
    try {
      const config = await request({
        url: '/api/share/config',
        method: 'GET',
      });
      this.setData({ shareConfig: config });
      this.generatePoster(config);
    } catch (err) {
      console.error('Failed to load share config:', err);
      this.setData({
        shareConfig: {
          title: '分享有礼',
          description: '分享给好友，好友下单即得优惠券',
          rewardAmount: 10,
          rewardDesc: '下单返10元优惠券',
          posterBgUrl: 'https://picsum.photos/750/1200?random=share',
          minOrderAmount: 100,
        }
      });
      this.generatePoster(this.data.shareConfig);
    }
  },

  async loadShareStatistics() {
    try {
      const statistics = await request({
        url: '/api/share/statistics',
        method: 'GET',
      });
      this.setData({
        statistics,
        canClaim: statistics.canClaim,
        rewardInfo: statistics.rewardInfo,
      });
    } catch (err) {
      console.error('Failed to load share statistics:', err);
      this.setData({
        statistics: {
          totalShares: 0,
          validShares: 0,
          rewardEarned: 0,
          canClaim: false,
        }
      });
    }
  },

  async generatePoster(config) {
    if (!config) return;

    this.setData({ isGenerating: true });

    try {
      const posterData = await request({
        url: '/api/share/poster',
        method: 'POST',
        data: {
          productId: config.productId || null,
          activityId: config.activityId,
        },
      });

      this.setData({
        posterUrl: posterData.posterUrl,
        isGenerating: false,
      });
    } catch (err) {
      console.error('Failed to generate poster:', err);
      this.setData({
        posterUrl: 'https://picsum.photos/750/1200?random=poster',
        isGenerating: false,
      });
    }
  },

  async savePoster() {
    if (!this.data.posterUrl) {
      wx.showToast({ title: '请先生成海报', icon: 'none' });
      return;
    }

    try {
      const imageUrl = this.data.posterUrl;
      wx.showLoading({ title: '保存中...' });

      const downloadResult = await wx.downloadFile({ url: imageUrl });

      if (downloadResult.statusCode === 200) {
        await wx.saveImageToPhotosAlbum({
          filePath: downloadResult.tempFilePath,
        });
        wx.hideLoading();
        wx.showToast({ title: '海报已保存', icon: 'success' });
      }
    } catch (err) {
      wx.hideLoading();
      if (err.errMsg.includes('auth deny')) {
        wx.showModal({
          title: '提示',
          content: '需要您授权保存图片到相册',
          success: (res) => {
            if (res.confirm) {
              wx.openSetting();
            }
          },
        });
      } else {
        wx.showToast({ title: '保存失败', icon: 'none' });
      }
    }
  },

  async shareToFriend() {
    if (!this.data.shareConfig) return;

    try {
      await wx.showShareMenu({
        withShareTicket: true,
      });
      wx.showToast({ title: '分享成功', icon: 'success' });
      setTimeout(() => {
        this.loadShareStatistics();
      }, 2000);
    } catch (err) {
      console.error('Failed to share:', err);
    }
  },

  onShareAppMessage() {
    const { shareConfig } = this.data;
    return {
      title: shareConfig?.title || '分享有礼',
      path: `/pages/index/index?from=share&shareId=${wx.getStorageSync('user_info')?.id || ''}`,
      imageUrl: this.data.posterUrl,
    };
  },

  onShareTimeline() {
    const { shareConfig } = this.data;
    return {
      title: shareConfig?.title || '分享有礼',
      query: `from=share&shareId=${wx.getStorageSync('user_info')?.id || ''}`,
    };
  },

  async claimReward() {
    if (!this.data.canClaim) {
      wx.showToast({ title: '暂无可领取奖励', icon: 'none' });
      return;
    }

    this.setData({ isClaiming: true });

    try {
      const result = await requestWithLoading({
        url: '/api/share/claim-reward',
        method: 'POST',
      });

      wx.showModal({
        title: '领取成功',
        content: `恭喜获得${result.rewardAmount}元优惠券`,
        showCancel: false,
        success: () => {
          this.loadShareStatistics();
        },
      });
    } catch (err) {
      wx.showToast({ title: '领取失败', icon: 'none' });
    } finally {
      this.setData({ isClaiming: false });
    }
  },

  goCoupon() {
    wx.navigateTo({ url: '/pages/coupon/coupon' });
  },

  previewPoster() {
    if (!this.data.posterUrl) return;
    wx.previewImage({
      urls: [this.data.posterUrl],
      current: this.data.posterUrl,
    });
  },
});
