// pages/points/index.js
const app = getApp()

Page({
  data: {
    loading: true,
    account: null,
    transactions: [],
    currentTab: 'all',
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
      const [accountRes, historyRes] = await Promise.all([
        app.request({ url: '/points/account' }),
        app.request({ url: '/points/history' }),
      ])
      const allTransactions = historyRes || []
      this.setData({
        account: accountRes,
        allTransactions: allTransactions,
        transactions: allTransactions,
        loading: false,
        currentTab: 'all',
      })
    } catch (err) {
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    const { allTransactions } = this.data
    let filtered = allTransactions
    if (tab === 'earn') {
      filtered = allTransactions.filter(t => t.type === 'EARN')
    } else if (tab === 'spend') {
      filtered = allTransactions.filter(t => t.type === 'SPEND')
    }
    this.setData({ currentTab: tab, transactions: filtered })
  },

  getFilteredTransactions() {
    const { transactions, currentTab } = this.data
    if (currentTab === 'all') return transactions
    if (currentTab === 'earn') return transactions.filter(t => t.type === 'EARN')
    if (currentTab === 'spend') return transactions.filter(t => t.type === 'SPEND')
    return transactions
  },

  goToSignIn() {
    wx.showLoading({ title: '签到中...', mask: true })
    app.request({
      url: '/points/sign-in',
      method: 'POST',
    }).then(res => {
      wx.hideLoading()
      wx.showToast({ title: '签到成功，获得10积分', icon: 'success' })
      this.loadPointsData()
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({ title: err.message || '签到失败', icon: 'none' })
    })
  },
})