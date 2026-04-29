Page({
  data: {
    cartList: [
      { id: 1, name: '经典款连衣裙', price: '299.00', count: 1, selected: true, image: 'https://picsum.photos/200/200?random=40' },
      { id: 2, name: '无线蓝牙耳机', price: '99.00', count: 2, selected: true, image: 'https://picsum.photos/200/200?random=41' },
    ],
    allSelected: true,
    totalPrice: '497.00',
  },
  onLoad() {},
  toggleSelect(e) {
    const index = e.currentTarget.dataset.index;
    const list = this.data.cartList;
    list[index].selected = !list[index].selected;
    this.setData({ cartList: list });
    this.calcTotal();
  },
  toggleAllSelect() {
    const allSelected = !this.data.allSelected;
    const list = this.data.cartList.map(item => ({ ...item, selected: allSelected }));
    this.setData({ cartList: list, allSelected });
    this.calcTotal();
  },
  addCount(e) {
    const index = e.currentTarget.dataset.index;
    const list = this.data.cartList;
    list[index].count++;
    this.setData({ cartList: list });
    this.calcTotal();
  },
  reduceCount(e) {
    const index = e.currentTarget.dataset.index;
    const list = this.data.cartList;
    if (list[index].count > 1) {
      list[index].count--;
      this.setData({ cartList: list });
      this.calcTotal();
    }
  },
  calcTotal() {
    let total = 0;
    this.data.cartList.forEach(item => {
      if (item.selected) total += parseFloat(item.price) * item.count;
    });
    this.setData({ totalPrice: total.toFixed(2) });
  },
  checkout() {
    wx.showToast({ title: '正在结算...', icon: 'none' });
  },
});
