<template>
  <main class="page-stack">
    <section class="hero-card section-grid">
      <AppSectionHeader
        :title="copy.blogs.hotTitle"
        :subtitle="copy.blogs.hotSubtitle"
        eyebrow="公开热榜"
      />
      <p class="page-lead">
        {{ copy.blogs.hotHint }}
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
          @click="loadHotBlogs"
        >
          {{ copy.common.retry }}
        </BaseButton>
      </template>
    </StatusPanel>

    <EmptyState
      v-else-if="blogs.length === 0"
      eyebrow="热榜待补充"
      :title="copy.blogs.hotTitle"
      :description="copy.blogs.empty"
    />

    <HotBlogList
      v-else
      :blogs="blogs"
      :loading-id="likeLoadingId"
      @toggle-like="toggleLike"
    />

    <LoginPromptModal
      v-model="showLoginPrompt"
      @confirm="goToLogin"
    />
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchHotBlogs, toggleBlogLike, type BlogCard } from '../api/blog'
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import HotBlogList from '../components/HotBlogList.vue'
import LoginPromptModal from '../components/LoginPromptModal.vue'
import StatusPanel from '../components/StatusPanel.vue'
import { copy } from '../constants/copy'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const blogs = ref<BlogCard[]>([])
const loading = ref(false)
const likeLoadingId = ref<number | null>(null)
const showLoginPrompt = ref(false)
const errorMessage = ref('')

async function loadHotBlogs() {
  loading.value = true
  errorMessage.value = ''
  try {
    blogs.value = await fetchHotBlogs()
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message ?? copy.errors.blogLoad
  } finally {
    loading.value = false
  }
}

async function toggleLike(blog: BlogCard) {
  if (!authStore.isAuthenticated) {
    showLoginPrompt.value = true
    return
  }
  likeLoadingId.value = blog.id
  try {
    const liked = await toggleBlogLike(blog.id)
    blog.isLiked = liked
    blog.liked = liked ? blog.liked + 1 : Math.max(blog.liked - 1, 0)
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message ?? copy.errors.network
  } finally {
    likeLoadingId.value = null
  }
}

async function goToLogin() {
  showLoginPrompt.value = false
  await router.push({ name: 'login', query: { redirect: '/blogs/hot' } })
}

onMounted(loadHotBlogs)
</script>
