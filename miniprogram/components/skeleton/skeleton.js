// components/skeleton/skeleton.js
Component({
  properties: {
    type: {
      type: String,
      value: 'product-list',
    },
    loading: {
      type: Boolean,
      value: true,
    },
  },

  data: {
    items: [1, 2, 3, 4, 5, 6],
  },
})