const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const { users } = require('../data/mockData');

const router = express.Router();
const JWT_SECRET = process.env.JWT_SECRET || 'minimall-secret-key-2024';

// POST /api/users/login - 用户登录
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({ error: '用户名和密码不能为空', code: 'INVALID_INPUT' });
    }

    const user = users.find(u => u.username === username);
    if (!user) {
      return res.status(401).json({ error: '用户名或密码错误', code: 'INVALID_CREDENTIALS' });
    }

    // For demo purposes, accept 'demo123' as password (check plain text for demo user)
    const isPasswordValid = username === 'demo' && password === 'demo123'
      ? true
      : await bcrypt.compare(password, user.password);

    if (!isPasswordValid) {
      return res.status(401).json({ error: '用户名或密码错误', code: 'INVALID_CREDENTIALS' });
    }

    const token = jwt.sign(
      { userId: user.id, username: user.username },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    res.json({
      data: {
        token,
        user: {
          id: user.id,
          username: user.username,
          nickname: user.nickname,
          avatar: user.avatar,
        },
      },
      meta: { timestamp: new Date().toISOString() },
    });
  } catch (err) {
    console.error('Login error:', err);
    res.status(500).json({ error: '登录失败', code: 'LOGIN_FAILED' });
  }
});

// POST /api/users/register - 用户注册
router.post('/register', async (req, res) => {
  try {
    const { username, password, nickname } = req.body;

    if (!username || !password) {
      return res.status(400).json({ error: '用户名和密码不能为空', code: 'INVALID_INPUT' });
    }

    if (users.find(u => u.username === username)) {
      return res.status(409).json({ error: '用户名已存在', code: 'USER_EXISTS' });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const newUser = {
      id: `user_${uuidv4().slice(0, 8)}`,
      username,
      password: hashedPassword,
      nickname: nickname || username,
      avatar: `https://picsum.photos/200/200?random=${Math.floor(Math.random() * 100)}`,
      createdAt: new Date().toISOString(),
    };

    users.push(newUser);

    const token = jwt.sign(
      { userId: newUser.id, username: newUser.username },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    res.status(201).json({
      data: {
        token,
        user: {
          id: newUser.id,
          username: newUser.username,
          nickname: newUser.nickname,
          avatar: newUser.avatar,
        },
      },
      meta: { timestamp: new Date().toISOString() },
    });
  } catch (err) {
    console.error('Register error:', err);
    res.status(500).json({ error: '注册失败', code: 'REGISTER_FAILED' });
  }
});

// GET /api/users/info - 获取用户信息
router.get('/info', (req, res) => {
  const token = req.headers.authorization?.replace('Bearer ', '');
  if (!token) {
    return res.status(401).json({ error: '未登录', code: 'UNAUTHORIZED' });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const user = users.find(u => u.id === decoded.userId);

    if (!user) {
      return res.status(404).json({ error: '用户不存在', code: 'USER_NOT_FOUND' });
    }

    res.json({
      data: {
        id: user.id,
        username: user.username,
        nickname: user.nickname,
        avatar: user.avatar,
      },
      meta: { timestamp: new Date().toISOString() },
    });
  } catch (err) {
    return res.status(401).json({ error: '登录已过期', code: 'TOKEN_EXPIRED' });
  }
});

module.exports = router;
