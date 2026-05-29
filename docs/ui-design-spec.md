# MINIMAL小程序 UI 设计规范

## 概述

本文档定义 MINIMAL 微信小程序的视觉设计系统，确保设计与开发的一致性，为开发者提供可复用的组件规范。

---

## 1. 设计基础

### 1.1 色彩系统

#### 主色调

| Token | 色值 | 用途 |
|-------|------|------|
| `--color-primary` | `#FF6B6B` | 主按钮、强调元素、价格 |
| `--color-primary-light` | `#FF8A8A` | 悬停状态 |
| `--color-primary-dark` | `#E85555` | 按下状态 |

#### 中性色板

| Token | 色值 | 用途 |
|-------|------|------|
| `--color-text-primary` | `#333333` | 主要文本 |
| `--color-text-secondary` | `#666666` | 次要文本 |
| `--color-text-tertiary` | `#999999` | 辅助文本、占位符 |
| `--color-border` | `#DDDDDD` | 边框、分割线 |
| `--color-bg-page` | `#F5F5F5` | 页面背景 |
| `--color-bg-card` | `#FFFFFF` | 卡片背景 |

#### 语义色彩

| Token | 色值 | 用途 |
|-------|------|------|
| `--color-success` | `#10B981` | 成功状态 |
| `--color-warning` | `#F59E0B` | 警告状态 |
| `--color-error` | `#EF4444` | 错误状态 |
| `--color-info` | `#3B82F6` | 信息提示 |

#### 无障碍说明

- 主色 `#FF6B6B` 与白色文字对比度为 **4.2:1**，符合 WCAG AA 标准
- `#999999` 灰色文字不适合作为重要文本使用，仅用于辅助信息
- 所有交互元素确保最小触摸区域为 **44×44rpx**

---

### 1.2 排版系统

#### 字体规范

| Token | 字号 | 行高 | 用途 |
|-------|------|------|------|
| `--font-size-xs` | 24rpx | 1.4 | 辅助文本、日期 |
| `--font-size-sm` | 28rpx | 1.4 | 次要信息、搜索框 |
| `--font-size-base` | 32rpx | 1.5 | 正文、列表项 |
| `--font-size-lg` | 36rpx | 1.4 | 页面标题 |
| `--font-size-xl` | 40rpx | 1.3 | 价格显示 |

#### 字重规范

| Token | 字重 | 用途 |
|-------|------|------|
| `--font-weight-normal` | 400 | 正文 |
| `--font-weight-medium` | 500 | 按钮文字 |
| `--font-weight-bold` | 700 | 标题、价格 |

---

### 1.3 间距系统

基础单位：**8rpx**

| Token | 值 | 用途 |
|-------|------|------|
| `--space-1` | 8rpx | 紧凑间距 |
| `--space-2` | 16rpx | 元素内间距 |
| `--space-3` | 24rpx | 组件间距 |
| `--space-4` | 32rpx | 区块间距 |
| `--space-6` | 48rpx | 页面边距 |

---

### 1.4 圆角系统

| Token | 值 | 用途 |
|-------|------|------|
| `--radius-sm` | 8rpx | 按钮、输入框 |
| `--radius-md` | 12rpx | 卡片 |
| `--radius-lg` | 32rpx | 胶囊按钮、搜索框 |

---

### 1.5 阴影系统

| Token | 值 | 用途 |
|-------|------|------|
| `--shadow-sm` | `0 2rpx 8rpx rgba(0,0,0,0.05)` | 卡片默认 |
| `--shadow-md` | `0 4rpx 12rpx rgba(0,0,0,0.1)` | 悬停/浮起 |

---

### 1.6 过渡动画

| Token | 值 | 用途 |
|-------|------|------|
| `--transition-fast` | 150ms | 按钮状态 |
| `--transition-normal` | 300ms | 页面切换 |

---

## 2. 基础组件

### 2.1 按钮

#### 主要按钮

```css
.btn-primary {
  background-color: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-sm);
  padding: 24rpx 48rpx;
  font-size: var(--font-size-base);
  text-align: center;
}
```

**状态说明：**

| 状态 | 样式变化 |
|------|----------|
| 默认 | `background: #FF6B6B` |
| 悬停（hover） | `background: #FF8A8A` |
| 按下（active） | `background: #E85555` |
| 禁用（disabled） | `background: #CCC; color: #FFF; cursor: not-allowed` |
| 加载中（loading） | `opacity: 0.7; 显示 loading 图标` |

#### 次要按钮

```css
.btn-secondary {
  background-color: var(--color-bg-card);
  color: var(--color-text-primary);
  border: 1rpx solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: 24rpx 48rpx;
  font-size: var(--font-size-base);
  text-align: center;
}
```

**状态说明：**

| 状态 | 样式变化 |
|------|----------|
| 默认 | 白底灰边 |
| 悬停 | 背景 `#F5F5F5` |
| 按下 | 背景 `#EEE` |
| 禁用 | 背景 `#F5F5F5; color: #CCC` |

#### 胶囊按钮

```css
.btn-pill {
  background-color: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-lg);
  padding: 8rpx 24rpx;
  font-size: var(--font-size-xs);
}
```

---

### 2.2 卡片

