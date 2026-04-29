<template>
  <main class="page-stack">
    <section class="hero-card two-column-grid">
      <div class="section-grid">
        <AppSectionHeader
          :title="copy.shops.nearbyTitle"
          :subtitle="notice"
          eyebrow="位置浏览"
        />
        <div class="metric-row">
          <span class="metric-pill">默认坐标：上海示例点位</span>
          <span class="metric-pill">范围：5km</span>
        </div>
        <div class="inline-actions">
          <BaseButton
            :loading="loading"
            @click="loadNearby"
          >
            {{ copy.common.retry }}
          </BaseButton>
          <BaseButton
            tag="a"
            href="/shops"
            variant="secondary"
          >
            {{ copy.actions.goShops }}
          </BaseButton>
        </div>
      </div>

      <section class="surface-card">
        <AppSectionHeader
          title="定位未开放时的浏览策略"
          :subtitle="copy.shops.locationDenied"
          eyebrow="降级提示"
        />
        <p class="body-muted">
          即使未获得真实定位，也会保留附近商户浏览入口，避免页面完全失效。
        </p>
      </section>
    </section>

    <StatusPanel
      v-if="loading"
      :title="copy.common.loading"
      description="正在根据默认坐标加载附近商户。"
    />

    <EmptyState
      v-else-if="shops.length === 0"
      eyebrow="暂无附近商户"
      title="当前坐标附近暂无可展示结果"
      :description="copy.shops.empty"
    />

    <section
      v-else
      class="list-stack"
    >
      <ShopCard
        v-for="shop in shops"
        :key="shop.id"
        :shop="shop"
      />
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchNearbyShops, type Shop } from '../api/shop'
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import ShopCard from '../components/ShopCard.vue'
import StatusPanel from '../components/StatusPanel.vue'
import { copy } from '../constants/copy'

const loading = ref(false)
const notice = ref(copy.shops.locationDenied)
const shops = ref<Shop[]>([])

async function loadNearby() {
  loading.value = true
  try {
    shops.value = await fetchNearbyShops(1, 121.490317, 31.222771, 1)
  } finally {
    loading.value = false
  }
}

onMounted(loadNearby)
</script>
