<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="layout-aside">
      <div class="logo">数据查询平台</div>
      <el-menu
        :default-active="activeMenu"
        router
        class="layout-menu"
      >
        <el-menu-item index="/datasource">
          <el-icon><Management /></el-icon>
          <span>数据源管理</span>
        </el-menu-item>
        <el-menu-item index="/event">
          <el-icon><Document /></el-icon>
          <span>事件管理</span>
        </el-menu-item>
        <el-menu-item index="/schedule">
          <el-icon><Clock /></el-icon>
          <span>调度管理</span>
        </el-menu-item>
        <el-menu-item index="/event-log">
          <el-icon><List /></el-icon>
          <span>事件日志</span>
        </el-menu-item>
        <el-menu-item index="/user-management" v-if="isAdmin">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-title">{{ currentTitle }}</div>
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-icon><UserFilled /></el-icon>
            <span>{{ userInfo.username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

const userInfo = ref({})
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')
const isAdmin = computed(() => userInfo.value.role === 'admin' || localStorage.getItem('userRole') === 'admin')

onMounted(() => {
  const user = localStorage.getItem('userInfo')
  if (user) {
    userInfo.value = JSON.parse(user)
  }
})

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userRole')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: #304156;
  color: white;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: white;
  background: #1f2d3d;
}

.layout-menu {
  border: none;
  background: #304156;
}

.layout-menu :deep(.el-menu-item) {
  color: #bfcbd9;
}

.layout-menu :deep(.el-menu-item.is-active) {
  background-color: #263445 !important;
  color: #409eff !important;
}

.layout-menu :deep(.el-menu-item:hover) {
  background-color: #263445 !important;
  color: white !important;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.layout-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
