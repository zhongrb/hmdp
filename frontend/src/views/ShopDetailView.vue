<template>
  <main class="page-stack">
    <StatusPanel
      v-if="loading"
      :title="copy.common.loading"
      description="正在加载商户详情与基础信息。"
    />

    <EmptyState
      v-else-if="!shop"
      eyebrow="详情不可用"
      :title="copy.shops.detailTitle"
      :description="copy.shops.detailNotFound"
    >
      <template #actions>
        <BaseButton
          tag="a"
          href="/shops"
          variant="secondary"
        >
          {{ copy.actions.goShops }}
        </BaseButton>
      </template>
    </EmptyState>

    <template v-else>
      <section class="hero-card two-column-grid">
        <div class="section-grid">
          <div>
            <p class="eyebrow">
              {{ copy.shops.detailTitle }}
            </p>
            <h1 class="page-title">
              {{ shop.name }}
            </h1>
            <p class="page-lead">
              {{ shop.address }}
            </p>
          </div>
          <div class="metric-row">
            <span class="metric-pill">{{ copy.shops.pricePrefix }} ¥{{ shop.avgPrice ?? '--' }}</span>
            <span class="metric-pill">{{ copy.shops.scorePrefix }} {{ shop.score }}</span>
            <span class="metric-pill">{{ shop.comments }} 条评价</span>
          </div>
          <div class="inline-actions">
            <BaseButton
              tag="a"
              href="/vouchers"
            >
              {{ copy.actions.goVouchers }}
            </BaseButton>
            <BaseButton
              tag="a"
              href="/nearby"
              variant="secondary"
            >
              {{ copy.actions.goNearby }}
            </BaseButton>
          </div>
        </div>

        <section class="surface-card info-list">
          <article class="info-item">
            <span class="info-label">{{ copy.labels.businessHours }}</span>
            <span class="info-value">{{ shop.openHours ?? '待补充' }}</span>
          </article>
          <article class="info-item">
            <span class="info-label">商户地址</span>
            <span class="info-value">{{ shop.address }}</span>
          </article>
          <article class="info-item">
            <span class="info-label">浏览说明</span>
            <span class="info-value">{{ copy.common.browseFirst }}</span>
          </article>
        </section>
      </section>
    </template>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchShopDetail, type Shop } from '../api/shop'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import StatusPanel from '../components/StatusPanel.vue'
import { copy } from '../constants/copy'

const route = useRoute()
const loading = ref(false)
const shop = ref<Shop | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    shop.value = await fetchShopDetail(String(route.params.shopId))
  } catch {
    shop.value = null
  } finally {
    loading.value = false
  }
})
</script>
