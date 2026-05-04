<template>
  <main class="page-stack">
    <section class="hero-card two-column-grid">
      <div class="section-grid">
        <div>
          <p class="eyebrow">
            {{ copy.appName }}
          </p>
          <h1 class="page-title">
            {{ copy.home.heroTitle }}
          </h1>
        </div>

        <div class="inline-actions">
          <BaseButton
            tag="a"
            href="/shops"
          >
            {{ copy.home.heroCtaPrimary }}
          </BaseButton>
          <BaseButton
            tag="a"
            href="/nearby"
            variant="secondary"
          >
            {{ copy.home.heroCtaSecondary }}
          </BaseButton>
        </div>

        <div class="metric-row">
          <span class="metric-pill">{{ copy.stats.openShops }}</span>
          <span class="metric-pill">{{ copy.stats.activityZones }}</span>
          <span class="metric-pill">{{ copy.stats.loginPromise }}</span>
        </div>
      </div>

      <section class="surface-card">
        <AppSectionHeader
          :title="copy.home.sectionTitle"
          :subtitle="copy.home.sectionSubtitle"
          eyebrow="浏览体验"
        />
        <div class="info-list">
          <article
            v-for="item in copy.home.highlights"
            :key="item"
            class="info-item"
          >
            <span class="info-value">{{ item }}</span>
          </article>
        </div>
      </section>
    </section>

    <section class="card-grid">
      <article
        v-for="entry in entries"
        :key="entry.title"
        class="surface-card"
      >
        <AppSectionHeader
          :title="entry.title"
          :subtitle="entry.description"
          :eyebrow="entry.eyebrow"
        />
        <BaseButton
          tag="a"
          :href="entry.href"
          variant="ghost"
        >
          {{ entry.action }}
        </BaseButton>
      </article>
    </section>

    <EmptyState
      :eyebrow="copy.marketing.trustTitle"
      title="从公开内容开始找灵感"
      :description="copy.marketing.trustBody"
    >
      <template #actions>
        <BaseButton
          tag="a"
          href="/vouchers"
          variant="secondary"
        >
          {{ copy.actions.goVouchers }}
        </BaseButton>
        <BaseButton
          tag="a"
          href="/blogs"
        >
          {{ copy.actions.exploreBlogs }}
        </BaseButton>
      </template>
    </EmptyState>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import { copy } from '../constants/copy'

const entries = computed(() => [
  {
    title: copy.nav.shops,
    description: '看看分类、评分和人均，挑一家更适合现在想去的店。',
    action: copy.actions.goShops,
    href: '/shops',
    eyebrow: copy.labels.publicBrowse,
  },
  {
    title: copy.nav.nearby,
    description: '想找离你更近的选择时，可以先看看周边有哪些热门商户。',
    action: copy.actions.goNearby,
    href: '/nearby',
    eyebrow: '地理推荐',
  },
  {
    title: copy.nav.vouchers,
    description: '先看看有哪些活动和价格，遇到合适的再去领券也不迟。',
    action: copy.actions.goVouchers,
    href: '/vouchers',
    eyebrow: copy.labels.protectedFeature,
  },
  {
    title: copy.nav.blogs,
    description: '看看大家最近吃了什么、玩了什么，给自己找点灵感。',
    action: copy.actions.exploreBlogs,
    href: '/blogs',
    eyebrow: '真实内容',
  },
])
</script>
