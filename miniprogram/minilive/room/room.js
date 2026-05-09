// minilive/room/room.js
const app = getApp()

Page({
  data: {
    roomId: '',
    roomInfo: null,
    goods: [],
    comments: [],
    commentText: '',
    likeCount: 0,
    isLiked: false,
    loading: true,
    showGoods: false,
  },

  onLoad(options) {
    this.setData({ roomId: options.roomId })
    this.loadRoomInfo()
    this.connectCommentSocket()
  },

  onUnload() {
    this.closeCommentSocket()
  },

  async loadRoomInfo() {
    try {
      const roomInfo = await app.request({ url: `/lives/${this.data.roomId}` })
      const goods = await app.request({ url: `/lives/${this.data.roomId}/goods` })
      this.setData({ roomInfo, goods, loading: false })
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  connectCommentSocket() {
    const ws = wx.connectSocket({
      url: `wss://${app.globalData.baseUrl.replace('http://', '').replace('https://', '')}/lives/${this.data.roomId}/comments`,
    })

    ws.onSocketOpen(() => {
      console.log('评论 socket 已连接')
    })

    ws.onSocketMessage((res) => {
      try {
        const comment = JSON.parse(res.data)
        this.setData({
          comments: [...this.data.comments.slice(-49), comment],
        })
      } catch (e) {}
    })

    ws.onSocketClose(() => {
      console.log('评论 socket 已关闭')
    })

    this.socketTask = ws
  },

  closeCommentSocket() {
    if (this.socketTask) {
      this.socketTask.close()
    }
  },

  onCommentInput(e) {
    this.setData({ commentText: e.detail.value })
  },

  sendComment() {
    const text = this.data.commentText.trim()
    if (!text) return

    if (this.socketTask) {
      this.socketTask.send({
        data: JSON.stringify({ content: text }),
      })
    }
    this.setData({ commentText: '' })
  },

  toggleLike() {
    const isLiked = !this.data.isLiked
    const likeCount = isLiked ? this.data.likeCount + 1 : this.data.likeCount - 1
    this.setData({ isLiked, likeCount })

    app.request({
      url: `/lives/${this.data.roomId}/like`,
      method: 'POST',
      data: { isLiked },
    }).catch(() => {
      this.setData({ isLiked: !isLiked, likeCount: likeCount })
    })
  },

  showGoodsList() {
    this.setData({ showGoods: true })
  },

  hideGoodsList() {
    this.setData({ showGoods: false })
  },

  goToGoods(e) {
    const { goodsId } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/product/detail?id=${goodsId}` })
  },
})
