<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ItemCard from '../components/ItemCard.vue'
import { favoriteApi, itemApi, orderApi, userApi } from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const tab = ref('items')
const myStatus = ref(null)
const loading = ref(false)
const items = ref([])
const bought = ref([])
const sold = ref([])
const favorites = ref([])

const editVisible = ref(false)
const editForm = reactive({ nickname: '', phone: '', avatar: '', intro: '' })

const profile = computed(() => userStore.user || {})
const initial = computed(() => (userStore.displayName || 'U').slice(0, 1))

const itemStatusText = { 1: '在售', 2: '交易中', 3: '已售出', 4: '已下架' }
const orderStatusText = { 1: '待确认', 2: '已完成', 3: '已取消' }

async function loadItems() {
  items.value = await itemApi.mine({ status: myStatus.value || undefined })
}

async function loadOrders(role) {
  const data = await orderApi.list(role)
  if (role === 'bought') {
    bought.value = data
  } else {
    sold.value = data
  }
}

async function loadFavorites() {
  favorites.value = await favoriteApi.list()
}

async function refresh() {
  loading.value = true
  try {
    if (tab.value === 'items') await loadItems()
    else if (tab.value === 'bought') await loadOrders('bought')
    else if (tab.value === 'sold') await loadOrders('sold')
    else await loadFavorites()
  } finally {
    loading.value = false
  }
}

function switchTab(name) {
  tab.value = name
  refresh()
}

function filterStatus(status) {
  myStatus.value = status
  loadItems()
}

async function offShelf(item) {
  await itemApi.offShelf(item.id)
  ElMessage.success('已下架')
  loadItems()
}

async function onShelf(item) {
  await itemApi.onShelf(item.id)
  ElMessage.success('已重新上架')
  loadItems()
}

async function cancelOrder(order) {
  try {
    await ElMessageBox.confirm('确认取消这笔订单吗？商品会重新回到在售状态。', '取消订单', {
      type: 'warning',
      confirmButtonText: '确认取消',
      cancelButtonText: '不取消'
    })
  } catch (e) {
    return
  }
  await orderApi.cancel(order.id)
  ElMessage.success('订单已取消')
  refresh()
}

async function confirmOrder(order) {
  try {
    await ElMessageBox.confirm('确认已经拿到商品并完成交易？确认后订单将标记为已完成。', '确认收货', {
      type: 'warning',
      confirmButtonText: '确认收货',
      cancelButtonText: '再等等'
    })
  } catch (e) {
    return
  }
  await orderApi.confirm(order.id)
  ElMessage.success('交易完成，感谢使用校园集市')
  refresh()
}

async function removeFavorite(item) {
  await favoriteApi.remove(item.id)
  ElMessage.success('已取消收藏')
  loadFavorites()
}

function openEdit() {
  Object.assign(editForm, {
    nickname: profile.value.nickname || '',
    phone: profile.value.phone || '',
    avatar: profile.value.avatar || '',
    intro: profile.value.intro || ''
  })
  editVisible.value = true
}

async function saveProfile() {
  await userApi.update({ ...editForm })
  await userStore.refreshMe()
  editVisible.value = false
  ElMessage.success('资料已更新')
}

onMounted(refresh)
</script>

