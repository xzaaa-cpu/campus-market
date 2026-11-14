<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { favoriteApi, itemApi, orderApi } from '../api'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const item = ref(null)
const loading = ref(true)
const submitting = ref(false)

const palettes = [
  'linear-gradient(135deg, #ffe3d3, #ffb28a)',
  'linear-gradient(135deg, #d9f5f0, #96dfd3)',
  'linear-gradient(135deg, #eef1ff, #c9d3ff)',
  'linear-gradient(135deg, #fff3cf, #ffd98a)',
  'linear-gradient(135deg, #ffe0ea, #ffb3c8)'
]

const fallbackBg = computed(() => {
  if (!item.value) return palettes[0]
  return palettes[Number(item.value.id) % palettes.length]
})

const statusText = computed(() => {
  const map = { 1: '在售', 2: '交易中', 3: '已售出', 4: '已下架' }
  return map[item.value?.status] || ''
})

const discount = computed(() => {
  if (!item.value?.originalPrice) return null
  const rate = (Number(item.value.price) / Number(item.value.originalPrice)) * 10
  if (!isFinite(rate) || rate >= 10) return null
  return rate.toFixed(1)
})

const isOwner = computed(() => item.value && item.value.sellerId === userStore.userId)

async function load() {
  loading.value = true
  try {
    item.value = await itemApi.detail(route.params.id)
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    loading.value = false
  }
}

