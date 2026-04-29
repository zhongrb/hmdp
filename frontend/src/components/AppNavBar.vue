<template>
  <nav
    class="app-nav"
    aria-label="主导航"
  >
    <RouterLink
      v-for="item in items"
      :key="item.to"
      :to="item.to"
      class="app-nav__link"
      :class="{ 'is-active': isActive(item) }"
    >
      {{ item.label }}
    </RouterLink>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { copy } from '../constants/copy'

const route = useRoute()

const items = computed(() => [
  { to: '/', label: copy.nav.home, names: ['home'] },
  { to: '/shops', label: copy.nav.shops, names: ['shop-list', 'shop-detail'] },
  { to: '/nearby', label: copy.nav.nearby, names: ['nearby-shop'] },
  { to: '/vouchers', label: copy.nav.vouchers, names: ['voucher-list'] },
  { to: '/blogs', label: copy.nav.blogs, names: ['blog-feed', 'blog-publish'] },
  { to: '/blogs/hot', label: copy.nav.hot, names: ['hot-blog-list'] },
])

function isActive(item: { names: string[] }) {
  return item.names.includes(String(route.name ?? ''))
}
</script>

<style scoped>
.app-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.app-nav__link {
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 14px;
  border-radius: 999px;
  color: #475569;
  text-decoration: none;
  transition: background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.app-nav__link:hover {
  background: rgba(148, 163, 184, 0.12);
  color: #0f172a;
}

.app-nav__link.is-active {
  background: #0f172a;
  color: #ffffff;
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.16);
}

@media (max-width: 768px) {
  .app-nav {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 4px;
    scrollbar-width: none;
  }

  .app-nav::-webkit-scrollbar {
    display: none;
  }
}
</style>
