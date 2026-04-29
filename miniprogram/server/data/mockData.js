// Mock users data - in production, use a real database
const users = [
  {
    id: 'user_001',
    username: 'demo',
    password: '$2a$10$8K1p/a0dR1xqM8K3cYq1wOqu1L2c0J0eX9H5J2kG1xqM8K3cYq1wO', // password: demo123
    nickname: '演示用户',
    avatar: 'https://picsum.photos/200/200?random=60',
    createdAt: '2024-01-01T00:00:00Z',
  },
];

// Categories
const categories = [
  { id: 1, name: '服装', icon: '👕', color: '#ff6b6b', children: [
    { id: 11, name: '女装' }, { id: 12, name: '男装' }, { id: 13, name: '童装' }
  ]},
  { id: 2, name: '数码', icon: '📱', color: '#4ecdc4', children: [
    { id: 21, name: '手机' }, { id: 22, name: '电脑' }, { id: 23, name: '配件' }
  ]},
  { id: 3, name: '食品', icon: '🍎', color: '#ffe66d', children: [
    { id: 31, name: '水果' }, { id: 32, name: '零食' }, { id: 33, name: '粮油' }
  ]},
  { id: 4, name: '美妆', icon: '💄', color: '#ff8cc8', children: [
    { id: 41, name: '护肤' }, { id: 42, name: '彩妆' }, { id: 43, name: '香水' }
  ]},
  { id: 5, name: '家居', icon: '🏠', color: '#88d8b0', children: [
    { id: 51, name: '家具' }, { id: 52, name: '家纺' }, { id: 53, name: '餐具' }
  ]},
  { id: 6, name: '运动', icon: '⚽', color: '#6c5ce7', children: [
    { id: 61, name: '健身' }, { id: 62, name: '球类' }, { id: 63, name: '户外' }
  ]},
  { id: 7, name: '图书', icon: '📚', color: '#fd79a8', children: [
    { id: 71, name: '文学' }, { id: 72, name: '科技' }, { id: 73, name: '童书' }
  ]},
  { id: 8, name: '母婴', icon: '🍼', color: '#74b9ff', children: [
    { id: 81, name: '奶粉' }, { id: 82, name: '纸尿裤' }, { id: 83, name: '玩具' }
  ]},
];

// Products
const products = [
  { id: 1, name: '经典款连衣裙', price: 299.00, originalPrice: 599.00, image: 'https://picsum.photos/300/300?random=20', categoryId: 11, stock: 100, description: '优雅时尚，适合多种场合' },
  { id: 2, name: '运动休闲鞋', price: 199.00, originalPrice: 399.00, image: 'https://picsum.photos/300/300?random=21', categoryId: 62, stock: 80, description: '轻便舒适，时尚百搭' },
  { id: 3, name: '保湿面霜', price: 159.00, originalPrice: 320.00, image: 'https://picsum.photos/300/300?random=22', categoryId: 41, stock: 200, description: '深层保湿，焕发肌肤活力' },
  { id: 4, name: '智能台灯', price: 89.00, originalPrice: 169.00, image: 'https://picsum.photos/300/300?random=23', categoryId: 51, stock: 150, description: '护眼LED，多档调光' },
  { id: 5, name: '婴儿奶粉', price: 268.00, originalPrice: 368.00, image: 'https://picsum.photos/300/300?random=24', categoryId: 81, stock: 50, description: '进口奶源，营养均衡' },
  { id: 6, name: '双肩背包', price: 129.00, originalPrice: 259.00, image: 'https://picsum.photos/300/300?random=25', categoryId: 63, stock: 120, description: '大容量，多功能背包' },
  { id: 7, name: '无线蓝牙耳机', price: 99.00, originalPrice: 199.00, image: 'https://picsum.photos/200/200?random=10', categoryId: 23, stock: 200, description: '真无线，降噪设计' },
  { id: 8, name: '智能手环', price: 149.00, originalPrice: 299.00, image: 'https://picsum.photos/200/200?random=11', categoryId: 23, stock: 150, description: '心率监测，睡眠追踪' },
  { id: 9, name: '保温杯', price: 49.00, originalPrice: 89.00, image: 'https://picsum.photos/200/200?random=12', categoryId: 53, stock: 300, description: '24小时保温，保冷保热' },
];

// Orders
const orders = [];

module.exports = { users, categories, products, orders };
