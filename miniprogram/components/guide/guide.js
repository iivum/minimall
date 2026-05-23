// components/guide/guide.js
Component({
  properties: {
    visible: {
      type: Boolean,
      value: false,
    },
  },

  methods: {
    onSkip() {
      this.triggerEvent('skip')
    },
    onFinish() {
      this.triggerEvent('finish')
    },
  },
})