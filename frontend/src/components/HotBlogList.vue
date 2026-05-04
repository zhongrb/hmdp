<template>
  <section class="list-stack">
    <article
      v-for="(blog, index) in blogs"
      :key="blog.id"
      class="surface-card hot-blog-card"
      :class="[`is-rank-${index + 1}`, { 'is-top-three': index < 3 }]"
      tabindex="0"
      role="button"
      :aria-label="`打开第 ${index + 1} 名商户 ${blog.shopName} 详情`"
      @click="$emit('open-shop', blog)"
      @keydown.enter.prevent="$emit('open-shop', blog)"
      @keydown.space.prevent="$emit('open-shop', blog)"
    >
      <div class="hot-blog-card__rank">
        <span class="hot-blog-card__rank-label">TOP</span>
        <strong class="hot-blog-card__rank-value">{{ index + 1 }}</strong>
      </div>

      <div class="hot-blog-card__body">
        <div class="hot-blog-card__header">
          <div class="hot-blog-card__summary">
            <p class="eyebrow">
              {{ blog.shopName }}
            </p>
            <h3 class="section-title">
              {{ blog.title }}
            </h3>
            <p class="section-subtitle hot-blog-card__content">
              {{ blog.content }}
            </p>
          </div>
          <div
            class="hot-blog-card__like"
            @click.stop
            @keydown.stop
          >
            <LikeButton
              :liked="blog.isLiked"
              :loading="loadingId === blog.id"
              @toggle="$emit('toggle-like', blog)"
            />
          </div>
        </div>

        <div class="metric-row">
          <span class="metric-pill">作者：{{ blog.authorName }}</span>
          <span class="metric-pill">点赞 {{ blog.liked }}</span>
          <span class="metric-pill">商户：{{ blog.shopName }}</span>
        </div>

        <div class="hot-blog-card__footer">
          <span class="hot-blog-card__hint">点击卡片查看商户详情</span>
          <span class="hot-blog-card__link">查看详情</span>
        </div>
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
  'open-shop': [blog: BlogCard]
}>()
</script>

<style scoped>
.hot-blog-card {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  gap: 20px;
  padding: 24px;
  cursor: pointer;
  border: 1px solid rgba(148, 163, 184, 0.24);
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.hot-blog-card:hover,
.hot-blog-card:focus-visible {
  transform: translateY(-2px);
  border-color: rgba(3, 105, 161, 0.28);
  box-shadow: 0 24px 52px rgba(15, 23, 42, 0.1);
  outline: none;
}

.hot-blog-card.is-top-three {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(240, 249, 255, 0.92));
}

.hot-blog-card__rank {
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 8px;
  padding: 18px 12px;
  border-radius: 28px;
  background: linear-gradient(160deg, rgba(15, 23, 42, 0.98), rgba(30, 41, 59, 0.92));
  color: #ffffff;
}

.hot-blog-card.is-top-three .hot-blog-card__rank {
  background: linear-gradient(160deg, rgba(180, 83, 9, 0.96), rgba(245, 158, 11, 0.92));
  box-shadow: 0 18px 36px rgba(245, 158, 11, 0.24);
}

.hot-blog-card__rank-label {
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.24em;
}

.hot-blog-card__rank-value {
  font-size: clamp(2.2rem, 4vw, 3.6rem);
  line-height: 1;
}

.hot-blog-card__body {
  display: grid;
  gap: 16px;
  min-width: 0;
}

.hot-blog-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.hot-blog-card__summary {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.hot-blog-card__summary :deep(.section-title),
.hot-blog-card__summary :deep(.section-subtitle) {
  margin: 0;
}

.hot-blog-card__content {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-blog-card__like {
  flex-shrink: 0;
}

.hot-blog-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hot-blog-card__hint {
  color: #64748b;
  font-size: 0.95rem;
}

.hot-blog-card__link {
  color: #0369a1;
  font-weight: 700;
}

@media (max-width: 768px) {
  .hot-blog-card {
    grid-template-columns: 1fr;
  }

  .hot-blog-card__rank {
    justify-items: flex-start;
    grid-auto-flow: column;
    justify-content: flex-start;
    align-items: end;
  }

  .hot-blog-card__header,
  .hot-blog-card__footer {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
