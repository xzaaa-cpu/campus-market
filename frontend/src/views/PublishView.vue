<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { categoryApi, itemApi } from '../api'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const categories = ref([])
const loading = ref(false)
const editId = ref(route.query.id ? Number(route.query.id) : null)

const conditions = ['全新', '几乎全新', '轻微使用', '明显使用']

const form = reactive({
  title: '',
  description: '',
  price: null,
  originalPrice: null,
  categoryId: null,
  imageUrl: '',
  itemCondition: '几乎全新',
  location: ''
})

const rules = {
  title: [
    { required: true, message: '请填写商品标题', trigger: 'blur' },
    { max: 100, message: '标题最多 100 个字', trigger: 'blur' }
  ],
  price: [{ required: true, message: '请填写价格', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const pageTitle = computed(() => (editId.value ? '编辑闲置' : '发布闲置'))

async function loadCategories() {
  categories.value = await categoryApi.list()
}

async function loadDetail() {
  if (!editId.value) return
  const data = await itemApi.detail(editId.value)
  Object.assign(form, {
    title: data.title,
    description: data.description,
    price: Number(data.price),
    originalPrice: data.originalPrice ? Number(data.originalPrice) : null,
    categoryId: data.categoryId,
    imageUrl: data.imageUrl || '',
    itemCondition: data.itemCondition || '几乎全新',
    location: data.location || ''
  })
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  loading.value = true
  try {
    const payload = {
      ...form,
      price: Number(form.price),
      originalPrice: form.originalPrice ? Number(form.originalPrice) : null
    }
    if (editId.value) {
      await itemApi.update(editId.value, payload)
      ElMessage.success('修改已保存')
    } else {
      await itemApi.create(payload)
      ElMessage.success('发布成功，同学已经能看到你的闲置啦')
    }
    router.push('/mine')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  await loadDetail()
})
</script>

<template>
  <main class="page publish">
    <div class="head">
      <h1>{{ pageTitle }}</h1>
      <p>信息填得越清楚，越容易成交。成交后平台不收任何佣金。</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form-card">
      <el-form-item label="商品标题" prop="title">
        <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="例：iPad Air 4 64G 深空灰 九成新" />
      </el-form-item>

      <div class="row">
        <el-form-item label="分类" prop="categoryId" class="col">
          <el-select v-model="form.categoryId" placeholder="请选择商品分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="`${c.icon} ${c.name}`" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="成色" class="col">
          <el-select v-model="form.itemCondition" style="width: 100%">
            <el-option v-for="c in conditions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </div>

      <div class="row">
        <el-form-item label="售价（元）" prop="price" class="col">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="5" style="width: 100%" />
        </el-form-item>

        <el-form-item label="原价（元，选填）" class="col">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
      </div>

      <el-form-item label="交易地点">
        <el-input v-model="form.location" maxlength="100" placeholder="例：嘉庚学院 · 图书馆门口" />
      </el-form-item>

      <el-form-item label="图片链接（选填）">
        <el-input v-model="form.imageUrl" placeholder="粘贴一张图片 URL，留空则显示分类占位图" />
      </el-form-item>

      <el-form-item label="商品描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="6"
          maxlength="2000"
          show-word-limit
          placeholder="写清楚购买时间、使用情况、配件是否齐全、为什么出掉……"
        />
      </el-form-item>

      <div class="form-actions">
        <el-button round @click="router.push('/')">取消</el-button>
        <el-button type="primary" round :loading="loading" @click="submit">
          {{ editId ? '保存修改' : '立即发布' }}
        </el-button>
      </div>
    </el-form>
  </main>
</template>

<style scoped>
.publish {
  padding-top: 28px;
  padding-bottom: 48px;
}

.head h1 {
  margin: 0 0 6px;
  font-size: 24px;
}

.head p {
  margin: 0 0 20px;
  color: var(--ink-3);
  font-size: 13px;
}

.form-card {
  background: #fff;
  border-radius: 18px;
  box-shadow: var(--shadow);
  padding: 28px 32px;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 6px;
}

@media (max-width: 720px) {
  .row {
    grid-template-columns: 1fr;
    gap: 0;
  }
  .form-card {
    padding: 20px 18px;
  }
}
</style>
