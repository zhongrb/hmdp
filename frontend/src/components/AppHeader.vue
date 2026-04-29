<template>
  <header class="app-header-wrap">
    <div class="app-header">
      <RouterLink
        to="/"
        class="brand"
        aria-label="回到首页"
      >
        <span class="brand__mark">H</span>
        <span class="brand__content">
          <strong>{{ copy.appName }}</strong>
          <span>先浏览，再登录互动</span>
        </span>
      </RouterLink>

      <AppNavBar />

      <div class="app-header__actions">
        <div
          v-if="authStore.isAuthenticated && authStore.user"
          class="user-badge"
        >
          <strong>{{ authStore.user.nickName }}</strong>
          <span>已登录</span>
        </div>
        <BaseButton
          v-if="authStore.isAuthenticated"
          variant="secondary"
          @click="logout"
        >
          退出登录
        </BaseButton>
        <BaseButton
          v-else
          tag="a"
          href="/login"
        >
          {{ copy.nav.login }}
        </BaseButton>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'
import { copy } from '../constants/copy'
import { useAuthStore } from '../stores/auth'
import AppNavBar from './AppNavBar.vue'
import BaseButton from './BaseButton.vue'

const router = useRouter()
const authStore = useAuthStore()

async function logout() {
  authStore.clearSession()
  authStore.setRedirectPath(null)
  await router.push('/')
}
</script>

<style scoped>
.app-header-wrap {
  position: sticky;
  top: 0;
  z-index: 30;
  padding: 16px 0 12px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.92), rgba(248, 250, 252, 0.72), transparent);
  backdrop-filter: blur(10px);
}

.app-header {
  width: var(--container-width);
  min-height: var(--header-height);
  margin: 0 auto;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 20px;
  align-items: center;
  padding: 16px 18px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
}

.brand {
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.brand__mark {
  width: 44px;
  height: 44px;
  display: inline-grid;
  place-items: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #0f172a, #334155);
  color: #ffffff;
  font-weight: 800;
}

.brand__content {
  display: grid;
  gap: 2px;
}

.brand__content strong {
  color: #020617;
  font-size: 1rem;
}

.brand__content span,
.user-badge span {
  color: #64748b;
  font-size: 0.9rem;
}

.app-header__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.user-badge {
  min-height: 48px;
  display: grid;
  align-content: center;
  padding: 0 14px;
  border-radius: 16px;
  background: rgba(226, 232, 240, 0.65);
}

.user-badge strong {
  color: #0f172a;
  font-size: 0.95rem;
}

@media (max-width: 1100px) {
  .app-header {
    grid-template-columns: 1fr;
  }

  .app-header__actions {
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .app-header-wrap {
    padding: 12px 0 8px;
  }

  .app-header {
    gap: 14px;
    padding: 14px;
    border-radius: 20px;
  }

  .app-header__actions {
    flex-wrap: wrap;
  }
}
</style>
