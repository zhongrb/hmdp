<template>
  <article class="shop-card">
    <div
      class="shop-card__media"
      aria-hidden="true"
    >
      <span>{{ shop.name.slice(0, 1) }}</span>
    </div>
    <div class="shop-card__content">
      <div class="shop-card__header">
        <div>
          <h3>{{ shop.name }}</h3>
          <p>{{ shop.address }}</p>
        </div>
        <span class="shop-card__score">{{ copy.shops.scorePrefix }} {{ shop.score }}</span>
      </div>
      <div class="metric-row">
        <span class="metric-pill">{{ copy.shops.pricePrefix }} ¥{{ shop.avgPrice ?? '--' }}</span>
        <span
          v-if="shop.distance != null"
          class="metric-pill"
        >{{ copy.shops.distancePrefix }} {{ formatDistance(shop.distance) }}</span>
        <span class="metric-pill">{{ shop.comments }} 条评价</span>
      </div>
      <p class="shop-card__hours">
        营业时间：{{ shop.openHours ?? '待补充' }}
      </p>
      <RouterLink
        :to="`/shops/${shop.id}`"
        class="shop-card__link"
      >
        查看详情
      </RouterLink>
    </div>
  </article>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { Shop } from '../api/shop'
import { copy } from '../constants/copy'

defineProps<{
  shop: Shop
}>()

function formatDistance(distance: number) {
  return `${distance.toFixed(2)} km`
}
</script>

<style scoped>
.shop-card {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  gap: 18px;
  padding: 20px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.06);
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.shop-card:hover {
  transform: translateY(-2px);
  border-color: rgba(3, 105, 161, 0.2);
  box-shadow: 0 24px 52px rgba(15, 23, 42, 0.1);
}

.shop-card__media {
  width: 92px;
  height: 92px;
  display: grid;
  place-items: center;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.96), rgba(3, 105, 161, 0.92));
  color: #ffffff;
  font-size: 2rem;
  font-weight: 800;
}

.shop-card__content {
  display: grid;
  gap: 14px;
  min-width: 0;
}

.shop-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.shop-card__header h3,
.shop-card__header p,
.shop-card__hours {
  margin: 0;
}

.shop-card__header h3 {
  font-size: 1.1rem;
}

.shop-card__header p,
.shop-card__hours {
  color: #64748b;
}

.shop-card__score {
  flex-shrink: 0;
  min-height: 40px;
  display: inline-flex;
  align-items: center;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(226, 232, 240, 0.8);
  color: #0f172a;
  font-weight: 700;
}

.shop-card__link {
  color: #0369a1;
  text-decoration: none;
  font-weight: 700;
}

.shop-card__link:hover {
  color: #0284c7;
}

@media (max-width: 640px) {
  .shop-card {
    grid-template-columns: 1fr;
  }

  .shop-card__media {
    width: 72px;
    height: 72px;
    border-radius: 20px;
  }

  .shop-card__header {
    flex-direction: column;
  }
}
</style>
