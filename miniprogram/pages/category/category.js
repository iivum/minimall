Page({
  data: {
    categories: [
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
    ],
    currentIndex: 0,
  },
  onLoad() {},
  switchCategory(e) {
    this.setData({ currentIndex: e.currentTarget.dataset.index });
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` });
  },
});
