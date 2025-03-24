<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <!-- 用户资料卡 -->
        <el-card class="profile-card">
          <div class="user-info-header">
            <div class="avatar-container">
              <el-avatar :size="100" :src="userAvatar">
                {{ user?.username?.charAt(0).toUpperCase() || 'U' }}
              </el-avatar>
              <div class="avatar-edit">
                <el-button circle size="small" @click="handleUploadAvatar">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </div>
            </div>
            <h2>{{ user?.username || '用户名' }}</h2>
            <p>{{ user?.email || '邮箱地址' }}</p>
            <el-tag v-if="user?.role" size="small" effect="plain">{{ formatRole(user?.role) }}</el-tag>
          </div>
          
          <div class="user-stats">
            <div class="stat-item">
              <h3>{{ userStats.projects }}</h3>
              <span>项目数</span>
            </div>
            <div class="stat-item">
              <h3>{{ userStats.tasks }}</h3>
              <span>任务数</span>
            </div>
            <div class="stat-item">
              <h3>{{ userStats.notifications }}</h3>
              <span>通知</span>
            </div>
          </div>
          
          <div class="action-buttons">
            <el-button type="primary" @click="activeName = 'account'">编辑资料</el-button>
            <el-button @click="activeName = 'security'">修改密码</el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="16">
        <!-- 用户资料表单 -->
        <el-card>
          <el-tabs v-model="activeName">
            <el-tab-pane label="账户设置" name="account">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="100px"
                label-position="left"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" />
                </el-form-item>
                
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
                
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="profileForm.realName" />
                </el-form-item>
                
                <el-form-item label="电话" prop="phone">
                  <el-input v-model="profileForm.phone" />
                </el-form-item>
                
                <el-form-item label="个人简介" prop="bio">
                  <el-input
                    v-model="profileForm.bio"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入个人简介"
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="updateProfile">
                    保存修改
                  </el-button>
                  <el-button @click="resetForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="安全设置" name="security">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
              >
                <el-form-item label="当前密码" prop="currentPassword">
                  <el-input
                    v-model="passwordForm.currentPassword"
                    type="password"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="updatePassword">
                    更新密码
                  </el-button>
                  <el-button @click="resetPasswordForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="通知设置" name="notifications">
              <div class="notification-settings">
                <el-divider content-position="left">系统通知</el-divider>
                
                <el-row class="notification-option">
                  <el-col :span="18">
                    <h4>任务分配通知</h4>
                    <p>当有新任务分配给您时接收通知</p>
                  </el-col>
                  <el-col :span="6" class="notification-switch">
                    <el-switch v-model="notificationSettings.taskAssigned" />
                  </el-col>
                </el-row>
                
                <el-row class="notification-option">
                  <el-col :span="18">
                    <h4>安全通知</h4>
                    <p>接收安全相关的提醒和警告</p>
                  </el-col>
                  <el-col :span="6" class="notification-switch">
                    <el-switch v-model="notificationSettings.security" />
                  </el-col>
                </el-row>
                
                <el-divider content-position="left">邮件通知</el-divider>
                
                <el-row class="notification-option">
                  <el-col :span="18">
                    <h4>系统更新</h4>
                    <p>接收关于系统更新和维护的邮件通知</p>
                  </el-col>
                  <el-col :span="6" class="notification-switch">
                    <el-switch v-model="notificationSettings.systemUpdates" />
                  </el-col>
                </el-row>
                
                <el-row class="notification-option">
                  <el-col :span="18">
                    <h4>营销信息</h4>
                    <p>接收新功能和促销信息的邮件通知</p>
                  </el-col>
                  <el-col :span="6" class="notification-switch">
                    <el-switch v-model="notificationSettings.marketing" />
                  </el-col>
                </el-row>
                
                <div class="notification-actions">
                  <el-button type="primary" @click="saveNotificationSettings">
                    保存设置
                  </el-button>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const activeName = ref('account')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

// 用户信息
const user = computed(() => userStore.user)

// 用户头像（假设 user 对象中有 avatar 字段，如果没有则使用默认值）
const userAvatar = computed(() => user.value?.avatar || '')

// 用户资料统计数据（模拟数据）
const userStats = reactive({
  projects: 5,
  tasks: 12,
  notifications: 3
})

// 用户资料表单
const profileForm = reactive({
  username: '',
  email: '',
  realName: '',
  phone: '',
  bio: ''
})

// 修改密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 通知设置
const notificationSettings = reactive({
  taskAssigned: true,
  security: true,
  systemUpdates: false,
  marketing: false
})

// 校验新密码
const validateNewPassword = (rule, value, callback) => {
  if (value === passwordForm.currentPassword) {
    callback(new Error('新密码不能与当前密码相同'))
  } else {
    callback()
  }
}

// 校验确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 资料表单验证规则
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 密码表单验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于 6 个字符', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 格式化角色名称
const formatRole = (role) => {
  const roleMap = {
    'ROLE_ADMIN': '管理员',
    'ROLE_USER': '普通用户',
    'ROLE_GUEST': '访客'
  }
  return roleMap[role] || role
}

// 上传头像
const handleUploadAvatar = () => {
  ElMessage.info('头像上传功能待实现')
}

// 更新个人资料
const updateProfile = async () => {
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 模拟 API 调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // TODO: 调用实际的 API 更新用户资料
        // await api.put('/user/profile', profileForm)
        
        ElMessage.success('个人资料更新成功')
      } catch (error) {
        ElMessage.error('更新失败：' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }
  })
}

// 更新密码
const updatePassword = async () => {
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 模拟 API 调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // TODO: 调用实际的 API 更新密码
        // await api.put('/user/password', {
        //   currentPassword: passwordForm.currentPassword,
        //   newPassword: passwordForm.newPassword
        // })
        
        ElMessage.success('密码更新成功')
        resetPasswordForm()
      } catch (error) {
        ElMessage.error('更新失败：' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }
  })
}

// 保存通知设置
const saveNotificationSettings = async () => {
  loading.value = true
  try {
    // 模拟 API 调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // TODO: 调用实际的 API 保存通知设置
    // await api.put('/user/notifications', notificationSettings)
    
    ElMessage.success('通知设置已保存')
  } catch (error) {
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  profileFormRef.value.resetFields()
  initProfileForm()
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordFormRef.value.resetFields()
}

// 初始化表单数据
const initProfileForm = () => {
  if (user.value) {
    profileForm.username = user.value.username || ''
    profileForm.email = user.value.email || ''
    profileForm.realName = user.value.realName || ''
    profileForm.phone = user.value.phone || ''
    profileForm.bio = user.value.bio || ''
  }
}

onMounted(async () => {
  if (userStore.isLoggedIn && !user.value) {
    await userStore.fetchUserProfile()
  }
  
  // 初始化表单数据
  initProfileForm()
  
  // 这里可以加载用户额外的资料信息
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.user-info-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.avatar-container {
  position: relative;
  margin-bottom: 15px;
}

.avatar-edit {
  position: absolute;
  right: 0;
  bottom: 0;
}

.user-info-header h2 {
  margin: 10px 0 5px;
  font-size: 20px;
}

.user-info-header p {
  margin: 0 0 10px;
  color: #606266;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-item h3 {
  margin: 0;
  font-size: 24px;
  color: #409EFF;
}

.stat-item span {
  font-size: 14px;
  color: #606266;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.notification-option {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.notification-option h4 {
  margin: 0 0 5px 0;
}

.notification-option p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.notification-switch {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.notification-actions {
  margin-top: 20px;
  text-align: right;
}
</style>