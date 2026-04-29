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
          <p class="page-lead">
            {{ copy.home.heroSubtitle }}
          </p>
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
      title="从公开内容开始了解平台"
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
    description: '先看分类、评分与人均，再决定是否深入查看商户详情。',
    action: copy.actions.goShops,
    href: '/shops',
    eyebrow: copy.labels.publicBrowse,
  },
  {
    title: copy.nav.nearby,
    description: '支持按距离浏览附近商户，即使未授权定位也有降级提示。',
    action: copy.actions.goNearby,
    href: '/nearby',
    eyebrow: '地理推荐',
  },
  {
    title: copy.nav.vouchers,
    description: '先浏览优惠活动节奏，登录后再参与抢购与签到。',
    action: copy.actions.goVouchers,
    href: '/vouchers',
    eyebrow: copy.labels.protectedFeature,
  },
  {
    title: copy.nav.blogs,
    description: '看看大家最近的探店内容和热榜动态，快速建立消费判断。',
    action: copy.actions.exploreBlogs,
    href: '/blogs',
    eyebrow: '真实内容',
  },
])
</script>
