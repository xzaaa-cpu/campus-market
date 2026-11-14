<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const initial = computed(() => (userStore.displayName || 'U').slice(0, 1).toUpperCase())

function handleCommand(command) {
  if (command === 'mine') {
    router.push('/mine')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  }
}

function go(path) {
  router.push(path)
}
</script>

<template>
  <header class="app-header">
    <div class="page header-inner">
      <div class="brand" @click="go('/')">
        <span class="brand-logo">🎒</span>
        <div class="brand-text">
          <strong>校园集市</strong>
          <small>Campus Market</small>
        </div>
      </div>

      <nav class="nav">
        <span class="nav-item" @click="go('/')">首页</span>
        <span class="nav-item" @click="go('/publish')">发布闲置</span>
        <span class="nav-item" @click="go('/mine')">我的</span>
      </nav>

      <div class="header-right">
        <template v-if="userStore.isLogin">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-chip">
              <span class="avatar">{{ initial }}</span>
              <span class="user-name">{{ userStore.displayName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="mine">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="go('/login')">登录</el-button>
          <el-button type="primary" round @click="go('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>

  <router-view />
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 30;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--line);
}

.header-inner {
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.brand-logo {
  font-size: 26px;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.brand-text strong {
  font-size: 17px;
  letter-spacing: 1px;
}

.brand-text small {
  font-size: 10px;
  color: var(--ink-3);
  letter-spacing: 1.6px;
}

.nav {
  display: flex;
  gap: 28px;
  font-size: 14px;
  color: var(--ink-2);
  flex: 1;
  justify-content: center;
}

.nav-item {
  cursor: pointer;
  padding: 6px 2px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.nav-item:hover {
  color: var(--brand);
  border-bottom-color: var(--brand);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px 6px 6px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s;
}

.user-chip:hover {
  background: var(--brand-soft);
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--brand), #ffb088);
  color: #fff;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-name {
  font-size: 14px;
  color: var(--ink);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 720px) {
  .nav {
    gap: 14px;
    font-size: 13px;
  }
  .brand-text small {
    display: none;
  }
  .user-name {
    display: none;
  }
}
</style>
