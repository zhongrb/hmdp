<template>
  <section class="list-stack">
    <article
      v-for="blog in blogs"
      :key="blog.id"
      class="surface-card hot-blog-card"
    >
      <div class="hot-blog-card__header">
        <div>
          <p class="eyebrow">
            {{ blog.shopName }}
          </p>
          <h3 class="section-title">
            {{ blog.title }}
          </h3>
        </div>
        <LikeButton
          :liked="blog.isLiked"
          :loading="loadingId === blog.id"
          @toggle="$emit('toggle-like', blog)"
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
  </section>
</template>

<script setup lang="ts">
import type { BlogCard } from '../api/blog'
import LikeButton from './LikeButton.vue'

defineProps<{
  blogs: BlogCard[]
  loadingId?: number | null
}>()

defineEmits<{
  'toggle-like': [blog: BlogCard]
}>()
</script>

<style scoped>
.hot-blog-card {
  display: grid;
  gap: 16px;
}

.hot-blog-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

@media (max-width: 768px) {
  .hot-blog-card__header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
