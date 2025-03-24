<template>
  <div class="home-container">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <template #header>
            <div class="welcome-header">
              <h2>欢迎回来，{{ user?.username || '用户' }}！</h2>
              <div>
                <el-button type="primary" @click="navigateTo('/photomerge')">甜影合拍</el-button>
                <el-button type="success" style="margin-left: 10px;">开始使用</el-button>
              </div>
            </div>
          </template>
          <div class="welcome-content">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="8">
                <div class="stat-item">
                  <h3>项目总数</h3>
                  <div class="stat-value">{{ stats.projects }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="8">
                <div class="stat-item">
                  <h3>任务完成</h3>
                  <div class="stat-value">{{ stats.completedTasks }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="8">
                <div class="stat-item">
                  <h3>系统消息</h3>
                  <div class="stat-value">{{ stats.messages }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <!-- 快速访问区域 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>快速访问</h3>
            </div>
          </template>
          <div class="quick-access">
            <el-row :gutter="10">
              <el-col :span="6" v-for="(item, index) in quickAccess" :key="index">
                <div class="quick-item" @click="navigateTo(item.path)">
                  <el-icon :size="24"><component :is="item.icon" /></el-icon>
                  <span>{{ item.title }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
      
      <!-- 最近活动 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>最近活动</h3>
            </div>
          </template>
          <div class="activities">
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in recentActivities"
                :key="index"
                :timestamp="activity.time"
                :type="activity.type"
              >
                {{ activity.content }}
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <!-- 系统状态 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>系统状态</h3>
              <el-button type="text">查看详情</el-button>
            </div>
          </template>
          <div class="system-status">
            <el-row :gutter="20">
              <el-col :xs="24" :md="8" v-for="(item, index) in systemStatus" :key="index">
                <div class="status-item">
                  <h4>{{ item.name }}</h4>
                  <el-progress
                    :percentage="item.value"
                    :color="item.color"
                    :stroke-width="15"
                    :format="format"
                  />
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import {
  Document,
  User,
  Setting,
  Bell,
  DataAnalysis,
  VideoPlay,
  Picture,
  Management
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.user)

// 统计数据
const stats = reactive({
  projects: 12,
  completedTasks: 48,
  messages: 7
})

// 快速访问项
const quickAccess = [
  { title: '项目管理', icon: 'Document', path: '/projects' },
  { title: '用户中心', icon: 'User', path: '/profile' },
  { title: '甜影合拍', icon: 'Picture', path: '/photomerge' },
  { title: '数据分析', icon: 'DataAnalysis', path: '/analytics' },
  { title: '媒体管理', icon: 'Picture', path: '/media' },
  { title: '系统设置', icon: 'Setting', path: '/settings' },
  { title: '消息中心', icon: 'Bell', path: '/messages' },
  { title: '视频管理', icon: 'VideoPlay', path: '/videos' }
]

// 最近活动
const recentActivities = [
  { content: '完成了项目"AI数据分析平台"的需求文档', time: '2025-03-22 18:30', type: 'success' },
  { content: '提交了新的功能请求', time: '2025-03-21 14:20', type: 'primary' },
  { content: '修复了用户界面的错误', time: '2025-03-20 09:45', type: 'info' },
  { content: '系统更新至最新版本', time: '2025-03-18 22:10', type: 'warning' }
]

// 系统状态
const systemStatus = [
  { name: 'CPU使用率', value: 42, color: '#67C23A' },
  { name: '内存使用率', value: 68, color: '#E6A23C' },
  { name: '存储空间', value: 85, color: '#F56C6C' }
]

const format = (percentage) => {
  return `${percentage}%`
}

const navigateTo = (path) => {
  router.push(path)
}

onMounted(async () => {
  try {
    if (userStore.isLoggedIn && !userStore.user) {
      await userStore.fetchUserProfile()
    }
    // 这里可以加载更多初始数据
  } catch (error) {
    console.error('Failed to load data:', error)
  }
})
</script>

<style scoped>
.home-container {
  padding: 10px;
}

.mt-20 {
  margin-top: 20px;
}

.welcome-card {
  background-color: #fff;
}

.welcome-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-header h2 {
  margin: 0;
  color: #303133;
}

.welcome-content {
  padding: 10px 0;
}

.stat-item {
  text-align: center;
  padding: 20px 0;
}

.stat-item h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #606266;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.quick-access {
  padding: 10px 0;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 15px 0;
  border-radius: 4px;
  transition: all 0.3s;
}

.quick-item:hover {
  background-color: #f5f7fa;
}

.quick-item span {
  margin-top: 8px;
  font-size: 14px;
}

.activities {
  padding: 10px 0;
  height: 280px;
  overflow-y: auto;
}

.system-status {
  padding: 10px 0;
}

.status-item {
  margin-bottom: 15px;
}

.status-item h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}
</style> 