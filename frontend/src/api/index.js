import http from './request'

export const authApi = {
  register: (data) => http.post('/auth/register', data),
  login: (data) => http.post('/auth/login', data)
}

export const userApi = {
  me: () => http.get('/user/me'),
  update: (data) => http.put('/user/me', data)
}

export const categoryApi = {
  list: () => http.get('/categories')
}

export const itemApi = {
  page: (params) => http.get('/items', { params }),
  mine: (params) => http.get('/items/mine', { params }),
  detail: (id) => http.get(`/items/${id}`),
  create: (data) => http.post('/items', data),
  update: (id, data) => http.put(`/items/${id}`, data),
  offShelf: (id) => http.post(`/items/${id}/off-shelf`),
  onShelf: (id) => http.post(`/items/${id}/on-shelf`)
}

export const orderApi = {
  create: (itemId) => http.post('/orders', { itemId }),
  list: (role) => http.get('/orders', { params: { role } }),
  cancel: (id) => http.post(`/orders/${id}/cancel`),
  confirm: (id) => http.post(`/orders/${id}/confirm`)
}

export const favoriteApi = {
  list: () => http.get('/favorites'),
  add: (itemId) => http.post(`/favorites/${itemId}`),
  remove: (itemId) => http.delete(`/favorites/${itemId}`)
}

export const statsApi = {
  summary: () => http.get('/stats/summary')
}
