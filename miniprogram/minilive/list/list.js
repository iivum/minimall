// minilive/list/list.js
const app = getApp()

Page({
  data: {
    lives: [],
    loading: true,
  },

  onLoad() {
    this.loadLives()
  },

  onShow() {
    this.loadLives()
  },

  onPullDownRefresh() {
    this.loadLives().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  async loadLives() {
    this.setData({ loading: true })
    try {
      const lives = await app.request({ url: '/lives' })
      this.setData({ lives, loading: false })
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  goToLiveRoom(e) {
    const { roomId } = e.currentTarget.dataset
    wx.navigateTo({ url: `/minilive/room/room?roomId=${roomId}` })
  },
})
