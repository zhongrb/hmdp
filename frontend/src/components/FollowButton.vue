<template>
  <BaseButton
    :variant="following ? 'secondary' : 'primary'"
    :loading="loading"
    @click="$emit('toggle')"
  >
    {{ following ? copy.blogs.unfollow : copy.blogs.follow }}
  </BaseButton>
</template>

<script setup lang="ts">
import BaseButton from './BaseButton.vue'
import { copy } from '../constants/copy'

withDefaults(
  defineProps<{
    following: boolean
    loading?: boolean
  }>(),
  {
    loading: false,
  },
)

defineEmits<{
  toggle: []
}>()
</script>