<template>
  <main class="page mine">
    <section class="profile card">
      <div class="profile-avatar">{{ initial }}</div>
      <div class="profile-main">
        <div class="name-line">
          <h2>{{ profile.nickname || profile.username || '同学' }}</h2>
          <span class="pill">@{{ profile.username }}</span>
        </div>
        <p class="intro">{{ profile.intro || '还没有写简介' }}</p>
        <div class="profile-meta muted">
          <span v-if="profile.phone">📱 {{ profile.phone }}</span>
          <span>注册于 {{ (profile.createdAt || '').slice(0, 10) }}</span>
        </div>
      </div>
      <el-button round @click="openEdit">编辑资料</el-button>
    </section>

    <section class="panel">
      <el-tabs v-model="tab" @tab-change="switchTab">
        <el-tab-pane label="我发布的" name="items">
          <div class="status-filter">
            <span class="chip" :class="{ active: myStatus === null }" @click="filterStatus(null)">全部</span>
            <span class="chip" :class="{ active: myStatus === 1 }" @click="filterStatus(1)">在售</span>
            <span class="chip" :class="{ active: myStatus === 2 }" @click="filterStatus(2)">交易中</span>
            <span class="chip" :class="{ active: myStatus === 3 }" @click="filterStatus(3)">已售出</span>
            <span class="chip" :class="{ active: myStatus === 4 }" @click="filterStatus(4)">已下架</span>
          </div>

          <div v-loading="loading" class="rows">
            <div v-for="item in items" :key="item.id" class="row-item">
              <div class="thumb">
                <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.title" />
                <span v-else class="thumb-emoji">{{ item.categoryIcon || '🎁' }}</span>
              </div>
              <div class="row-main">
                <div class="row-title ellipsis">{{ item.title }}</div>
                <div class="row-sub muted">
                  <span class="price"><span class="price-symbol">¥</span>{{ Number(item.price).toFixed(2) }}</span>
                  <span>· 👁 {{ item.viewCount }}</span>
                  <span>· {{ item.createdAt }}</span>
                </div>
              </div>
              <span class="badge" :class="`b-${item.status}`">{{ itemStatusText[item.status] }}</span>
              <div class="row-actions">
                <el-button text @click="router.push({ path: '/item/' + item.id })">查看</el-button>
                <el-button v-if="item.status === 1 || item.status === 4" text @click="router.push({ name: 'publish', query: { id: item.id } })">
                  编辑
                </el-button>
                <el-button v-if="item.status === 1" text @click="offShelf(item)">下架</el-button>
                <el-button v-if="item.status === 4" text type="primary" @click="onShelf(item)">上架</el-button>
              </div>
            </div>
            <el-empty v-if="!loading && !items.length" description="还没有发布过闲置" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我买到的" name="bought">
          <div v-loading="loading" class="rows">
            <div v-for="order in bought" :key="order.id" class="row-item">
              <div class="thumb">
                <img v-if="order.itemImage" :src="order.itemImage" :alt="order.itemTitle" />
                <span v-else class="thumb-emoji">📦</span>
              </div>
              <div class="row-main">
                <div class="row-title ellipsis">{{ order.itemTitle }}</div>
                <div class="row-sub muted">
                  <span>订单号 {{ order.orderNo }}</span>
                  <span>· 卖家 {{ order.sellerNickname }}</span>
                  <span>· {{ order.createdAt }}</span>
                </div>
              </div>
              <div class="amount price">¥{{ Number(order.amount).toFixed(2) }}</div>
              <span class="badge" :class="`o-${order.status}`">{{ orderStatusText[order.status] }}</span>
              <div class="row-actions">
                <el-button v-if="order.status === 1" text type="primary" @click="confirmOrder(order)">确认收货</el-button>
                <el-button v-if="order.status === 1" text @click="cancelOrder(order)">取消订单</el-button>
                <el-button text @click="router.push({ path: '/item/' + order.itemId })">商品</el-button>
              </div>
            </div>
            <el-empty v-if="!loading && !bought.length" description="还没有购买记录，去首页逛逛吧" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我卖出的" name="sold">
          <div v-loading="loading" class="rows">
            <div v-for="order in sold" :key="order.id" class="row-item">
              <div class="thumb">
                <img v-if="order.itemImage" :src="order.itemImage" :alt="order.itemTitle" />
                <span v-else class="thumb-emoji">📦</span>
              </div>
              <div class="row-main">
                <div class="row-title ellipsis">{{ order.itemTitle }}</div>
                <div class="row-sub muted">
                  <span>买家 {{ order.buyerNickname }}</span>
                  <span>· {{ order.createdAt }}</span>
                </div>
              </div>
              <div class="amount price">¥{{ Number(order.amount).toFixed(2) }}</div>
              <span class="badge" :class="`o-${order.status}`">{{ orderStatusText[order.status] }}</span>
              <div class="row-actions">
                <el-button v-if="order.status === 1" text @click="cancelOrder(order)">取消订单</el-button>
                <el-button text @click="router.push({ path: '/item/' + order.itemId })">商品</el-button>
              </div>
            </div>
            <el-empty v-if="!loading && !sold.length" description="还没有卖出记录" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="favorites">
          <div v-loading="loading" class="grid">
            <div v-for="item in favorites" :key="item.id" class="grid-cell">
              <ItemCard :item="item" @click="router.push({ path: '/item/' + item.id })" />
              <button class="unfav" @click.stop="removeFavorite(item)">取消收藏</button>
            </div>
            <el-empty v-if="!loading && !favorites.length" description="还没有收藏任何闲置" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="editVisible" title="编辑资料" width="420px">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" maxlength="30" placeholder="方便买家联系你" />
        </el-form-item>
        <el-form-item label="头像链接">
          <el-input v-model="editForm.avatar" placeholder="留空则显示昵称首字" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="editForm.intro" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.mine {
  padding-top: 26px;
  padding-bottom: 48px;
}

.profile {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 26px 30px;
}

.profile-avatar {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--brand), #ffc4a3);
  color: #fff;
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.name-line {
  display: flex;
  align-items: center;
  gap: 10px;
}

.name-line h2 {
  margin: 0;
  font-size: 20px;
}

.intro {
  margin: 8px 0 6px;
  color: var(--ink-2);
  font-size: 13px;
}

.profile-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
}

.panel {
  margin-top: 22px;
  background: #fff;
  border-radius: 18px;
  box-shadow: var(--shadow);
  padding: 8px 26px 26px;
}

.status-filter {
  display: flex;
  gap: 10px;
  margin: 4px 0 16px;
}

.chip {
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid var(--line);
  color: var(--ink-2);
  cursor: pointer;
  user-select: none;
}

.chip.active {
  background: var(--brand);
  color: #fff;
  border-color: var(--brand);
}

.rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 120px;
}

.row-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 14px;
  transition: border-color 0.2s;
}

.row-item:hover {
  border-color: #ffc9b2;
}

.thumb {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  background: #f5f6f8;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-emoji {
  font-size: 26px;
}

.row-main {
  flex: 1;
  min-width: 0;
}

.row-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 6px;
}

.row-sub {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 12px;
}

.amount {
  font-size: 16px;
}

.badge {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 999px;
  background: #f3f4f6;
  color: var(--ink-3);
  white-space: nowrap;
}

.badge.b-1,
.badge.o-1 {
  background: var(--brand-soft);
  color: var(--brand-dark);
}

.badge.b-3,
.badge.o-2 {
  background: #e8f8f1;
  color: #0f9d76;
}

.row-actions {
  display: flex;
  gap: 2px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.grid-cell {
  position: relative;
}

.unfav {
  position: absolute;
  right: 10px;
  top: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.92);
  color: var(--ink-2);
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  cursor: pointer;
}

.unfav:hover {
  color: var(--brand-dark);
}

@media (max-width: 860px) {
  .profile {
    flex-wrap: wrap;
  }
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .row-item {
    flex-wrap: wrap;
  }
}
</style>
