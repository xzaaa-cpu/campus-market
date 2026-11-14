<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const ITEM_STATUS = {
  1: { text: '在售', type: 'success' },
  2: { text: '交易中', type: 'warning' },
  3: { text: '已售出', type: 'info' },
  4: { text: '已下架', type: 'danger' }
}

const ORDER_STATUS = {
  1: { text: '待确认', type: 'warning' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已取消', type: 'info' }
}

const sections = [
  { key: 'dashboard', label: '数据概览', icon: '📊' },
  { key: 'users', label: '用户与权限', icon: '👥' },
  { key: 'items', label: '商品审核', icon: '📦' },
  { key: 'orders', label: '全站订单', icon: '🧾' },
  { key: 'categories', label: '分类管理', icon: '🗂️' }
]

const section = ref('dashboard')
const loading = ref(false)

const summary = ref(null)
const trend = ref([])
const maxTrend = computed(() => {
  const all = trend.value.flatMap((t) => [t.items, t.orders, 0])
  return Math.max(1, ...all)
})

const users = ref([])
const userQuery = reactive({ keyword: '', role: '', page: 1, size: 10 })
const userTotal = ref(0)

const items = ref([])
const itemQuery = reactive({ keyword: '', status: null, page: 1, size: 10 })
const itemTotal = ref(0)

const orders = ref([])
const orderQuery = reactive({ status: null, page: 1, size: 10 })
const orderTotal = ref(0)

const categories = ref([])
const categoryDialog = ref(false)
const categoryForm = reactive({ id: null, name: '', icon: '✨', sort: 99 })
const categorySaving = ref(false)

async function loadDashboard() {
  loading.value = true
  try {
    const data = await adminApi.summary()
    summary.value = data
    trend.value = data.trend || []
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  loading.value = true
  try {
    const data = await adminApi.users({
      keyword: userQuery.keyword || undefined,
      role: userQuery.role || undefined,
      page: userQuery.page,
      size: userQuery.size
    })
    users.value = data.list
    userTotal.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadItems() {
  loading.value = true
  try {
    const data = await adminApi.items({
      keyword: itemQuery.keyword || undefined,
      status: itemQuery.status ?? undefined,
      page: itemQuery.page,
      size: itemQuery.size
    })
    items.value = data.list
    itemTotal.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadOrders() {
  loading.value = true
  try {
    const data = await adminApi.orders({
      status: orderQuery.status ?? undefined,
      page: orderQuery.page,
      size: orderQuery.size
    })
    orders.value = data.list
    orderTotal.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  loading.value = true
  try {
    categories.value = await adminApi.categories()
  } finally {
    loading.value = false
  }
}

function switchSection(key) {
  if (section.value === key) return
  section.value = key
  if (key === 'dashboard') loadDashboard()
  if (key === 'users') loadUsers()
  if (key === 'items') loadItems()
  if (key === 'orders') loadOrders()
  if (key === 'categories') loadCategories()
}

function searchUsers() {
  userQuery.page = 1
  loadUsers()
}

function searchItems() {
  itemQuery.page = 1
  loadItems()
}

async function toggleRole(row) {
  if (row.id === userStore.userId) {
    ElMessage.warning('不能修改自己的角色')
    return
  }
  const target = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  const label = target === 'ADMIN' ? '设为管理员' : '取消管理员'
  try {
    await ElMessageBox.confirm(`确认把「${row.nickname}」${label}？`, '修改角色', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await adminApi.updateRole(row.id, target)
  ElMessage.success(`${label}成功`)
  loadUsers()
}

async function changeItemStatus(row, status) {
  const label = status === 4 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确认${label}商品「${row.title}」？`, `${label}商品`, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await adminApi.changeItemStatus(row.id, status)
  ElMessage.success(`${label}成功`)
  loadItems()
}

function openCategoryDialog(row) {
  categoryForm.id = row?.id ?? null
  categoryForm.name = row?.name ?? ''
  categoryForm.icon = row?.icon ?? '✨'
  categoryForm.sort = row?.sort ?? 99
  categoryDialog.value = true
}

async function saveCategory() {
  if (!categoryForm.name.trim()) {
    ElMessage.warning('分类名称不能为空')
    return
  }
  categorySaving.value = true
  try {
    const payload = { name: categoryForm.name.trim(), icon: categoryForm.icon, sort: categoryForm.sort }
    if (categoryForm.id) {
      await adminApi.updateCategory(categoryForm.id, payload)
      ElMessage.success('分类已更新')
    } else {
      await adminApi.createCategory(payload)
      ElMessage.success('分类已新增')
    }
    categoryDialog.value = false
    loadCategories()
  } finally {
    categorySaving.value = false
  }
}

async function removeCategory(row) {
  try {
    await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '删除分类', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  try {
    await adminApi.deleteCategory(row.id)
    ElMessage.success('分类已删除')
    loadCategories()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

function money(value) {
  return '¥' + Number(value || 0).toFixed(2)
}

onMounted(loadDashboard)
</script>

<template>
  <div class="admin page">
    <aside class="admin-side">
      <div class="side-title">
        <span class="badge">ADMIN</span>
        <p>校园集市 · 管理后台</p>
      </div>
      <div
        v-for="s in sections"
        :key="s.key"
        class="side-item"
        :class="{ active: section === s.key }"
        @click="switchSection(s.key)"
      >
        <span class="side-icon">{{ s.icon }}</span>
        <span>{{ s.label }}</span>
      </div>
      <div class="side-foot">
        <div class="side-user">{{ userStore.displayName }}</div>
        <el-button text size="small" @click="router.push('/')">返回前台</el-button>
      </div>
    </aside>

    <main class="admin-main">
      <!-- 数据概览 -->
      <section v-if="section === 'dashboard'" v-loading="loading">
        <div class="main-head">
          <div>
            <h2>数据概览</h2>
            <p>平台当前整体运营情况，右侧为最近 7 天新增与成交趋势。</p>
          </div>
          <el-button size="small" @click="loadDashboard">刷新</el-button>
        </div>

        <div v-if="summary" class="stat-grid">
          <div class="stat-card">
            <span class="stat-label">注册用户</span>
            <strong>{{ summary.userCount }}</strong>
            <small>其中管理员 {{ summary.adminCount }} 人</small>
          </div>
          <div class="stat-card">
            <span class="stat-label">商品总数</span>
            <strong>{{ summary.itemCount }}</strong>
            <small>在售 {{ summary.onSaleCount }} · 交易中 {{ summary.tradingCount }}</small>
          </div>
          <div class="stat-card">
            <span class="stat-label">已下架</span>
            <strong>{{ summary.offShelfCount }}</strong>
            <small>已售出 {{ summary.soldCount }}</small>
          </div>
          <div class="stat-card">
            <span class="stat-label">订单总数</span>
            <strong>{{ summary.orderCount }}</strong>
            <small>已完成 {{ summary.finishedOrderCount }}</small>
          </div>
          <div class="stat-card accent">
            <span class="stat-label">成交总额 GMV</span>
            <strong>{{ money(summary.gmv) }}</strong>
            <small>仅统计已完成订单</small>
          </div>
        </div>

        <div class="card trend-card">
          <h3>最近 7 天</h3>
          <div class="chart">
            <div v-for="t in trend" :key="t.date" class="chart-col">
              <div class="bars">
                <div class="bar bar-item" :style="{ height: (t.items / maxTrend) * 100 + '%' }" :title="`新增商品 ${t.items}`"></div>
                <div class="bar bar-order" :style="{ height: (t.orders / maxTrend) * 100 + '%' }" :title="`成交订单 ${t.orders}`"></div>
              </div>
              <span class="chart-date">{{ t.date }}</span>
            </div>
          </div>
          <div class="legend">
            <span><i class="dot dot-item"></i>新增商品</span>
            <span><i class="dot dot-order"></i>成交订单</span>
          </div>
        </div>
      </section>

      <!-- 用户与权限 -->
      <section v-else-if="section === 'users'" v-loading="loading">
        <div class="main-head">
          <div>
            <h2>用户与权限</h2>
            <p>共 {{ userTotal }} 位用户，管理员可直接调整角色。</p>
          </div>
          <div class="head-actions">
            <el-input v-model="userQuery.keyword" placeholder="用户名 / 昵称" clearable style="width: 180px" @keyup.enter="searchUsers" />
            <el-select v-model="userQuery.role" placeholder="全部角色" clearable style="width: 120px" @change="searchUsers">
              <el-option label="普通用户" value="USER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
            <el-button type="primary" @click="searchUsers">查询</el-button>
          </div>
        </div>

        <div class="card">
          <el-table :data="users" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="username" label="用户名" width="130" />
            <el-table-column prop="nickname" label="昵称" width="130" />
            <el-table-column prop="intro" label="简介" show-overflow-tooltip />
            <el-table-column label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
                  {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="170" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button
                  size="small"
                  :type="row.role === 'ADMIN' ? 'info' : 'primary'"
                  :disabled="row.id === userStore.userId"
                  @click="toggleRole(row)"
                >
                  {{ row.role === 'ADMIN' ? '取消管理员' : '设为管理员' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            layout="prev, pager, next, total"
            :total="userTotal"
            :page-size="userQuery.size"
            :current-page="userQuery.page"
            @current-change="(p) => { userQuery.page = p; loadUsers() }"
          />
        </div>
      </section>

      <!-- 商品审核 -->
      <section v-else-if="section === 'items'" v-loading="loading">
        <div class="main-head">
          <div>
            <h2>商品审核</h2>
            <p>共 {{ itemTotal }} 件商品，可对违规或失效商品强制下架。</p>
          </div>
          <div class="head-actions">
            <el-input v-model="itemQuery.keyword" placeholder="商品标题 / 卖家昵称" clearable style="width: 200px" @keyup.enter="searchItems" />
            <el-select v-model="itemQuery.status" placeholder="全部状态" clearable style="width: 130px" @change="searchItems">
              <el-option label="在售" :value="1" />
              <el-option label="交易中" :value="2" />
              <el-option label="已售出" :value="3" />
              <el-option label="已下架" :value="4" />
            </el-select>
            <el-button type="primary" @click="searchItems">查询</el-button>
          </div>
        </div>

        <div class="card">
          <el-table :data="items" stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column label="商品" min-width="170">
              <template #default="{ row }">
                <div class="item-cell">
                  <span class="item-emoji">{{ row.categoryIcon || '📦' }}</span>
                  <div>
                    <div class="item-title" @click="router.push(`/item/${row.id}`)">{{ row.title }}</div>
                    <small>{{ row.categoryName }}</small>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="价格" width="100">
              <template #default="{ row }">{{ money(row.price) }}</template>
            </el-table-column>
            <el-table-column prop="sellerNickname" label="卖家" width="90" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="ITEM_STATUS[row.status]?.type" size="small">{{ ITEM_STATUS[row.status]?.text }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览" width="70" />
            <el-table-column prop="createdAt" label="发布时间" width="160" />
            <el-table-column label="操作" width="105" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 1"
                  size="small"
                  type="danger"
                  @click="changeItemStatus(row, 4)"
                >
                  下架
                </el-button>
                <el-button
                  v-else-if="row.status === 4"
                  size="small"
                  type="success"
                  @click="changeItemStatus(row, 1)"
                >
                  上架
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            layout="prev, pager, next, total"
            :total="itemTotal"
            :page-size="itemQuery.size"
            :current-page="itemQuery.page"
            @current-change="(p) => { itemQuery.page = p; loadItems() }"
          />
        </div>
      </section>

      <!-- 全站订单 -->
      <section v-else-if="section === 'orders'" v-loading="loading">
        <div class="main-head">
          <div>
            <h2>全站订单</h2>
            <p>共 {{ orderTotal }} 笔订单，可按状态筛选查看交易明细。</p>
          </div>
          <div class="head-actions">
            <el-select v-model="orderQuery.status" placeholder="全部状态" clearable style="width: 130px" @change="() => { orderQuery.page = 1; loadOrders() }">
              <el-option label="待确认" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已取消" :value="3" />
            </el-select>
            <el-button type="primary" @click="loadOrders">刷新</el-button>
          </div>
        </div>

        <div class="card">
          <el-table :data="orders" stripe>
            <el-table-column label="订单号 / 下单时间" width="190">
              <template #default="{ row }">
                <div class="order-no">{{ row.orderNo }}</div>
                <small class="order-time">{{ row.createdAt }}</small>
              </template>
            </el-table-column>
            <el-table-column prop="itemTitle" label="商品" min-width="200" show-overflow-tooltip />
            <el-table-column label="金额" width="110">
              <template #default="{ row }">{{ money(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="买家 → 卖家" width="150">
              <template #default="{ row }">{{ row.buyerNickname }} → {{ row.sellerNickname }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="ORDER_STATUS[row.status]?.type" size="small">{{ ORDER_STATUS[row.status]?.text }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="router.push(`/item/${row.itemId}`)">商品</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            layout="prev, pager, next, total"
            :total="orderTotal"
            :page-size="orderQuery.size"
            :current-page="orderQuery.page"
            @current-change="(p) => { orderQuery.page = p; loadOrders() }"
          />
        </div>
      </section>

      <!-- 分类管理 -->
      <section v-else-if="section === 'categories'" v-loading="loading">
        <div class="main-head">
          <div>
            <h2>分类管理</h2>
            <p>共 {{ categories.length }} 个分类，删除前需保证分类下没有商品。</p>
          </div>
          <el-button type="primary" @click="openCategoryDialog(null)">新增分类</el-button>
        </div>

        <div class="card">
          <el-table :data="categories" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column label="图标" width="80">
              <template #default="{ row }"><span class="cat-icon">{{ row.icon }}</span></template>
            </el-table-column>
            <el-table-column prop="name" label="分类名称" min-width="160" />
            <el-table-column prop="sort" label="排序" width="90" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openCategoryDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="removeCategory(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-dialog v-model="categoryDialog" :title="categoryForm.id ? '编辑分类' : '新增分类'" width="420px">
          <el-form label-width="80px">
            <el-form-item label="名称">
              <el-input v-model="categoryForm.name" maxlength="30" placeholder="如：数码电子" />
            </el-form-item>
            <el-form-item label="图标">
              <el-input v-model="categoryForm.icon" maxlength="20" placeholder="一个 emoji，如 💻" />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="categoryForm.sort" :min="1" :max="99" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="categoryDialog = false">取消</el-button>
            <el-button type="primary" :loading="categorySaving" @click="saveCategory">保存</el-button>
          </template>
        </el-dialog>
      </section>
    </main>
  </div>
</template>

<style scoped>
.admin {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.admin-side {
  width: 220px;
  flex: none;
  background: #1f3a34;
  border-radius: 18px;
  padding: 20px 14px;
  color: #e9f3ef;
  position: sticky;
  top: 88px;
}

.side-title {
  padding: 0 8px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.badge {
  display: inline-block;
  font-size: 10px;
  letter-spacing: 2px;
  background: var(--brand);
  color: #fff;
  padding: 2px 8px;
  border-radius: 999px;
}

.side-title p {
  margin: 10px 0 0;
  font-size: 13px;
  color: #9fc0b4;
}

.side-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  margin-top: 6px;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.side-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.side-item.active {
  background: var(--brand);
  color: #fff;
}

.side-icon {
  width: 18px;
  text-align: center;
}

.side-foot :deep(.el-button) {
  color: #9fc0b4;
}

.side-foot {
  margin-top: 22px;
  padding-top: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
  text-align: center;
}

.side-user {
  font-size: 13px;
  margin-bottom: 6px;
  color: #c9ded6;
}

.admin-main {
  flex: 1;
  min-width: 0;
}

.main-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.main-head h2 {
  margin: 0;
  font-size: 20px;
}

.main-head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--ink-3);
}

.head-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-card.accent {
  background: linear-gradient(135deg, #fff6f1, #ffefd9);
  border-color: #ffd8c2;
}

.stat-label {
  font-size: 12px;
  color: var(--ink-3);
}

.stat-card strong {
  font-size: 26px;
  line-height: 1.2;
}

.stat-card small {
  font-size: 12px;
  color: var(--ink-3);
}

.trend-card {
  padding: 18px 20px;
}

.trend-card h3 {
  margin: 0 0 16px;
  font-size: 15px;
}

.chart {
  display: flex;
  align-items: flex-end;
  gap: 18px;
  height: 160px;
}

.chart-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: flex-end;
  gap: 8px;
}

.bars {
  display: flex;
  gap: 6px;
  align-items: flex-end;
  height: 130px;
  width: 100%;
  justify-content: center;
}

.bar {
  width: 16px;
  border-radius: 6px 6px 0 0;
  min-height: 3px;
  transition: height 0.4s;
}

.bar-item {
  background: linear-gradient(180deg, #ffb088, var(--brand));
}

.bar-order {
  background: linear-gradient(180deg, #9fe3d9, var(--teal));
}

.chart-date {
  font-size: 12px;
  color: var(--ink-3);
}

.legend {
  display: flex;
  gap: 18px;
  margin-top: 14px;
  font-size: 12px;
  color: var(--ink-2);
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.dot-item {
  background: var(--brand);
}

.dot-order {
  background: var(--teal);
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-emoji {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #f4f1ee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
}

.item-title {
  font-size: 13px;
  color: var(--ink);
  cursor: pointer;
}

.item-title:hover {
  color: var(--brand);
  text-decoration: underline;
}

.item-cell small {
  color: var(--ink-3);
  font-size: 11px;
}

.order-no {
  font-size: 12px;
  letter-spacing: -0.2px;
}

.order-time {
  font-size: 11px;
  color: var(--ink-3);
}

.cat-icon {
  font-size: 18px;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}

.card.el-loading-parent--relative {
  min-height: 120px;
}

@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  }
}

@media (max-width: 900px) {
  .admin {
    flex-direction: column;
  }
  .admin-side {
    width: 100%;
    position: static;
  }
  .chart {
    gap: 8px;
  }
}
</style>
