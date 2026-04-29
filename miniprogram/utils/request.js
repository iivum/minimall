const BASE_URL = 'http://localhost:3000';

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header,
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          wx.showToast({ title: '登录已过期', icon: 'none' });
        } else {
          reject(new Error(res.data.message || '请求失败'));
        }
      },
      fail: (err) => {
        reject(new Error('网络异常，请检查网络连接'));
      },
    });
  });
};

const requestWithLoading = async (options) => {
  wx.showLoading({ title: '加载中...', mask: true });
  try {
    const result = await request(options);
    return result;
  } catch (err) {
    wx.showToast({ title: err.message, icon: 'none' });
    throw err;
  } finally {
    wx.hideLoading();
  }
};

module.exports = { request, requestWithLoading };
