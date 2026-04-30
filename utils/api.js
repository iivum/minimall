const { request, requestWithLoading } = require('./request.js');
const { API_BASE_URL } = require('../constants/index.js');

const STORAGE_KEYS = {
  USER_INFO: 'user_info',
  CART_LIST: 'cart_list',
  PRODUCT_CACHE: 'product_cache',
};

// ============ Auth APIs ============

const authLogin = (code) => {
  return request({
    url: '/api/auth/login',
    method: 'POST',
    data: { code },
  });
};

const authRegister = (userInfo) => {
  return request({
    url: '/api/auth/register',
    method: 'POST',
    data: userInfo,
  });
};

const getUserInfo = () => {
  return request({
    url: '/api/user/info',
    method: 'GET',
  });
};

// ============ Product APIs ============

const getBanners = () => {
  return request({
    url: '/api/home/banners',
    method: 'GET',
  });
};

const getCategories = () => {
  return request({
    url: '/api/category/list',
    method: 'GET',
  });
};

const getProductList = (params) => {
  return request({
    url: '/api/product/list',
    method: 'GET',
    data: params,
  });
};

const getProductDetail = (id) => {
  return request({
    url: `/api/product/detail/${id}`,
    method: 'GET',
  });
};

const getRecommendProducts = () => {
  return request({
    url: '/api/product/recommend',
    method: 'GET',
  });
};

const getFlashSaleProducts = () => {
  return request({
    url: '/api/product/flash-sale',
    method: 'GET',
  });
};

// ============ Cart APIs ============

const getCartList = () => {
  return request({
    url: '/api/cart/list',
    method: 'GET',
  });
};

const addToCart = (productId, count) => {
  return request({
    url: '/api/cart/add',
    method: 'POST',
    data: { productId, count },
  });
};

const updateCartItem = (cartItemId, count) => {
  return request({
    url: '/api/cart/update',
    method: 'POST',
    data: { cartItemId, count },
  });
};

const removeCartItem = (cartItemId) => {
  return request({
    url: '/api/cart/remove',
    method: 'POST',
    data: { cartItemId },
  });
};

const clearCart = () => {
  return request({
    url: '/api/cart/clear',
    method: 'POST',
  });
};

// ============ Order APIs ============

const createOrder = (cartItemIds, addressId) => {
  return request({
    url: '/api/order/create',
    method: 'POST',
    data: { cartItemIds, addressId },
  });
};

const getOrderList = (status) => {
  return request({
    url: '/api/order/list',
    method: 'GET',
    data: { status },
  });
};

const getOrderDetail = (orderId) => {
  return request({
    url: `/api/order/detail/${orderId}`,
    method: 'GET',
  });
};

const payOrder = (orderId) => {
  return request({
    url: '/api/order/pay',
    method: 'POST',
    data: { orderId },
  });
};

// ============ Share APIs ============

const getShareConfig = () => {
  return request({
    url: '/api/share/config',
    method: 'GET',
  });
};

const getSharePoster = (data) => {
  return request({
    url: '/api/share/poster',
    method: 'POST',
    data,
  });
};

const getShareStatistics = () => {
  return request({
    url: '/api/share/statistics',
    method: 'GET',
  });
};

const claimShareReward = () => {
  return request({
    url: '/api/share/claim-reward',
    method: 'POST',
  });
};

// ============ Coupon APIs ============

const getCouponList = (params) => {
  return request({
    url: '/api/coupon/list',
    method: 'GET',
    data: params,
  });
};

const getNewUserCoupon = () => {
  return request({
    url: '/api/coupon/new-user',
    method: 'GET',
  });
};

const claimCoupon = (couponId) => {
  return request({
    url: '/api/coupon/claim',
    method: 'POST',
    data: { couponId },
  });
};

// ============ Points APIs ============

const getPointsInfo = () => {
  return request({
    url: '/api/points/info',
    method: 'GET',
  });
};

const getPointsHistory = (params) => {
  return request({
    url: '/api/points/history',
    method: 'GET',
    data: params,
  });
};

const redeemPoints = (data) => {
  return request({
    url: '/api/points/redeem',
    method: 'POST',
    data,
  });
};

// ============ Local Cache ============

const setLocalCache = (key, data) => {
  try {
    wx.setStorageSync(key, data);
  } catch (e) {
    console.error('Storage set error:', e);
  }
};

const getLocalCache = (key, defaultValue = null) => {
  try {
    return wx.getStorageSync(key) || defaultValue;
  } catch (e) {
    console.error('Storage get error:', e);
    return defaultValue;
  }
};

const removeLocalCache = (key) => {
  try {
    wx.removeStorageSync(key);
  } catch (e) {
    console.error('Storage remove error:', e);
  }
};

const cacheUserInfo = (userInfo) => {
  setLocalCache(STORAGE_KEYS.USER_INFO, userInfo);
};

const getCachedUserInfo = () => {
  return getLocalCache(STORAGE_KEYS.USER_INFO);
};

const cacheCartList = (cartList) => {
  setLocalCache(STORAGE_KEYS.CART_LIST, cartList);
};

const getCachedCartList = () => {
  return getLocalCache(STORAGE_KEYS.CART_LIST, []);
};

const cacheProduct = (product) => {
  const cache = getLocalCache(STORAGE_KEYS.PRODUCT_CACHE, {});
  cache[product.id] = product;
  setLocalCache(STORAGE_KEYS.PRODUCT_CACHE, cache);
};

const getCachedProduct = (productId) => {
  const cache = getLocalCache(STORAGE_KEYS.PRODUCT_CACHE, {});
  return cache[productId];
};

module.exports = {
  // Auth
  authLogin,
  authRegister,
  getUserInfo,
  // Products
  getBanners,
  getCategories,
  getProductList,
  getProductDetail,
  getRecommendProducts,
  getFlashSaleProducts,
  // Cart
  getCartList,
  addToCart,
  updateCartItem,
  removeCartItem,
  clearCart,
  // Orders
  createOrder,
  getOrderList,
  getOrderDetail,
  payOrder,
  // Share
  getShareConfig,
  getSharePoster,
  getShareStatistics,
  claimShareReward,
  // Coupon
  getCouponList,
  getNewUserCoupon,
  claimCoupon,
  // Points
  getPointsInfo,
  getPointsHistory,
  redeemPoints,
  // Cache
  cacheUserInfo,
  getCachedUserInfo,
  cacheCartList,
  getCachedCartList,
  cacheProduct,
  getCachedProduct,
  STORAGE_KEYS,
};
