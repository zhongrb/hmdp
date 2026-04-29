import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './assets/app.css'
import { installRouterGuards } from './router/guards'
import { routes } from './router/routes'

const app = createApp(App)
const pinia = createPinia()
const router = createRouter({
  history: createWebHistory(),
  routes,
})

app.use(pinia)
installRouterGuards(router)
app.use(router)
app.mount('#app')
