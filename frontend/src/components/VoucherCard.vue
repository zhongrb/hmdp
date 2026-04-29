<template>
  <article
    class="voucher-card"
    :class="`voucher-card--${statusTone}`"
  >
    <div class="voucher-card__header">
      <div class="voucher-card__header-main">
        <h3 class="voucher-card__title">
          {{ voucher.title }}
        </h3>
        <p class="voucher-card__subtitle">
          {{ voucher.subTitle }}
        </p>
      </div>
      <span
        class="voucher-card__status"
        :class="`is-${statusTone}`"
      >
        {{ statusLabel }}
      </span>
    </div>

    <div class="voucher-card__deal">
      <span class="voucher-card__deal-label">{{ copy.vouchers.payLabel }}</span>
      <div class="voucher-card__deal-price-row">
        <strong class="voucher-card__deal-price">¥{{ formatCurrency(voucher.payValue) }}</strong>
        <p class="voucher-card__deal-compare">
          <span>{{ copy.vouchers.actualLabel }}</span>
          <strong>¥{{ formatCurrency(voucher.actualValue) }}</strong>
        </p>
      </div>
    </div>

    <div class="voucher-card__meta">
      <div class="voucher-card__meta-item">
        <span class="voucher-card__meta-label">{{ copy.vouchers.stockLabel }}</span>
        <strong>{{ voucher.stock }}</strong>
      </div>
      <div class="voucher-card__meta-item voucher-card__meta-item--time">
        <span class="voucher-card__meta-label">{{ copy.vouchers.windowLabel }}</span>
        <strong>{{ formattedWindow }}</strong>
      </div>
    </div>

    <div class="voucher-card__footer">
      <p class="voucher-card__hint">
        {{ copy.vouchers.browseHint }}
      </p>
      <BaseButton
        class="voucher-card__action"
        :disabled="disabled"
        :loading="claiming"
        @click="$emit('claim')"
      >
        {{ claiming ? copy.vouchers.claimDone : copy.vouchers.claimNow }}
      </BaseButton>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Voucher } from '../api/voucher'
import BaseButton from './BaseButton.vue'
import { copy } from '../constants/copy'

const props = defineProps<{
  voucher: Voucher
  claiming?: boolean
  disabled?: boolean
}>()

defineEmits<{
  claim: []
}>()

const statusLabel = computed(() => {
  if (props.voucher.status === 0) {
    return copy.vouchers.statusUpcoming
  }
  if (props.voucher.status === 1) {
    return copy.vouchers.statusActive
  }
  if (props.voucher.status === 2) {
    return copy.vouchers.statusEnded
  }
  return copy.vouchers.statusSoldOut
})

const statusTone = computed(() => {
  if (props.voucher.status === 1) {
    return 'active'
  }
  if (props.voucher.status === 0) {
    return 'upcoming'
  }
  return 'inactive'
})

const formattedWindow = computed(() => {
  return `${formatDateTime(props.voucher.beginTime)} 至 ${formatDateTime(props.voucher.endTime)}`
})

function formatCurrency(value: number) {
  return (value / 100).toFixed(value % 100 === 0 ? 0 : 2)
}

function formatDateTime(value: string) {
  const date = new Date(value)
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hours = `${date.getHours()}`.padStart(2, '0')
  const minutes = `${date.getMinutes()}`.padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}
</script>

<style scoped>
.voucher-card {
  display: grid;
  gap: 18px;
  padding: 24px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 28px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96));
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.voucher-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.12);
}

.voucher-card--active {
  border-color: rgba(20, 184, 166, 0.18);
}

.voucher-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.voucher-card__header-main {
  display: grid;
  gap: 8px;
}

.voucher-card__title,
.voucher-card__subtitle,
.voucher-card__deal-compare,
.voucher-card__hint {
  margin: 0;
}

.voucher-card__title {
  font-size: 1.5rem;
  line-height: 1.35;
  color: #0f172a;
}

.voucher-card__subtitle {
  color: #475569;
  line-height: 1.6;
}

.voucher-card__status {
  min-height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 13px;
  border-radius: 999px;
  font-size: 0.88rem;
  font-weight: 700;
  white-space: nowrap;
}

.voucher-card__status.is-active {
  background: rgba(20, 184, 166, 0.1);
  color: #0f766e;
}

.voucher-card__status.is-upcoming {
  background: rgba(3, 105, 161, 0.08);
  color: #0369a1;
}

.voucher-card__status.is-inactive {
  background: rgba(148, 163, 184, 0.14);
  color: #475569;
}

.voucher-card__deal {
  display: grid;
  gap: 10px;
  padding: 18px 20px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(240, 249, 255, 0.92), rgba(236, 253, 245, 0.92));
  border: 1px solid rgba(125, 211, 252, 0.22);
}

.voucher-card__deal-label,
.voucher-card__meta-label {
  font-size: 0.875rem;
  color: #64748b;
}

.voucher-card__deal-price-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.voucher-card__deal-price {
  font-size: clamp(2.1rem, 4.2vw, 2.9rem);
  line-height: 1;
  letter-spacing: -0.035em;
  color: #0f172a;
}

.voucher-card__deal-compare {
  display: grid;
  gap: 4px;
  justify-items: end;
  color: #475569;
  flex-shrink: 0;
}

.voucher-card__deal-compare strong {
  font-size: 1.25rem;
  color: #0f172a;
}

.voucher-card__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 18px;
}

.voucher-card__meta-item {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 44px;
  padding: 0;
  background: transparent;
  border: 0;
}

.voucher-card__meta-item strong {
  font-size: 1rem;
  color: #0f172a;
}

.voucher-card__meta-item--time {
  flex: 1 1 280px;
  min-width: 220px;
}

.voucher-card__meta-item--time strong {
  font-size: 0.96rem;
  line-height: 1.5;
  color: #1e293b;
}

.voucher-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 6px;
}

.voucher-card__hint {
  max-width: 28ch;
  color: #64748b;
  font-size: 0.94rem;
  line-height: 1.5;
}

.voucher-card__action {
  min-width: 160px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .voucher-card {
    padding: 20px;
    gap: 16px;
  }

  .voucher-card__header,
  .voucher-card__deal-price-row,
  .voucher-card__footer {
    flex-direction: column;
    align-items: stretch;
  }

  .voucher-card__status {
    align-self: flex-start;
  }

  .voucher-card__deal-compare {
    justify-items: start;
  }

  .voucher-card__meta {
    display: grid;
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .voucher-card__meta-item,
  .voucher-card__meta-item--time {
    min-width: 0;
  }

  .voucher-card__hint {
    max-width: none;
  }

  .voucher-card__action {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .voucher-card {
    transition: none;
  }

  .voucher-card:hover {
    transform: none;
  }
}
</style>