async function buy() {
  if (!userStore.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (isOwner.value) {
    ElMessage.warning('不能购买自己发布的闲置')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认以 ¥${Number(item.value.price).toFixed(2)} 下单购买「${item.value.title}」？下单后请主动联系卖家约定面交时间和地点。`,
      '确认下单',
      { confirmButtonText: '确认下单', cancelButtonText: '再想想', type: 'warning' }
    )
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    await orderApi.create(item.value.id)
    ElMessage.success('下单成功，请到「我的 - 我买到的」查看订单')
    await load()
  } finally {
    submitting.value = false
  }
}

async function toggleFavorite() {
  if (!userStore.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (item.value.favorited) {
    await favoriteApi.remove(item.value.id)
    item.value.favorited = false
    ElMessage.success('已取消收藏')
  } else {
    await favoriteApi.add(item.value.id)
    item.value.favorited = true
    ElMessage.success('已加入收藏')
  }
}

onMounted(load)
</script>

<template>
  <main class="page detail" v-loading="loading">
    <div class="crumb">
      <span @click="router.push('/')">首页</span>
      <span class="sep">/</span>
      <span>{{ item?.categoryName || '闲置' }}</span>
      <span class="sep">/</span>
      <span class="current">详情</span>
    </div>

    <section v-if="item" class="detail-card">
      <div class="gallery">
        <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.title" />
        <div v-else class="gallery-fallback" :style="{ background: fallbackBg }">
          <span class="big-icon">{{ item.categoryIcon || '🎁' }}</span>
          <span class="gallery-tip">卖家暂未上传实物图</span>
        </div>
      </div>

      <div class="info">
        <div class="title-row">
          <h1>{{ item.title }}</h1>
          <span class="status" :class="`s-${item.status}`">{{ statusText }}</span>
        </div>

        <div class="price-box">
          <div class="price-main">
            <span class="price"><span class="price-symbol">¥</span>{{ Number(item.price).toFixed(2) }}</span>
            <span v-if="item.originalPrice" class="origin">原价 ¥{{ Number(item.originalPrice).toFixed(2) }}</span>
            <span v-if="discount" class="pill">约 {{ discount }} 折</span>
          </div>
          <div class="view-meta muted">👁 {{ item.viewCount }} 次浏览 · 发布于 {{ item.createdAt }}</div>
        </div>

        <div class="tags">
          <span v-if="item.itemCondition" class="tag">成色 · {{ item.itemCondition }}</span>
          <span v-if="item.categoryName" class="tag">{{ item.categoryIcon }} {{ item.categoryName }}</span>
          <span v-if="item.location" class="tag">📍 {{ item.location }}</span>
        </div>

        <div class="seller">
          <div class="seller-avatar">{{ (item.sellerNickname || '同').slice(0, 1) }}</div>
          <div class="seller-info">
            <strong>{{ item.sellerNickname || '匿名同学' }}</strong>
            <span class="muted">卖家 · 支持当面验货</span>
          </div>
          <span class="pill">校内认证</span>
        </div>

        <div class="desc">
          <h3>商品描述</h3>
          <p>{{ item.description || '卖家很懒，没有写描述，可以直接联系问问～' }}</p>
        </div>

        <div class="actions">
          <el-button :type="item.favorited ? 'success' : 'default'" plain round size="large" @click="toggleFavorite">
            {{ item.favorited ? '★ 已收藏' : '☆ 收藏' }}
          </el-button>
          <el-button
            type="primary"
            round
            size="large"
            :loading="submitting"
            :disabled="item.status !== 1 || isOwner"
            @click="buy"
          >
            {{ isOwner ? '这是你发布的闲置' : item.status === 1 ? '立即购买' : '暂不可购买' }}
          </el-button>
        </div>
      </div>
    </section>

    <el-empty v-else-if="!loading" description="商品不存在或已被删除" />

    <aside class="tips">
      <h4>交易提示</h4>
      <ul>
        <li>建议在校内公共场所当面交易，先验货再付款。</li>
        <li>不要提前转账，不要点陌生链接。</li>
        <li>下单后如需取消，可在「我的 - 我买到的」中操作。</li>
      </ul>
    </aside>
  </main>
</template>

<style scoped>
.detail {
  padding-top: 20px;
  padding-bottom: 40px;
}

.crumb {
  font-size: 13px;
  color: var(--ink-3);
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}

.crumb span {
  cursor: pointer;
}

.crumb .current {
  color: var(--ink-2);
  cursor: default;
}

.detail-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 24px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.1fr);
  gap: 32px;
}

.gallery {
  border-radius: 16px;
  overflow: hidden;
  background: #f7f7f9;
  aspect-ratio: 1 / 1;
}

.gallery img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gallery-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.big-icon {
  font-size: 64px;
}

.gallery-tip {
  font-size: 13px;
  color: rgba(34, 36, 42, 0.5);
}

.info {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.title-row h1 {
  font-size: 22px;
  line-height: 1.4;
  margin: 0;
  flex: 1;
}

.status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  background: var(--brand-soft);
  color: var(--brand-dark);
  white-space: nowrap;
}

.status.s-2,
.status.s-3,
.status.s-4 {
  background: #f3f4f6;
  color: var(--ink-3);
}

.price-box {
  background: linear-gradient(100deg, #fff6f1, #fff);
  border-radius: 14px;
  padding: 16px 18px;
}

.price-main {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price-main .price {
  font-size: 32px;
}

.origin {
  color: var(--ink-3);
  text-decoration: line-through;
  font-size: 13px;
}

.view-meta {
  margin-top: 8px;
  font-size: 12px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  font-size: 12px;
  color: var(--ink-2);
  background: #f5f6f8;
  border-radius: 8px;
  padding: 6px 12px;
}

.seller {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 14px;
}

.seller-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #14b8a6, #6ee7d8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.seller-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  font-size: 12px;
}

.seller-info strong {
  font-size: 14px;
}

.desc h3 {
  font-size: 14px;
  margin: 0 0 10px;
  color: var(--ink);
}

.desc p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--ink-2);
  white-space: pre-wrap;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
  padding-top: 8px;
}

.actions .el-button {
  flex: 1;
  margin-left: 0;
}

.tips {
  margin-top: 20px;
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: var(--shadow);
}

.tips h4 {
  margin: 0 0 10px;
  font-size: 14px;
}

.tips ul {
  margin: 0;
  padding-left: 18px;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.9;
}

@media (max-width: 860px) {
  .detail-card {
    grid-template-columns: minmax(0, 1fr);
    gap: 20px;
  }
  .gallery {
    aspect-ratio: 4 / 3;
  }
}
</style>
