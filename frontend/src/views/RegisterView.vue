<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '' })

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ]
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await userStore.register({ ...form })
    ElMessage.success('注册成功，已自动登录')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">🎒 校园集市</div>
      <h1>注册账号</h1>
      <p class="muted">只要 10 秒，把你的闲置挂上货架</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" size="large" placeholder="3-20 位，登录时使用" />
        </el-form-item>
        <el-form-item label="昵称（选填）">
          <el-input v-model="form.nickname" size="large" maxlength="20" placeholder="同学们看到的名字" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            size="large"
            show-password
            placeholder="6-20 位"
            @keyup.enter="submit"
          />
        </el-form-item>

        <el-button type="primary" size="large" round class="submit" :loading="loading" @click="submit">
          注册并登录
        </el-button>
      </el-form>

      <div class="demo-tip">
        已经有账号？<a @click="router.push('/login')">去登录</a>
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
