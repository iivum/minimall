const { request } = require('../../utils/request.js');

const DEFAULT_CATEGORIES = [
  { id: 1, name: '服装', children: [
    { id: 11, name: '女装' }, { id: 12, name: '男装' }, { id: 13, name: '童装' }
  ]},
  { id: 2, name: '数码', children: [
    { id: 21, name: '手机' }, { id: 22, name: '电脑' }, { id: 23, name: '配件' }
  ]},
  { id: 3, name: '食品', children: [
    { id: 31, name: '水果' }, { id: 32, name: '零食' }, { id: 33, name: '粮油' }
  ]},
  { id: 4, name: '美妆', children: [
    { id: 41, name: '护肤' }, { id: 42, name: '彩妆' }, { id: 43, name: '香水' }
  ]},
  { id: 5, name: '家居', children: [
    { id: 51, name: '家具' }, { id: 52, name: '家纺' }, { id: 53, name: '餐具' }
  ]},
  { id: 6, name: '运动', children: [
    { id: 61, name: '健身' }, { id: 62, name: '球类' }, { id: 63, name: '户外' }
  ]},
];

Page({
  data: {
    categories: DEFAULT_CATEGORIES,
    currentIndex: 0,
    products: [],
    loading: false,
    hasMore: true,
    page: 0,
    pageSize: 10,
  },

  onLoad() {
    this.loadInitialProducts();
  },

  onReachBottom() {
    if (!this.data.loading && this.data.hasMore) {
      this.loadMoreProducts();
    }
  },

  async loadInitialProducts() {
    const firstCategory = DEFAULT_CATEGORIES[0];
    if (firstCategory && firstCategory.children.length > 0) {
      await this.loadProductsBySubCategory(firstCategory.children[0].id);
    }
  },

  async switchCategory(e) {
    const index = e.currentTarget.dataset.index;
    const category = this.data.categories[index];
    this.setData({ currentIndex: index, products: [], page: 0, hasMore: true });

    if (category && category.children.length > 0) {
      await this.loadProductsBySubCategory(category.children[0].id);
    }
  },

  async loadProductsBySubCategory(subCategoryId) {
    if (this.data.loading) return;
    this.setData({ loading: true });

    try {
      const res = await request({
        url: '/api/products',
        data: {
          categoryId: subCategoryId,
          limit: this.data.pageSize,
          offset: this.data.page * this.data.pageSize,
        },
      });

      const newProducts = res.data || [];
      this.setData({
        loading: false,
        products: this.data.page === 0 ? newProducts : [...this.data.products, ...newProducts],
        hasMore: newProducts.length >= this.data.pageSize,
        page: this.data.page + 1,
      });
    } catch (err) {
      this.setData({ loading: false });
      console.error('Load products error:', err);
    }
  },

  async loadMoreProducts() {
    const currentCategory = this.data.categories[this.data.currentIndex];
    if (currentCategory && currentCategory.children.length > 0) {
      await this.loadProductsBySubCategory(currentCategory.children[0].id);
    }
  },

  async switchSubCategory(e) {
    const subCategoryId = e.currentTarget.dataset.id;
    this.setData({ products: [], page: 0, hasMore: true });
    await this.loadProductsBySubCategory(subCategoryId);
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` });
  },
});
