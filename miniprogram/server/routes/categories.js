const express = require('express');
const { categories } = require('../data/mockData');

const router = express.Router();

// GET /api/categories - 获取分类列表
router.get('/', (req, res) => {
  try {
    res.json({
      data: categories,
      meta: { timestamp: new Date().toISOString() },
    });
  } catch (err) {
    console.error('Get categories error:', err);
    res.status(500).json({ error: '获取分类列表失败', code: 'GET_CATEGORIES_FAILED' });
  }
});

module.exports = router;
