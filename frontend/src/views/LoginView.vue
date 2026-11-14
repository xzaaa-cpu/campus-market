<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function fillDemo() {
  form.username = 'demo'
  form.password = '123456'
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await userStore.login({ ...form })
    ElMessage.success('登录成功，欢迎回来')
    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' ? redirect : '/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">🎒 校园集市</div>
      <h1>登录</h1>
      <p class="muted">登录后即可下单、收藏和发布闲置</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" size="large" placeholder="请输入用户名" @keyup.enter="submit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            size="large"
            show-password
            placeholder="请输入密码"
            @keyup.enter="submit"
          />
        </el-form-item>

        <el-button type="primary" size="large" round class="submit" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>

      <div class="demo-tip">
        没有账号？<a @click="router.push('/register')">立即注册</a>
        <el-divider direction="vertical" />
        <a @click="fillDemo">一键填入演示账号 demo / 123456</a>
      </div>
    </div>
  </main>
</template>

<style scoped>
.auth-page {
  min-height: calc(100vh - 68px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: radial-gradient(1000px 500px at 20% 10%, #fff1e8, transparent),
    radial-gradient(800px 400px at 90% 90%, #e9fbf7, transparent), var(--bg);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 34px 32px 26px;
}

.auth-brand {
  font-size: 13px;
  color: var(--brand-dark);
  letter-spacing: 1px;
  margin-bottom: 18px;
}

.auth-card h1 {
  margin: 0 0 6px;
  font-size: 24px;
}

.auth-card > p {
  margin: 0 0 22px;
  font-size: 13px;
}

.submit {
  width: 100%;
  margin-top: 6px;
}

.demo-tip {
  margin-top: 18px;
  font-size: 12px;
  color: var(--ink-3);
  text-align: center;
}

.demo-tip a {
  color: var(--brand-dark);
  cursor: pointer;
}
</style>
