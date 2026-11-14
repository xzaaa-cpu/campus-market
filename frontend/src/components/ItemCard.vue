<script setup>
import { computed } from 'vue'

const props = defineProps({
  item: { type: Object, required: true },
  showStatus: { type: Boolean, default: false }
})

const likeCount = computed(() => props.item.viewCount || 0)

const gradient = computed(() => {
  const palette = [
    'linear-gradient(135deg, #ffe3d3, #ffb28a)',
    'linear-gradient(135deg, #d9f5f0, #96dfd3)',
    'linear-gradient(135deg, #eef1ff, #c9d3ff)',
    'linear-gradient(135deg, #fff3cf, #ffd98a)',
    'linear-gradient(135deg, #ffe0ea, #ffb3c8)'
  ]
  const id = Number(props.item.id) || 0
  return palette[id % palette.length]
})

const conditionMap = {
  全新: { bg: '#e8f8f1', color: '#0f9d76' },
  几乎全新: { bg: '#eaf3ff', color: '#2b6cb0' },
  轻微使用: { bg: '#fff5e6', color: '#b9741a' },
  明显使用: { bg: '#f4f4f6', color: '#6b7280' }
}

const conditionStyle = computed(() => {
  const style = conditionMap[props.item.itemCondition]
  return style ? { background: style.bg, color: style.color } : {}
})

const statusText = computed(() => {
  const map = { 1: '在售', 2: '交易中', 3: '已售出', 4: '已下架' }
  return map[props.item.status] || ''
})
</script>

<template>
  <article class="item-card">
    <div class="thumb">
      <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.title" loading="lazy" />
      <div v-else class="thumb-fallback" :style="{ background: gradient }">
        <span class="thumb-icon">{{ item.categoryIcon || '🎁' }}</span>
        <span class="thumb-title">{{ item.title }}</span>
      </div>
      <span v-if="showStatus" class="status-tag" :class="`s-${item.status}`">{{ statusText }}</span>
    </div>

    <div class="body">
      <h3 class="title ellipsis-2">{{ item.title }}</h3>
      <div class="price-line">
        <span class="price"><span class="price-symbol">¥</span>{{ Number(item.price).toFixed(2) }}</span>
        <span v-if="item.originalPrice" class="origin">¥{{ Number(item.originalPrice).toFixed(0) }}</span>
      </div>
      <div class="meta">
        <span v-if="item.itemCondition" class="cond" :style="conditionStyle">{{ item.itemCondition }}</span>
        <span class="muted ellipsis loc">📍 {{ item.location || '校内面交' }}</span>
      </div>
      <div class="foot">
        <span class="seller">{{ (item.sellerNickname || '同学').slice(0, 1) }} · {{ item.sellerNickname || '匿名同学' }}</span>
        <span class="muted views">👁 {{ likeCount }}</span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.item-card {
  background: #fff;
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
  display: flex;
  flex-direction: column;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.thumb {
  position: relative;
  aspect-ratio: 4 / 3;
  background: #f7f7f9;
  overflow: hidden;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  text-align: center;
}

.thumb-icon {
  font-size: 34px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.08));
}

.thumb-title {
  font-size: 12px;
  color: rgba(34, 36, 42, 0.55);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.status-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--ink-2);
}

.status-tag.s-1 {
  color: var(--brand-dark);
}

.status-tag.s-3,
.status-tag.s-4 {
  color: var(--ink-3);
}

.body {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title {
  margin: 0;
  font-size: 14px;
  line-height: 1.45;
  min-height: 40px;
  color: var(--ink);
  font-weight: 600;
}

.price-line {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price {
  font-size: 20px;
}

.origin {
  font-size: 12px;
  color: var(--ink-3);
  text-decoration: line-through;
}

.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 22px;
}

.cond {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  white-space: nowrap;
}

.loc {
  font-size: 12px;
}

.foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px dashed var(--line);
  font-size: 12px;
}

.seller {
  color: var(--ink-2);
  max-width: 65%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.views {
  font-size: 12px;
}
</style>
