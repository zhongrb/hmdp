<template>
  <main class="page-stack">
    <section class="hero-card section-grid">
      <AppSectionHeader
        :title="copy.follows.title"
        :subtitle="copy.follows.subtitle"
        eyebrow="社交能力"
      />
      <h1 class="page-title">
        {{ copy.follows.heroTitle }}
      </h1>
      <p class="page-lead">
        {{ copy.follows.heroBody }}
      </p>
    </section>

    <StatusPanel
      v-if="loading"
      :title="copy.common.loading"
      :description="copy.status.activityLoading"
    />

    <StatusPanel
      v-else-if="errorMessage"
      :title="errorMessage"
      tone="error"
    >
      <template #actions>
        <BaseButton
          variant="secondary"
          @click="loadCommonFollows"
        >
          {{ copy.common.retry }}
        </BaseButton>
      </template>
    </StatusPanel>

    <EmptyState
      v-else-if="users.length === 0"
      eyebrow="暂无交集"
      :title="copy.follows.title"
      :description="copy.blogs.commonEmpty"
    />

    <section
      v-else
      class="list-stack"
    >
      <article
        v-for="user in users"
        :key="user.id"
        class="surface-card follow-user-card"
      >
        <div>
          <p class="eyebrow">
            共同关注用户
          </p>
          <h2 class="section-title">
            {{ user.nickName }}
          </h2>
        </div>
        <p class="section-subtitle">
          {{ user.icon || '当前用户未提供头像信息。' }}
        </p>
      </article>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchCommonFollows } from '../api/follow'
import type { AuthUser } from '../stores/auth'
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import StatusPanel from '../components/StatusPanel.vue'
import { copy } from '../constants/copy'

const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const users = ref<AuthUser[]>([])

async function loadCommonFollows() {
  loading.value = true
  errorMessage.value = ''
  try {
    users.value = await fetchCommonFollows(Number(route.params.targetUserId))
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message ?? copy.errors.followLoad
  } finally {
    loading.value = false
  }
}

onMounted(loadCommonFollows)
</script>

<style scoped>
.follow-user-card {
  display: grid;
  gap: 12px;
}
</style>
