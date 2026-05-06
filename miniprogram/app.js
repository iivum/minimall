// app.js
const APP_ID = 'YOUR_APPID'
const BASE_URL = 'http://localhost:8080/api'

App({
  globalData: {
    userId: null,
    token: null,
    openid: null,
    baseUrl: BASE_URL,
  },

  onLaunch() {
    this.checkLoginStatus()
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token')
    const userId = wx.getStorageSync('userId')
    if (token && userId) {
      this.globalData.token = token
      this.globalData.userId = userId
    }
  },

  login(code) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.globalData.baseUrl}/auth/login`,
        method: 'POST',
        data: { openid: code },
        success: (res) => {
          if (res.statusCode === 200) {
            const { token, userId } = res.data
            this.globalData.token = token
            this.globalData.userId = userId
            wx.setStorageSync('token', token)
            wx.setStorageSync('userId', userId)
            resolve({ token, userId })
          } else {
            reject(new Error(res.data.message || '登录失败'))
          }
        },
        fail: reject,
      })
    })
  },

  register(userInfo) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.globalData.baseUrl}/auth/register`,
        method: 'POST',
        data: userInfo,
        success: (res) => {
          if (res.statusCode === 200) {
            const { token, userId } = res.data
            this.globalData.token = token
            this.globalData.userId = userId
            wx.setStorageSync('token', token)
            wx.setStorageSync('userId', userId)
            resolve({ token, userId })
          } else {
            reject(new Error(res.data.message || '注册失败'))
          }
        },
        fail: reject,
      })
    })
  },

  logout() {
    this.globalData.token = null
    this.globalData.userId = null
    this.globalData.openid = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('userId')
    wx.removeStorageSync('openid')
  },

  request(options) {
    const token = this.globalData.token
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.globalData.baseUrl}${options.url}`,
        method: options.method || 'GET',
        data: options.data,
        header: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : '',
          ...options.header,
        },
        success: (res) => {
          if (res.statusCode === 200) {
            resolve(res.data)
          } else if (res.statusCode === 401) {
            this.handleAuthError().then(() => {
              this.request(options).then(resolve).catch(reject)
            }).catch(reject)
          } else {
            reject(new Error(res.data.message || '请求失败'))
          }
        },
        fail: reject,
      })
    })
  },

  async handleAuthError() {
    try {
      const code = await wx.login()
      await this.login(code)
    } catch (err) {
      throw err
    }
  },
})