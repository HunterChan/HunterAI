<template>
  <el-container class="layout-container">
    <!-- 头部区域 -->
    <el-header height="60px" class="header">
      <div class="logo">
        <h1>HunterAI</h1>
      </div>
      <div class="menu">
        <el-menu
          mode="horizontal"
          :ellipsis="false"
          class="header-menu"
          :router="true"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/projects">
            <el-icon><Folder /></el-icon>
            <span>项目</span>
          </el-menu-item>
          <el-menu-item index="/help">
            <el-icon><QuestionFilled /></el-icon>
            <span>帮助</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="user-actions">
        <el-dropdown @command="handleCommand">
          <span class="user-dropdown-link">
            <el-avatar :size="32" :src="user?.avatar">
              {{ user?.username?.charAt(0).toUpperCase() || 'U' }}
            </el-avatar>
            <span class="username">{{ user?.username || '用户' }}</span>
            <el-icon><CaretBottom /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                <span>个人资料</span>
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                <span>设置</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside width="220px" class="sidebar">
        <el-scrollbar>
          <el-menu
            :default-active="activeMenuItem"
            class="sidebar-menu"
            :router="true"
            :collapse="isCollapse"
          >
            <el-menu-item index="/photomerge">
              <el-icon><Picture /></el-icon>
              <span>甜影合拍</span>
            </el-menu-item>
            
            <el-sub-menu index="2">
              <template #title>
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/users/list">用户列表</el-menu-item>
              <el-menu-item index="/users/roles">角色管理</el-menu-item>
              <el-menu-item index="/users/permissions">权限管理</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="3">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统设置</span>
              </template>
              <el-menu-item index="/system/basic">基本设置</el-menu-item>
              <el-menu-item index="/system/advanced">高级设置</el-menu-item>
              <el-menu-item index="/system/security">安全设置</el-menu-item>
              <el-menu-item index="/system/backup">备份恢复</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-scrollbar>
      </el-aside>
      
      <!-- 主内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
    
    <!-- 底部区域 -->
    <el-footer height="50px" class="footer">
      <div class="copyright">
        &copy; {{ currentYear }} HunterAI. 保留所有权利.
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  HomeFilled,
  DataAnalysis,
  Folder,
  QuestionFilled,
  CaretBottom,
  User,
  Setting,
  SwitchButton,
  Document,
  Picture
} from '@element-plus/icons-vue'

// 路由和状态管理
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏状态
const isCollapse = ref(false)
const activeMenuItem = computed(() => route.path)

// 用户信息
const user = computed(() => userStore.user)

// 获取当前年份
const currentYear = computed(() => new Date().getFullYear())

// 处理用户下拉菜单操作
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/system/basic')
      break
    case 'logout':
      confirmLogout()
      break
  }
}

// 确认登出
const confirmLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '退出确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    userStore.logout()
    ElMessage.success('已成功退出登录')
    router.push('/login')
  }).catch(() => {
    // 取消登出操作
  })
}

onMounted(async () => {
  if (userStore.isLoggedIn && !userStore.user) {
    try {
      await userStore.fetchUserProfile()
    } catch (error) {
      console.error('Failed to fetch user profile:', error)
    }
  }
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.logo h1 {
  margin: 0;
  font-size: 24px;
  color: #409EFF;
}

.header-menu {
  border-bottom: none;
}

.user-actions {
  display: flex;
  align-items: center;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

.main-container {
  min-height: calc(100vh - 110px);
}

.sidebar {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #ebeef5;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
}

.copyright {
  color: #909399;
  font-size: 14px;
}
</style> 