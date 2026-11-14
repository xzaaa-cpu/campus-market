<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ItemCard from '../components/ItemCard.vue'
import { categoryApi, itemApi, statsApi } from '../api'

const router = useRouter()
const loading = ref(false)
const categories = ref([])
const list = ref([])
const total = ref(0)
const stats = ref({})
const keyword = ref('')

const query = reactive({
  categoryId: null,
  sort: 'latest',
  page: 1,
  size: 12
})

const sorts = [
  { key: 'latest', label: '最新发布', icon: '🕒' },
  { key: 'popular', label: '最多浏览', icon: '🔥' },
  { key: 'priceAsc', label: '价格从低到高', icon: '📉' },
  { key: 'priceDesc', label: '价格从高到低', icon: '📈' }
]

async function loadItems() {
  loading.value = true
  try {
    const data = await itemApi.page({
      keyword: keyword.value || undefined,
      categoryId: query.categoryId || undefined,
      sort: query.sort,
      page: query.page,
      size: query.size
    })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function pickCategory(id) {
  query.categoryId = id
  query.page = 1
  loadItems()
}

function pickSort(key) {
  query.sort = key
  query.page = 1
  loadItems()
}

function doSearch() {
  query.page = 1
  loadItems()
}

function goDetail(item) {
  router.push({ name: 'item-detail', params: { id: item.id } })
}

function changePage(page) {
  query.page = page
  loadItems()
}

onMounted(async () => {
  loadItems()
  try {
    categories.value = await categoryApi.list()
    stats.value = await statsApi.summary()
  } catch (e) {
    ElMessage.warning('分类或统计数据加载失败，请确认后端已启动')
  }
})
</script>

<template>
  <main class="page home">
    <section class="hero">
      <div class="hero-copy">
        <div class="hero-tag">嘉庚学院 · 校内面交 · 0 佣金</div>
        <h1>把闲置交给<br /><em>刚好需要的同学</em></h1>
        <p>课本、数码、宿舍好物，发布一条闲置只要 30 秒。</p>

        <div class="search-bar">
          <span class="search-icon">🔍</span>
          <input
            v-model.trim="keyword"
            placeholder="搜一搜：iPad、高数教材、篮球……"
            @keyup.enter="doSearch"
          />
          <button class="search-btn" @click="doSearch">搜索</button>
        </div>

        <div class="hero-stats">
          <div class="stat">
            <strong>{{ stats.onSaleCount ?? '—' }}</strong>
            <span>件在售闲置</span>
          </div>
          <div class="stat">
            <strong>{{ stats.userCount ?? '—' }}</strong>
            <span>位同学</span>
          </div>
          <div class="stat">
            <strong>{{ stats.orderCount ?? '—' }}</strong>
            <span>笔成交</span>
          </div>
        </div>
      </div>

      <div class="hero-art">
        <div class="art-card a1">📚<span>教材 3 折出</span></div>
        <div class="art-card a2">💻<span>数码好价</span></div>
        <div class="art-card a3">🏀<span>运动装备</span></div>
        <div class="art-ring"></div>
      </div>
    </section>

    <div class="filter-bar">
      <div class="chips">
        <span class="chip" :class="{ active: query.categoryId === null }" @click="pickCategory(null)">全部</span>
        <span
          v-for="c in categories"
          :key="c.id"
          class="chip"
          :class="{ active: query.categoryId === c.id }"
          @click="pickCategory(c.id)"
        >
          <span class="chip-icon">{{ c.icon }}</span>{{ c.name }}
        </span>
      </div>
    </div>

    <div class="section-head">
      <h2>最新闲置</h2>
      <div class="sorts">
        <span
          v-for="s in sorts"
          :key="s.key"
          class="sort"
          :class="{ active: query.sort === s.key }"
          @click="pickSort(s.key)"
        >
          {{ s.label }}
        </span>
      </div>
    </div>

    <div v-loading="loading" class="grid-wrap">
      <div v-if="list.length" class="grid">
        <ItemCard v-for="item in list" :key="item.id" :item="item" @click="goDetail(item)" />
      </div>
      <el-empty v-else-if="!loading" description="暂时没有符合条件的闲置，换个关键词试试" />
    </div>

    <div v-if="total > 0" class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        @current-change="changePage"
      />
    </div>

    <section class="publish-cta">
      <div>
        <h3>你也有闲置吗？</h3>
        <p>1 分钟发布，同学直接联系你，交易不收佣金。</p>
      </div>
      <el-button type="primary" round size="large" @click="router.push('/publish')">发布闲置</el-button>
    </section>

    <footer class="foot">© 2026 校园集市 · 课程项目演示 · SpringBoot + Vue3</footer>
  </main>
</template>

<style scoped>
.home {
  padding-bottom: 40px;
}

.hero {
  margin-top: 24px;
  border-radius: 22px;
  background: linear-gradient(120deg, #fff4ec 0%, #ffe8d9 55%, #ffe3d2 100%);
  padding: 44px 48px;
  display: flex;
  align-items: center;
  gap: 40px;
  position: relative;
  overflow: hidden;
}

.hero-tag {
  display: inline-block;
  font-size: 12px;
  color: var(--brand-dark);
  background: rgba(255, 255, 255, 0.7);
  padding: 4px 12px;
  border-radius: 999px;
  margin-bottom: 18px;
  letter-spacing: 0.5px;
}

.hero-copy {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 2;
}

.hero-copy h1 {
  font-size: 38px;
  line-height: 1.3;
  margin: 0 0 12px;
  letter-spacing: 1px;
}

.hero-copy em {
  font-style: normal;
  color: var(--brand-dark);
  border-bottom: 6px solid rgba(255, 107, 53, 0.18);
}

.hero-copy p {
  margin: 0 0 22px;
  color: var(--ink-2);
  font-size: 14px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border-radius: 999px;
  padding: 6px 6px 6px 18px;
  max-width: 520px;
  box-shadow: 0 8px 22px rgba(232, 85, 31, 0.12);
}

.search-icon {
  font-size: 16px;
  opacity: 0.6;
}

.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  padding: 10px 0;
  background: transparent;
  color: var(--ink);
}

.search-btn {
  border: none;
  background: var(--brand);
  color: #fff;
  padding: 10px 22px;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-btn:hover {
  background: var(--brand-dark);
}

.hero-stats {
  display: flex;
  gap: 34px;
  margin-top: 26px;
}

.stat strong {
  display: block;
  font-size: 22px;
  color: var(--brand-dark);
}

.stat span {
  font-size: 12px;
  color: var(--ink-2);
}

.hero-art {
  width: 260px;
  height: 220px;
  position: relative;
  flex-shrink: 0;
}

.art-card {
  position: absolute;
  width: 120px;
  padding: 14px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 12px 26px rgba(34, 36, 42, 0.1);
  font-size: 26px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.art-card span {
  font-size: 12px;
  color: var(--ink-2);
}

.a1 {
  top: 0;
  left: 10px;
  transform: rotate(-6deg);
}

.a2 {
  top: 40px;
  right: 0;
  transform: rotate(7deg);
}

.a3 {
  bottom: 0;
  left: 54px;
  transform: rotate(3deg);
}

.art-ring {
  position: absolute;
  width: 140px;
  height: 140px;
  border: 2px dashed rgba(232, 85, 31, 0.35);
  border-radius: 50%;
  top: 40px;
  left: 40px;
}

.filter-bar {
  margin-top: 26px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 999px;
  background: #fff;
  color: var(--ink-2);
  font-size: 13px;
  cursor: pointer;
  border: 1px solid var(--line);
  transition: all 0.2s;
  user-select: none;
}

.chip:hover {
  border-color: #ffc9b2;
  color: var(--brand-dark);
}

.chip.active {
  background: var(--brand);
  border-color: var(--brand);
  color: #fff;
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.28);
}

.chip-icon {
  font-size: 14px;
}

.sorts {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--ink-3);
}

.sort {
  cursor: pointer;
  padding-bottom: 2px;
  border-bottom: 2px solid transparent;
}

.sort.active {
  color: var(--brand-dark);
  border-bottom-color: var(--brand);
  font-weight: 600;
}

.grid-wrap {
  min-height: 260px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.publish-cta {
  margin-top: 44px;
  background: linear-gradient(110deg, #16302b, #1f4a40);
  color: #fff;
  border-radius: 18px;
  padding: 30px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.publish-cta h3 {
  margin: 0 0 6px;
  font-size: 20px;
}

.publish-cta p {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
}

.foot {
  text-align: center;
  color: var(--ink-3);
  font-size: 12px;
  margin-top: 36px;
}

@media (max-width: 980px) {
  .grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .hero-art {
    display: none;
  }
}

@media (max-width: 640px) {
  .hero {
    padding: 28px 22px;
  }
  .hero-copy h1 {
    font-size: 26px;
  }
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }
  .publish-cta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