```css
.card {
  background-color: var(--color-bg-card);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  margin-bottom: var(--space-3);
  box-shadow: var(--shadow-sm);
}
```

**状态说明：**

| 状态 | 样式变化 |
|------|----------|
| 默认 | `shadow-sm` |
| 悬停 | `shadow-md; transform: translateY(-4rpx)` |

---

### 2.3 输入框

```css
.form-input {
  background-color: var(--color-bg-page);
  border: 1rpx solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 16rpx 32rpx;
  font-size: var(--font-size-sm);
}
```

**状态说明：**

| 状态 | 样式变化 |
|------|----------|
| 默认 | 灰色背景 |
| 聚焦 | `border-color: var(--color-primary); box-shadow: 0 0 0 3rpx rgba(255,107,107,0.1)` |
| 错误 | `border-color: var(--color-error)` |
| 禁用 | `background: #F0F0F0; color: #CCC` |

---

### 2.4 商品项

```css
.product-item {
  background-color: var(--color-bg-card);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-3);
  overflow: hidden;
  display: flex;
  flex-direction: row;
}
.product-image { width: 240rpx; height: 240rpx; }
.product-info { flex: 1; padding: var(--space-3); display: flex; flex-direction: column; justify-content: space-between; }
.product-name { font-size: var(--font-size-base); font-weight: bold; color: var(--color-text-primary); }
.product-desc { font-size: var(--font-size-xs); color: var(--color-text-tertiary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
```

---

### 2.5 价格显示

```css
.price {
  color: var(--color-primary);
  font-size: var(--font-size-xl);
  font-weight: bold;
}
.original-price {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-xs);
  text-decoration: line-through;
}
```

---

### 2.6 数量控制器

```css
.quantity-control {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.quantity-control button {
  width: 60rpx;
  height: 60rpx;
  line-height: 60rpx;
  background-color: var(--color-bg-page);
  border-radius: var(--radius-sm);
}
.quantity-control input {
  width: 80rpx;
  text-align: center;
  border: 1rpx solid var(--color-border);
  border-radius: var(--radius-sm);
}
```

---

### 2.7 搜索栏

```css
.search-bar {
  background-color: var(--color-bg-card);
  padding: 20rpx 24rpx;
}
.search-input {
  background-color: var(--color-bg-page);
  border-radius: 32rpx;
  padding: 16rpx 32rpx;
  font-size: var(--font-size-sm);
}
```

---

### 2.8 加载与空状态

```css
.loading, .empty {
  text-align: center;
  padding: 100rpx 0;
  color: var(--color-text-tertiary);
}
```

---

## 3. 布局规范

### 3.1 页面容器

```css
.container {
  padding: var(--space-3);
}
```

### 3.2 弹性布局

```css
.flex-row { display: flex; flex-direction: row; align-items: center; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-center { display: flex; justify-content: center; align-items: center; }
```

---

## 4. 移动端适配

### 4.1 设备适配说明

MINIMAL 为微信小程序，默认针对移动端设计。由于微信小程序使用 rpx 单位自动适配不同屏幕宽度，以下规范确保在主流机型上的一致体验：

#### 主流 iPhone 机型

| 机型 | 屏幕宽度 | rpx 转换比例 |
|------|----------|--------------|
| iPhone SE | 375px | 1rpx = 0.5px |
| iPhone 12/13/14 | 390px | 1rpx = 0.52px |
| iPhone 12/13/14 Pro Max | 428px | 1rpx = 0.57px |

#### 主流 Android 机型

| 机型 | 屏幕宽度 | rpx 转换比例 |
|------|----------|--------------|
| 小米 12 | 420px | 1rpx = 0.56px |
| 华为 P50 | 428px | 1rpx = 0.57px |
| OPPO Find X5 | 480px | 1rpx = 0.64px |

#### 设计注意事项

1. **安全区域**：页面内容需留出顶部状态栏和底部导航栏的安全距离
2. **触摸区域**：所有可点击元素最小尺寸 44×44rpx
3. **文字大小**：正文文字不小于 24rpx，确保可读性
4. **图片适配**：商品图片使用固定 rpx 尺寸，由 rpx 自动适配不同屏幕

### 4.2 固定底部操作栏

```css
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3);
  background-color: var(--color-bg-card);
}
```

---

## 5. 无障碍标准

### 5.1 WCAG AA 合规

- **色彩对比度**：主色与白色对比度 4.2:1，符合 WCAG AA 标准
- **触摸目标**：交互元素最小 44×44rpx
- **焦点指示**：使用明显的颜色变化指示当前焦点
- **文字缩放**：支持系统字体放大至 200%

### 5.2 包容性设计

- 所有按钮都有明确的文字标签
- 图片需提供 alt 描述（在 wxml 中使用 aria-label）
- 错误状态需配合文字说明
- 支持系统减少动画偏好设置

---

## 6. 设计交付检查清单

- [ ] 主色调和辅助色定义完整
- [ ] 所有组件状态（default/hover/active/disabled/loading）已说明
- [ ] 色彩对比度符合 WCAG AA 标准
- [ ] 移动端适配覆盖主流机型
- [ ] 组件样式与代码实现一致

---

**文档版本**：1.0
**更新日期**：2026-05-29
**维护者**：UI 设计师