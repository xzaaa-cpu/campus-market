import { defineStore } from 'pinia'
import { authApi, userApi } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('cm_token') || '',
    user: JSON.parse(localStorage.getItem('cm_user') || 'null')
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
    userId: (state) => state.user?.id || null,
    displayName: (state) => state.user?.nickname || state.user?.username || '未登录',
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    setAuth(payload) {
      this.token = payload.token
      this.user = payload.user
      localStorage.setItem('cm_token', payload.token)
      localStorage.setItem('cm_user', JSON.stringify(payload.user))
    },
    async login(form) {
      const data = await authApi.login(form)
      this.setAuth(data)
      return data
    },
    async register(form) {
      const data = await authApi.register(form)
      this.setAuth(data)
      return data
    },
    async refreshMe() {
      if (!this.token) return null
      const data = await userApi.me()
      this.user = data
      localStorage.setItem('cm_user', JSON.stringify(data))
      return data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('cm_token')
      localStorage.removeItem('cm_user')
    }
  }
})
