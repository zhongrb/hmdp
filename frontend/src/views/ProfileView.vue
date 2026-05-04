<template>
  <main class="page-stack">
    <section class="hero-card section-grid">
      <AppSectionHeader
        title="个人信息"
        eyebrow="当前账号"
      />
      <h1 class="page-title">
        {{ authStore.user?.nickName }}
      </h1>
    </section>

    <section class="surface-card profile-card">
      <article class="profile-item">
        <p class="eyebrow">
          昵称
        </p>
        <h2 class="section-title">
          {{ authStore.user?.nickName }}
        </h2>
      </article>

      <article class="profile-item">
        <p class="eyebrow">
          用户 ID
        </p>
        <p class="section-subtitle">
          {{ authStore.user?.id }}
        </p>
      </article>

      <article class="profile-item">
        <p class="eyebrow">
          登录状态
        </p>
        <p class="section-subtitle">
          已登录
        </p>
      </article>

      <article
        v-if="authStore.user?.icon"
        class="profile-item"
      >
        <p class="eyebrow">
          头像地址
        </p>
        <p class="section-subtitle profile-link">
          {{ authStore.user.icon }}
        </p>
      </article>

      <div class="inline-actions">
        <BaseButton
          tag="a"
          href="/"
          variant="secondary"
        >
          返回首页
        </BaseButton>
        <BaseButton
          tag="a"
          href="/vouchers"
        >
          继续浏览
        </BaseButton>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
</script>

<style scoped>
.profile-card {
  display: grid;
  gap: 18px;
}

.profile-item {
  display: grid;
  gap: 8px;
}

.profile-link {
  word-break: break-all;
}
</style>
