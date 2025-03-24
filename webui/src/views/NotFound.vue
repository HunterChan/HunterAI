<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-message">抱歉，您访问的页面不存在或已被移除</p>
      <div class="action-buttons">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.not-found-content {
  text-align: center;
  max-width: 500px;
  padding: 50px 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.error-code {
  font-size: 120px;
  margin: 0;
  color: #409EFF;
  line-height: 1;
  font-weight: 700;
}

.error-title {
  font-size: 30px;
  margin: 20px 0;
  color: #303133;
}

.error-message {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}
</style> 