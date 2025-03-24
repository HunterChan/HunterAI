import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API基础URL
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 在发送请求之前做一些处理，例如添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 处理请求错误
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 对响应数据做一些处理
    return response.data
  },
  error => {
    // 处理响应错误
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      if (status === 401) {
        // 处理未授权的情况，例如重定向到登录页面
        console.warn('Unauthorized, redirecting to login...')
        // router.push('/login')
      } else if (status === 403) {
        // 处理禁止访问的情况
        console.warn('Forbidden')
      } else if (status === 500) {
        // 处理服务器错误
        console.error('Server error')
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      console.error('No response from server')
    } else {
      // 设置请求时发生错误
      console.error('Request configuration error')
    }
    return Promise.reject(error)
  }
)

export default api 