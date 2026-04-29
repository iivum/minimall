<template>
  <div class="layout-container">
    <el-container>
      <el-aside width="200px" class="aside">
        <div class="logo">
          <h3>Minimall</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="menu"
          router
        >
          <el-sub-menu index="/dashboard">
            <template #title>
              <el-icon><Odometer /></el-icon>
              <span>数据看板</span>
            </template>
            <el-menu-item index="/dashboard/overview">
              <span>运营概览</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/orders">
              <span>订单分析</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/users">
              <span>用户分析</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-right">
            <el-dropdown>
              <span class="user-info">
                <el-icon><User /></el-icon>
                管理员
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.aside {
  background: #304156;
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    h3 {
      color: #fff;
      font-size: 18px;
    }
  }
  .menu {
    border-right: none;
    background: #304156;
    :deep(.el-menu-item) {
      color: #bfcbd9;
      &:hover {
        background: #263445;
      }
      &.is-active {
        color: #409eff;
      }
    }
    :deep(.el-sub-menu__title) {
      color: #bfcbd9;
      &:hover {
        background: #263445;
      }
    }
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;
  }
}

.main {
  background: #f0f2f5;
}
</style>
