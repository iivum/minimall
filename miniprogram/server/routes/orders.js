const express = require('express');
const { v4: uuidv4 } = require('uuid');
const { orders, products } = require('../data/mockData');

const router = express.Router();

// Order status map
const ORDER_STATUS = {
  PENDING_PAYMENT: '待付款',
  PAID: '待发货',
  SHIPPED: '待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

// POST /api/orders - 创建订单
router.post('/', (req, res) => {
  try {
    const { items, totalPrice, address } = req.body;
    const userId = req.user.userId;

    if (!items || items.length === 0) {
      return res.status(400).json({ error: '订单商品不能为空', code: 'INVALID_INPUT' });
    }

    // Validate products exist and have sufficient stock
    for (const item of items) {
      const product = products.find(p => p.id === item.id);
      if (!product) {
        return res.status(404).json({ error: `商品ID ${item.id} 不存在`, code: 'PRODUCT_NOT_FOUND' });
      }
      if (product.stock < item.count) {
        return res.status(400).json({ error: `商品${product.name}库存不足`, code: 'STOCK_INSUFFICIENT' });
      }
    }

    // Create order
    const order = {
      id: `ORD${Date.now()}`,
      orderNo: `ORD${uuidv4().slice(0, 8).toUpperCase()}`,
      userId,
      items,
      totalPrice,
      address: address || {},
      status: ORDER_STATUS.PENDING_PAYMENT,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
    };

    orders.push(order);

    // Reduce stock
    for (const item of items) {
      const product = products.find(p => p.id === item.id);
      product.stock -= item.count;
    }

    res.status(201).json({
      data: order,
      meta: { timestamp: new Date().toISOString() },
    });
  } catch (err) {
    console.error('Create order error:', err);
    res.status(500).json({ error: '创建订单失败', code: 'CREATE_ORDER_FAILED' });
  }
});

// GET /api/orders - 获取订单列表
router.get('/', (req, res) => {
  try {
    const { status, limit = 50, offset = 0 } = req.query;
    const userId = req.user.userId;

    let filteredOrders = orders.filter(o => o.userId === userId);

    // Filter by status
    if (status) {
      filteredOrders = filteredOrders.filter(o => o.status === status);
    }

    // Sort by createTime descending
    filteredOrders.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));

    // Pagination
    const total = filteredOrders.length;
    const paginatedOrders = filteredOrders.slice(
      parseInt(offset),
      parseInt(offset) + parseInt(limit)
    );

    res.json({
      data: paginatedOrders,
      meta: {
        total,
        limit: parseInt(limit),
        offset: parseInt(offset),
        timestamp: new Date().toISOString(),
      },
    });
  } catch (err) {
    console.error('Get orders error:', err);
    res.status(500).json({ error: '获取订单列表失败', code: 'GET_ORDERS_FAILED' });
  }
});

// GET /api/orders/:id - 获取订单详情
router.get('/:id', (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.user.userId;
    const order = orders.find(o => o.id === id && o.userId === userId);

    if (!order) {
      return res.status(404).json({ error: '订单不存在', code: 'ORDER_NOT_FOUND' });
    }

    res.json({
      data: order,
      meta: { timestamp: new Date().toISOString() },
    });
  } catch (err) {
    console.error('Get order error:', err);
    res.status(500).json({ error: '获取订单详情失败', code: 'GET_ORDER_FAILED' });
  }
});

// PUT /api/orders/:id/status - 更新订单状态
router.put('/:id/status', (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;
    const userId = req.user.userId;

    const order = orders.find(o => o.id === id && o.userId === userId);
    if (!order) {
      return res.status(404).json({ error: '订单不存在', code: 'ORDER_NOT_FOUND' });
    }

    // Validate status transition
    const validStatuses = Object.values(ORDER_STATUS);
    if (!validStatuses.includes(status)) {
      return res.status(400).json({ error: '无效的订单状态', code: 'INVALID_STATUS' });
    }

    order.status = status;
    order.updateTime = new Date().toISOString();

    res.json({
      data: order,
      meta: { timestamp: new Date().toISOString() },
    });
  } catch (err) {
    console.error('Update order status error:', err);
    res.status(500).json({ error: '更新订单状态失败', code: 'UPDATE_STATUS_FAILED' });
  }
});

module.exports = router;
