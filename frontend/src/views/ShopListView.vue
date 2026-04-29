<template>
  <main class="page-stack">
    <section class="hero-card section-grid">
      <AppSectionHeader
        :title="copy.shops.listTitle"
        :subtitle="copy.shops.browseHint"
        eyebrow="公开浏览"
      />
      <div
        class="filter-row"
        aria-label="商户分类筛选"
      >
        <button
          v-for="type in shopTypes"
          :key="type.id"
          type="button"
          class="filter-chip"
          :class="{ 'is-active': type.id === activeTypeId }"
          @click="selectType(type.id)"
        >
          {{ type.name }}
        </button>
      </div>
    </section>

    <StatusPanel
      v-if="loading"
      :title="copy.common.loading"
      description="正在为你加载当前分类下的商户列表。"
    />

    <EmptyState
      v-else-if="shops.length === 0"
      eyebrow="暂无结果"
      title="当前分类还没有可展示商户"
      :description="copy.shops.empty"
    >
      <template #actions>
        <BaseButton
          variant="secondary"
          @click="loadShops"
        >
          {{ copy.common.retry }}
        </BaseButton>
      </template>
    </EmptyState>

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
import { fetchShopList, fetchShopTypes, type Shop, type ShopType } from '../api/shop'
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import ShopCard from '../components/ShopCard.vue'
import StatusPanel from '../components/StatusPanel.vue'
import { copy } from '../constants/copy'

const loading = ref(false)
const shopTypes = ref<ShopType[]>([])
const shops = ref<Shop[]>([])
const activeTypeId = ref<number | null>(null)

async function loadTypes() {
  shopTypes.value = await fetchShopTypes()
  if (shopTypes.value.length > 0 && activeTypeId.value == null) {
    activeTypeId.value = shopTypes.value[0].id
  }
}

async function loadShops() {
  loading.value = true
  try {
    shops.value = await fetchShopList(activeTypeId.value ?? undefined, 1)
  } finally {
    loading.value = false
  }
}

async function selectType(typeId: number) {
  activeTypeId.value = typeId
  await loadShops()
}

onMounted(async () => {
  await loadTypes()
  await loadShops()
})
</script>
