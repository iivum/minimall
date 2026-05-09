// minilive/components/goods-list/goods-list.js
Component({
  properties: {
    goods: {
      type: Array,
      value: [],
    },
    visible: {
      type: Boolean,
      value: false,
    },
  },

  methods: {
    hideGoods() {
      this.triggerEvent('hide')
    },

    goToGoods(e) {
      const { goodsId } = e.currentTarget.dataset
      wx.navigateTo({ url: `/pages/product/detail?id=${goodsId}` })
    },
  },
})
