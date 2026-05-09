/**
 * Admin Panel JavaScript
 * Version: 1.0.0
 */

(function() {
  'use strict';

  // ============================================
  // Sidebar Toggle
  // ============================================
  const sidebar = document.getElementById('sidebar');
  const sidebarToggle = document.getElementById('sidebarToggle');
  let isSidebarOpen = false;

  if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener('click', function() {
      isSidebarOpen = !isSidebarOpen;
      if (isSidebarOpen) {
        sidebar.classList.add('admin-sidebar--open');
      } else {
        sidebar.classList.remove('admin-sidebar--open');
      }
    });

    // Close sidebar when clicking overlay (mobile)
    document.addEventListener('click', function(e) {
      if (isSidebarOpen &&
          !sidebar.contains(e.target) &&
          !sidebarToggle.contains(e.target)) {
        isSidebarOpen = false;
        sidebar.classList.remove('admin-sidebar--open');
      }
    });
  }

  // ============================================
  // User Menu Dropdown
  // ============================================
  const userMenuTrigger = document.getElementById('userMenuTrigger');
  const userMenuDropdown = document.getElementById('userMenuDropdown');

  if (userMenuTrigger && userMenuDropdown) {
    userMenuTrigger.addEventListener('click', function(e) {
      e.stopPropagation();
      userMenuDropdown.classList.toggle('admin-user-menu__dropdown--open');
    });

    document.addEventListener('click', function(e) {
      if (!userMenuTrigger.contains(e.target) && !userMenuDropdown.contains(e.target)) {
        userMenuDropdown.classList.remove('admin-user-menu__dropdown--open');
      }
    });

    // Close on escape key
    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape') {
        userMenuDropdown.classList.remove('admin-user-menu__dropdown--open');
      }
    });
  }

  // ============================================
  // Navigation Active State
  // ============================================
  function setActiveNavItem() {
    const currentPath = window.location.pathname;
    const navItems = document.querySelectorAll('.admin-nav-item');

    navItems.forEach(function(item) {
      const href = item.getAttribute('href');
      if (href && currentPath === href) {
        item.classList.add('admin-nav-item--active');
      } else if (href && currentPath.startsWith(href) && href !== '/admin') {
        // Sub-page active state (e.g., /admin/products/new under /admin/products)
        item.classList.add('admin-nav-item--active');
      } else {
        item.classList.remove('admin-nav-item--active');
      }
    });
  }

  setActiveNavItem();

  // ============================================
  // Breadcrumb Generation
  // ============================================
  function generateBreadcrumb() {
    const breadcrumb = document.getElementById('breadcrumb');
    if (!breadcrumb) return;

    const pathParts = window.location.pathname.split('/').filter(Boolean);
    const breadcrumbs = [];

    // Home
    breadcrumbs.push({
      text: '首页',
      href: '/admin',
      isHome: true
    });

    // Build breadcrumb from path
    let currentPath = '/admin';
    pathParts.slice(1).forEach(function(part, index) {
      currentPath += '/' + part;

      // Format the text (replace hyphens, capitalize)
      let text = part
        .replace(/-/g, ' ')
        .replace(/\b\w/g, function(c) { return c.toUpperCase(); });

      // Map common paths to Chinese
      const pathMap = {
        'products': '商品管理',
        'orders': '订单管理',
        'users': '用户管理',
        'coupons': '优惠券',
        'live': '直播管理',
        'settings': '系统设置',
        'new': '新增',
        'edit': '编辑',
        'dashboard': '控制台'
      };

      text = pathMap[part] || text;

      breadcrumbs.push({
        text: text,
        href: currentPath,
        isLast: index === pathParts.slice(1).length - 1
      });
    });

    // Render breadcrumbs
    breadcrumb.innerHTML = breadcrumbs.map(function(item, index) {
      if (item.isLast) {
        return '<span class="admin-breadcrumb__item admin-breadcrumb__item--current">' + item.text + '</span>';
      }
      return '<a href="' + item.href + '" class="admin-breadcrumb__item">' + item.text + '</a>' +
             '<span class="admin-breadcrumb__separator">/</span>';
    }).join('');
  }

  generateBreadcrumb();

  // ============================================
  // Image Upload Preview
  // ============================================
  function initImageUpload() {
    const uploadArea = document.querySelector('.admin-image-upload__preview');
    const fileInput = document.querySelector('.admin-image-upload__input');

    if (uploadArea && fileInput) {
      uploadArea.addEventListener('click', function() {
        fileInput.click();
      });

      uploadArea.addEventListener('dragover', function(e) {
        e.preventDefault();
        uploadArea.classList.add('admin-image-upload__preview--dragover');
      });

      uploadArea.addEventListener('dragleave', function() {
        uploadArea.classList.remove('admin-image-upload__preview--dragover');
      });

      uploadArea.addEventListener('drop', function(e) {
        e.preventDefault();
        uploadArea.classList.remove('admin-image-upload__preview--dragover');

        const files = e.dataTransfer.files;
        if (files.length > 0 && files[0].type.startsWith('image/')) {
          handleImageUpload(files[0]).catch(console.error);
        }
      });

      fileInput.addEventListener('change', function() {
        if (this.files.length > 0) {
          handleImageUpload(this.files[0]).catch(console.error);
        }
      });
    }
  }

  async function handleImageUpload(file) {
    // Validate file size (max 2MB)
    if (file.size > 2 * 1024 * 1024) {
      alert('图片大小不能超过 2MB');
      return;
    }

    // Validate file type
    if (!file.type.startsWith('image/')) {
      alert('请上传图片文件');
      return;
    }

    // Read file as base64
    const reader = new FileReader();
    const base64Data = await new Promise((resolve) => {
      reader.onload = (e) => resolve(e.target.result);
      reader.readAsDataURL(file);
    });

    // Preview immediately
    const preview = document.querySelector('.admin-image-upload__preview');
    const placeholder = preview.querySelector('.admin-image-upload__placeholder');

    if (placeholder) {
      placeholder.style.display = 'none';
    }

    let img = preview.querySelector('img');
    if (!img) {
      img = document.createElement('img');
      preview.appendChild(img);
    }
    img.src = base64Data;

    // Upload to server
    try {
      const result = await AdminAPI.uploadImageBase64(base64Data);
      const imageUrlInput = document.getElementById('image-url');
      if (imageUrlInput && result.url) {
        imageUrlInput.value = result.url;
      }
    } catch (error) {
      console.error('Image upload failed:', error);
      alert('图片上传失败，请重试');
    }
  }

  initImageUpload();

  // ============================================
  // Form Validation
  // ============================================
  function initFormValidation() {
    const forms = document.querySelectorAll('.admin-product-form');

    forms.forEach(function(form) {
      form.addEventListener('submit', function(e) {
        let isValid = true;
        const requiredFields = form.querySelectorAll('[required]');

        requiredFields.forEach(function(field) {
          if (!field.value.trim()) {
            isValid = false;
            field.classList.add('form-input--error');
          } else {
            field.classList.remove('form-input--error');
          }
        });

        // Price validation
        const priceField = form.querySelector('input[name="price"]');
        if (priceField && priceField.value) {
          const price = parseFloat(priceField.value);
          if (isNaN(price) || price < 0) {
            isValid = false;
            priceField.classList.add('form-input--error');
          }
        }

        // Stock validation
        const stockField = form.querySelector('input[name="stock"]');
        if (stockField && stockField.value) {
          const stock = parseInt(stockField.value);
          if (isNaN(stock) || stock < 0 || !Number.isInteger(stock)) {
            isValid = false;
            stockField.classList.add('form-input--error');
          }
        }

        if (!isValid) {
          e.preventDefault();
          alert('请检查表单中的错误');
        }
      });

      // Clear error on input
      const inputs = form.querySelectorAll('.form-input');
      inputs.forEach(function(input) {
        input.addEventListener('input', function() {
          this.classList.remove('form-input--error');
        });
      });
    });
  }

  initFormValidation();

  // ============================================
  // API Helpers
  // ============================================
  const API_BASE = '/api';

  window.AdminAPI = {
    // Products (paginated)
    async getProducts(params = {}) {
      const queryString = new URLSearchParams(params).toString();
      const url = API_BASE + '/products' + (queryString ? '?' + queryString : '');
      const response = await fetch(url);
      if (!response.ok) throw new Error('Failed to fetch products');
      return response.json();
    },

    async getProduct(id) {
      const response = await fetch(API_BASE + '/products/' + id);
      if (!response.ok) throw new Error('Failed to fetch product');
      return response.json();
    },

    async createProduct(data) {
      const response = await fetch(API_BASE + '/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });
      if (!response.ok) throw new Error('Failed to create product');
      return response.json();
    },

    async updateProduct(id, data) {
      const response = await fetch(API_BASE + '/products/' + id, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });
      if (!response.ok) throw new Error('Failed to update product');
      return response.json();
    },

    async deleteProduct(id) {
      const response = await fetch(API_BASE + '/products/' + id, {
        method: 'DELETE'
      });
      if (!response.ok) throw new Error('Failed to delete product');
    },

    // Search (non-paginated)
    async searchProducts(name) {
      const response = await fetch(API_BASE + '/products/search?name=' + encodeURIComponent(name));
      if (!response.ok) throw new Error('Failed to search products');
      return response.json();
    },

    // Image upload via base64
    async uploadImageBase64(base64Data) {
      const response = await fetch(API_BASE + '/upload/image/base64', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ image: base64Data })
      });
      if (!response.ok) throw new Error('Failed to upload image');
      return response.json();
    }
  };

  // ============================================
  // Product List Page
  // ============================================
  function initProductList() {
    const productListContainer = document.getElementById('product-list');
    if (!productListContainer) return;

    let currentPage = 0;
    let pageSize = 10;
    let searchQuery = '';
    let sortOrder = '';
    let isLoading = false;
    let totalPages = 0;
    let totalElements = 0;

    async function loadProducts() {
      if (isLoading) return;
      isLoading = true;

      const loadingEl = document.getElementById('product-loading');
      const emptyEl = document.getElementById('product-empty');
      const tableBody = document.getElementById('product-table-body');

      if (loadingEl) loadingEl.style.display = 'flex';
      if (emptyEl) emptyEl.style.display = 'none';

      try {
        const params = { page: currentPage, size: pageSize };
        if (searchQuery) params.search = searchQuery;
        if (sortOrder) params.sort = sortOrder;

        const pageResponse = await AdminAPI.getProducts(params);
        const products = pageResponse.content || [];
        totalElements = pageResponse.totalElements || 0;
        totalPages = pageResponse.totalPages || 1;

        if (loadingEl) loadingEl.style.display = 'none';

        if (!products || products.length === 0) {
          if (emptyEl) emptyEl.style.display = 'flex';
          if (tableBody) tableBody.innerHTML = '';
          updatePaginationInfo();
          return;
        }

        renderProducts(products);
        updatePaginationInfo();
      } catch (error) {
        console.error('Error loading products:', error);
        if (loadingEl) loadingEl.style.display = 'none';
      } finally {
        isLoading = false;
      }
    }

    function updatePaginationInfo() {
      const totalCountEl = document.getElementById('total-count');
      const currentPageEl = document.getElementById('current-page');
      const totalPagesEl = document.getElementById('total-pages');
      const prevBtn = document.getElementById('prev-page');
      const nextBtn = document.getElementById('next-page');

      if (totalCountEl) totalCountEl.textContent = totalElements;
      if (currentPageEl) currentPageEl.textContent = currentPage + 1;
      if (totalPagesEl) totalPagesEl.textContent = totalPages;

      if (prevBtn) prevBtn.disabled = currentPage === 0;
      if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1;
    }

    function renderProducts(products) {
      const tableBody = document.getElementById('product-table-body');
      if (!tableBody) return;

      tableBody.innerHTML = products.map(function(product) {
        const statusBadge = product.active
          ? '<span class="badge badge--success">上架</span>'
          : '<span class="badge badge--error">下架</span>';

        const stockClass = product.stock < 10 ? 'text-error' : '';

        return '<tr>' +
          '<td>' + escapeHtml(product.name) + '</td>' +
          '<td>' +
            (product.imageUrl
              ? '<img src="' + escapeHtml(product.imageUrl) + '" alt="" style="width: 48px; height: 48px; object-fit: cover; border-radius: var(--radius-md);">'
              : '<span class="text-secondary">无图片</span>') +
          '</td>' +
          '<td>¥' + product.price.toFixed(2) + '</td>' +
          '<td class="' + stockClass + '">' + product.stock + '</td>' +
          '<td>' + statusBadge + '</td>' +
          '<td>' + formatDate(product.createdAt) + '</td>' +
          '<td>' +
            '<div class="admin-table__actions">' +
              '<a href="/admin/products/' + product.id + '" class="btn btn--ghost btn--sm">查看</a>' +
              '<a href="/admin/products/' + product.id + '/edit" class="btn btn--ghost btn--sm">编辑</a>' +
              '<button type="button" class="btn btn--danger btn--sm" onclick="deleteProduct(\'' + product.id + '\')">删除</button>' +
            '</div>' +
          '</td>' +
        '</tr>';
      }).join('');
    }

    // Pagination controls
    const prevBtn = document.getElementById('prev-page');
    const nextBtn = document.getElementById('next-page');

    if (prevBtn) {
      prevBtn.addEventListener('click', function() {
        if (currentPage > 0) {
          currentPage--;
          loadProducts();
        }
      });
    }

    if (nextBtn) {
      nextBtn.addEventListener('click', function() {
        if (currentPage < totalPages - 1) {
          currentPage++;
          loadProducts();
        }
      });
    }

    // Search handler
    const searchInput = document.getElementById('product-search');
    const searchBtn = document.getElementById('product-search-btn');

    if (searchInput) {
      searchInput.addEventListener('input', function() {
        searchQuery = this.value.trim();
      });

      searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
          e.preventDefault();
          currentPage = 0;
          loadProducts();
        }
      });
    }

    if (searchBtn) {
      searchBtn.addEventListener('click', function() {
        if (searchInput) {
          searchQuery = searchInput.value.trim();
        }
        currentPage = 0;
        loadProducts();
      });
    }

    // Sort handler
    const sortSelects = document.querySelectorAll('.admin-toolbar__filters select');
    sortSelects.forEach(function(select) {
      select.addEventListener('change', function() {
        sortOrder = this.value;
        currentPage = 0;
        loadProducts();
      });
    });

    // Initial load
    loadProducts();
  }

  // ============================================
  // Delete Product
  // ============================================
  window.deleteProduct = async function(id) {
    if (!confirm('确定要删除这个商品吗？')) return;

    try {
      await AdminAPI.deleteProduct(id);
      alert('商品已删除');
      // Refresh the list
      const event = new CustomEvent('product-deleted');
      document.dispatchEvent(event);
    } catch (error) {
      console.error('Error deleting product:', error);
      alert('删除失败，请稍后重试');
    }
  };

  // ============================================
  // Product Form Page
  // ============================================
  function initProductForm() {
    const form = document.getElementById('product-form');
    if (!form) return;

    const isEdit = form.dataset.edit === 'true';
    const productId = form.dataset.productId;

    // Load product data if editing
    if (isEdit && productId) {
      loadProduct(productId);
    }

    async function loadProduct(id) {
      try {
        const product = await AdminAPI.getProduct(id);

        // Fill form fields
        const nameField = form.querySelector('input[name="name"]');
        const descField = form.querySelector('textarea[name="description"]');
        const priceField = form.querySelector('input[name="price"]');
        const stockField = form.querySelector('input[name="stock"]');
        const imageField = form.querySelector('input[name="imageUrl"]');
        const activeField = form.querySelector('input[name="active"]');

        if (nameField) nameField.value = product.name || '';
        if (descField) descField.value = product.description || '';
        if (priceField) priceField.value = product.price || '';
        if (stockField) stockField.value = product.stock || 0;
        if (imageField) imageField.value = product.imageUrl || '';
        if (activeField) activeField.checked = product.active !== false;

        // Show existing image
        if (product.imageUrl) {
          const preview = document.querySelector('.admin-image-upload__preview');
          if (preview) {
            const placeholder = preview.querySelector('.admin-image-upload__placeholder');
            if (placeholder) placeholder.style.display = 'none';
            let img = preview.querySelector('img');
            if (!img) {
              img = document.createElement('img');
              preview.appendChild(img);
            }
            img.src = product.imageUrl;
          }
        }

        // Set page title
        const pageTitle = document.getElementById('page-title');
        if (pageTitle) {
          pageTitle.textContent = '编辑商品';
        }
      } catch (error) {
        console.error('Error loading product:', error);
        alert('加载商品信息失败');
      }
    }

    // Form submission
    form.addEventListener('submit', async function(e) {
      e.preventDefault();

      const formData = {
        name: form.querySelector('input[name="name"]')?.value || '',
        description: form.querySelector('textarea[name="description"]')?.value || '',
        price: parseFloat(form.querySelector('input[name="price"]')?.value) || 0,
        stock: parseInt(form.querySelector('input[name="stock"]')?.value) || 0,
        imageUrl: form.querySelector('input[name="imageUrl"]')?.value || '',
        active: form.querySelector('input[name="active"]')?.checked !== false
      };

      // Validation
      if (!formData.name.trim()) {
        alert('请输入商品名称');
        return;
      }

      if (formData.price < 0) {
        alert('价格不能为负数');
        return;
      }

      if (formData.stock < 0) {
        alert('库存不能为负数');
        return;
      }

      try {
        if (isEdit) {
          await AdminAPI.updateProduct(productId, formData);
          alert('商品更新成功');
        } else {
          await AdminAPI.createProduct(formData);
          alert('商品创建成功');
        }
        window.location.href = '/admin/products';
      } catch (error) {
        console.error('Error saving product:', error);
        alert('保存失败，请稍后重试');
      }
    });
  }

  // ============================================
  // Product Detail Page
  // ============================================
  function initProductDetail() {
    const detailContainer = document.getElementById('product-detail');
    if (!detailContainer) return;

    const productId = detailContainer.dataset.productId;
    if (!productId) return;

    loadProductDetail(productId);

    async function loadProductDetail(id) {
      try {
        const product = await AdminAPI.getProduct(id);
        renderProductDetail(product);
      } catch (error) {
        console.error('Error loading product:', error);
        detailContainer.innerHTML = '<div class="admin-empty-state"><p>加载失败</p></div>';
      }
    }

    function renderProductDetail(product) {
      const statusBadge = product.active
        ? '<span class="badge badge--success">上架</span>'
        : '<span class="badge badge--error">下架</span>';

      const stockClass = product.stock < 10 ? 'text-error' : '';

      detailContainer.innerHTML = '<div class="admin-card">' +
        '<div class="admin-card__header">' +
          '<h2 class="admin-card__title">' + escapeHtml(product.name) + '</h2>' +
          statusBadge +
        '</div>' +
        '<div class="admin-card__body">' +
          '<div class="admin-detail-grid">' +
            '<div class="admin-detail-item">' +
              '<label class="admin-detail-label">商品图片</label>' +
              '<div class="admin-detail-value">' +
                (product.imageUrl
                  ? '<img src="' + escapeHtml(product.imageUrl) + '" alt="" style="max-width: 200px; border-radius: var(--radius-lg);">'
                  : '<span class="text-secondary">无图片</span>') +
              '</div>' +
            '</div>' +
            '<div class="admin-detail-item">' +
              '<label class="admin-detail-label">价格</label>' +
              '<div class="admin-detail-value text-xl font-semibold text-primary">¥' + product.price.toFixed(2) + '</div>' +
            '</div>' +
            '<div class="admin-detail-item">' +
              '<label class="admin-detail-label">库存</label>' +
              '<div class="admin-detail-value ' + stockClass + '">' + product.stock + '</div>' +
            '</div>' +
            '<div class="admin-detail-item">' +
              '<label class="admin-detail-label">创建时间</label>' +
              '<div class="admin-detail-value">' + formatDate(product.createdAt) + '</div>' +
            '</div>' +
            '<div class="admin-detail-item">' +
              '<label class="admin-detail-label">更新时间</label>' +
              '<div class="admin-detail-value">' + formatDate(product.updatedAt) + '</div>' +
            '</div>' +
            '<div class="admin-detail-item admin-detail-item--full">' +
              '<label class="admin-detail-label">商品描述</label>' +
              '<div class="admin-detail-value">' + (product.description ? escapeHtml(product.description) : '<span class="text-secondary">无描述</span>') + '</div>' +
            '</div>' +
          '</div>' +
        '</div>' +
        '<div class="admin-card__footer">' +
          '<a href="/admin/products/' + product.id + '/edit" class="btn btn--primary">编辑商品</a>' +
          '<a href="/admin/products" class="btn btn--secondary">返回列表</a>' +
        '</div>' +
      '</div>';
    }
  }

  // ============================================
  // Utility Functions
  // ============================================
  function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  function formatDate(instant) {
    if (!instant) return '-';
    const date = new Date(instant);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // ============================================
  // Initialize on DOM Ready
  // ============================================
  document.addEventListener('DOMContentLoaded', function() {
    initProductList();
    initProductForm();
    initProductDetail();
  });

})();