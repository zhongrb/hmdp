<template>
  <main class="page-stack">
    <section class="hero-card section-grid">
      <AppSectionHeader
        :title="copy.blogs.feedTitle"
        :subtitle="copy.blogs.feedSubtitle"
        eyebrow="内容流"
      />
      <h1 class="page-title">
        {{ copy.blogs.feedHeroTitle }}
      </h1>
      <p class="page-lead">
        {{ copy.blogs.feedHeroBody }}
      </p>
      <div class="inline-actions">
        <BaseButton
          tag="a"
          href="/blogs/hot"
          variant="secondary"
        >
          {{ copy.actions.viewHot }}
        </BaseButton>
        <BaseButton
          tag="a"
          href="/blogs/publish"
        >
          {{ copy.actions.publishBlog }}
        </BaseButton>
      </div>
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
          @click="loadFeed"
        >
          {{ copy.common.retry }}
        </BaseButton>
      </template>
    </StatusPanel>

    <EmptyState
      v-else-if="feed.length === 0"
      eyebrow="内容待补充"
      :title="copy.blogs.feedTitle"
      :description="copy.blogs.empty"
    />

    <section
      v-else
      class="list-stack"
    >
      <article
        v-for="blog in feed"
        :key="blog.id"
        class="surface-card blog-card"
      >
        <div class="blog-card__header">
          <div>
            <p class="eyebrow">
              {{ blog.shopName }}
            </p>
            <h2 class="section-title">
              {{ blog.title }}
            </h2>
          </div>
          <LikeButton
            :liked="blog.isLiked"
            :loading="likeLoadingId === blog.id"
            @toggle="toggleLike(blog)"
          />
        </div>
        <p class="section-subtitle">
          {{ blog.content }}
        </p>
        <div class="metric-row">
          <span class="metric-pill">作者：{{ blog.authorName }}</span>
          <span class="metric-pill">点赞 {{ blog.liked }}</span>
        </div>
      </article>

      <div class="inline-actions">
        <BaseButton
          variant="secondary"
          :loading="loadingMore"
          @click="loadMore"
        >
          {{ copy.actions.loadMore }}
        </BaseButton>
      </div>
    </section>

    <LoginPromptModal
      v-model="showLoginPrompt"
      @confirm="goToLogin"
    />
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchBlogFeed, toggleBlogLike, type BlogCard } from '../api/blog'
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import LikeButton from '../components/LikeButton.vue'
import LoginPromptModal from '../components/LoginPromptModal.vue'
import StatusPanel from '../components/StatusPanel.vue'
import { copy } from '../constants/copy'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const loadingMore = ref(false)
const likeLoadingId = ref<number | null>(null)
const showLoginPrompt = ref(false)
const errorMessage = ref('')
const feed = ref<BlogCard[]>([])
const lastId = ref<number | undefined>()
const offset = ref(0)

async function loadFeed() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await fetchBlogFeed()
    feed.value = result.list
    lastId.value = result.lastId ?? undefined
    offset.value = result.offset ?? result.list.length
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message ?? copy.errors.blogLoad
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  try {
    const result = await fetchBlogFeed(lastId.value, offset.value)
    feed.value = [...feed.value, ...result.list]
    lastId.value = result.lastId ?? lastId.value
    offset.value = result.offset ?? offset.value
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message ?? copy.errors.blogLoad
  } finally {
    loadingMore.value = false
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
  await router.push({ name: 'login', query: { redirect: '/blogs' } })
}

onMounted(loadFeed)
</script>

<style scoped>
.blog-card {
  display: grid;
  gap: 16px;
}

.blog-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

@media (max-width: 768px) {
  .blog-card__header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
