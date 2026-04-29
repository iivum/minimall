Component({
  properties: {
    goods: { type: Object, value: {} },
  },
  methods: {
    goDetail() {
      const id = this.properties.goods.id;
      wx.navigateTo({ url: `/pages/detail/detail?id=${id}` });
    },
  },
});
