// pages/points/record/index.js
const app = getApp()

Page({
  data: {
    account: { balance: 0 },
    history: [],
    filteredHistory: [],
    activeTab: 'all',
    loading: true,
  },

  onLoad() {
    this.loadPointsData()
  },

  onShow() {
    this.loadPointsData()
  },

  async loadPointsData() {
    this.setData({ loading: true })
    try {
      const userId = app.globalData.userId
      if (!userId) {
        this.setData({ loading: false })
        return
      }
      const [accountRes, historyRes] = await Promise.all([
        app.request({ url: `/points/account/${userId}` }),
        app.request({ url: `/points/history/${userId}` }),
      ])
      this.setData({
        account: accountRes,
        history: historyRes,
        filteredHistory: historyRes,
        loading: false,
      })
    } catch (err) {
      console.error('加载积分数据失败', err)
      this.setData({ loading: false })
    }
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    this.filterHistory(tab)
  },

  filterHistory(tab) {
    let filtered = this.data.history
    if (tab === 'earn') {
      filtered = this.data.history.filter(item => item.type === 'EARN')
    } else if (tab === 'spend') {
      filtered = this.data.history.filter(item => item.type === 'SPEND')
    }
    this.setData({ filteredHistory: filtered })
  },
})