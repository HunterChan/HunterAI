import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
// import api from '../api' // 假设有一个API服务

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  const loading = ref(false)
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  
  // 操作方法
  
  /**
   * 用户登录
   * @param {Object} credentials - 包含用户名和密码的对象
   */
  const login = async (credentials) => {
    loading.value = true
    
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // TODO: 实际的API调用
      // const response = await api.post('/auth/login', credentials)
      // const data = response.data
      
      // 模拟返回数据
      const data = {
        token: 'fake-jwt-token',
        user: {
          id: 1,
          username: credentials.username,
          email: `${credentials.username}@example.com`,
          role: 'ROLE_USER'
        }
      }
      
      // 保存token到本地存储和状态
      token.value = data.token
      localStorage.setItem('token', data.token)
      
      // 保存用户信息
      user.value = data.user
      
      return data
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('登录失败，请检查用户名和密码')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 用户注册
   * @param {Object} userData - 包含用户注册信息的对象
   */
  const register = async (userData) => {
    loading.value = true
    
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // TODO: 实际的API调用
      // const response = await api.post('/auth/register', userData)
      // const data = response.data
      
      // 注册成功后不自动登录，返回到登录页
      return { success: true }
    } catch (error) {
      console.error('注册失败:', error)
      ElMessage.error('注册失败，请稍后重试')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取用户个人资料
   */
  const fetchUserProfile = async () => {
    if (!token.value) return null
    
    loading.value = true
    
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 800))
      
      // TODO: 实际的API调用
      // const response = await api.get('/user/profile')
      // const data = response.data
      
      // 模拟返回数据
      const data = {
        id: 1,
        username: 'testuser',
        email: 'testuser@example.com',
        role: 'ROLE_USER',
        avatar: '',
        realName: '测试用户',
        phone: '13800138000',
        bio: '这是一个测试账户的个人简介'
      }
      
      // 更新用户信息
      user.value = data
      
      return data
    } catch (error) {
      console.error('获取用户资料失败:', error)
      
      // 如果是401错误，则登出用户
      if (error.response && error.response.status === 401) {
        logout()
      }
      
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 更新用户个人资料
   * @param {Object} profileData - 要更新的个人资料数据
   */
  const updateProfile = async (profileData) => {
    if (!token.value) return null
    
    loading.value = true
    
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // TODO: 实际的API调用
      // const response = await api.put('/user/profile', profileData)
      // const data = response.data
      
      // 更新本地用户信息
      user.value = {
        ...user.value,
        ...profileData
      }
      
      ElMessage.success('个人资料更新成功')
      return user.value
    } catch (error) {
      console.error('更新个人资料失败:', error)
      ElMessage.error('更新失败，请稍后重试')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 更改密码
   * @param {Object} passwordData - 包含当前密码和新密码的对象
   */
  const changePassword = async (passwordData) => {
    if (!token.value) return null
    
    loading.value = true
    
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // TODO: 实际的API调用
      // const response = await api.put('/user/password', passwordData)
      
      ElMessage.success('密码更新成功')
      return true
    } catch (error) {
      console.error('更改密码失败:', error)
      ElMessage.error('密码更新失败，请稍后重试')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 用户登出
   */
  const logout = () => {
    // 清除token
    token.value = ''
    localStorage.removeItem('token')
    
    // 清除用户信息
    user.value = null
    
    // TODO: 实际的API调用（如果需要）
    // api.post('/auth/logout')
  }
  
  return {
    // 状态
    token,
    user,
    loading,
    isLoggedIn,
    
    // 操作方法
    login,
    register,
    fetchUserProfile,
    updateProfile,
    changePassword,
    logout
  }
}) 