import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('cm_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code !== 'undefined') {
      if (body.code === 0) {
        return body.data
      }
      if (body.code === 401) {
        localStorage.removeItem('cm_token')
        localStorage.removeItem('cm_user')
        ElMessage.error(body.message || '登录已失效，请重新登录')
        if (window.location.pathname !== '/login') {
          setTimeout(() => {
            window.location.href = '/login'
          }, 600)
        }
        return Promise.reject(new Error(body.message))
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    const message = error?.response?.data?.message || error.message || '网络异常'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http
