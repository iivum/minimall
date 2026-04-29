Component({
  properties: {
    item: { type: Object, value: {} },
    index: { type: Number, value: 0 },
  },
  methods: {
    toggleSelect() {
      this.triggerEvent('toggleSelect', { index: this.properties.index });
    },
    addCount() {
      this.triggerEvent('addCount', { index: this.properties.index });
    },
    reduceCount() {
      this.triggerEvent('reduceCount', { index: this.properties.index });
    },
    deleteItem() {
      this.triggerEvent('deleteItem', { index: this.properties.index });
    },
  },
});
