import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue'), meta: { title: '校园集市' } },
  { path: '/item/:id', name: 'item-detail', component: () => import('../views/ItemDetailView.vue'), meta: { title: '商品详情' } },
  { path: '/publish', name: 'publish', component: () => import('../views/PublishView.vue'), meta: { title: '发布闲置', auth: true } },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue'), meta: { title: '注册' } },
  { path: '/mine', name: 'mine', component: () => import('../views/MineView.vue'), meta: { title: '个人中心', auth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  const token = localStorage.getItem('cm_token')
  if (to.meta?.auth && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  document.title = to.meta?.title ? `${to.meta.title} · 校园集市` : '校园集市'
  return true
})

export default router
