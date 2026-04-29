const express = require('express');
const { products } = require('../data/mockData');

const router = express.Router();

// GET /api/products - 获取商品列表
router.get('/', (req, res) => {
  try {
    const { categoryId, keyword, limit = 50, offset = 0 } = req.query;

    let filteredProducts = [...products];

    // Filter by category
    if (categoryId) {
      filteredProducts = filteredProducts.filter(p => p.categoryId === parseInt(categoryId));
    }

    // Filter by keyword (search in name)
    if (keyword) {
      const lowerKeyword = keyword.toLowerCase();
      filteredProducts = filteredProducts.filter(p =>
        p.name.toLowerCase().includes(lowerKeyword)
      );
    }

    // Pagination
    const total = filteredProducts.length;
    const paginatedProducts = filteredProducts.slice(
      parseInt(offset),
      parseInt(offset) + parseInt(limit)
    );

    res.json({
      data: paginatedProducts,
      meta: {
        total,
        limit: parseInt(limit),
        offset: parseInt(offset),
        timestamp: new Date().toISOString(),
      },
    });
  } catch (err) {
    console.error('Get products error:', err);
    res.status(500).json({ error: '获取商品列表失败', code: 'GET_PRODUCTS_FAILED' });
  }
});

// GET /api/products/:id - 获取商品详情
router.get('/:id', (req, res) => {
  try {
    const { id } = req.params;
    const product = products.find(p => p.id === parseInt(id));

    if (!product) {
      return res.status(404).json({ error: '商品不存在', code: 'PRODUCT_NOT_FOUND' });
    }

    res.json({
      data: product,
      meta: { timestamp: new Date().toISOString() },
    });
  } catch (err) {
    console.error('Get product error:', err);
    res.status(500).json({ error: '获取商品详情失败', code: 'GET_PRODUCT_FAILED' });
  }
});

module.exports = router;
