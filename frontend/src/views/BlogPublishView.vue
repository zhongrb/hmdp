<template>
  <main class="page-stack">
    <section class="hero-card section-grid">
      <AppSectionHeader
        :title="copy.blogs.publishTitle"
        :subtitle="copy.blogs.publishSubtitle"
        eyebrow="受保护功能"
      />
      <p class="page-lead">
        登录后可把你的真实体验补充进站内内容流与热榜推荐。
      </p>
    </section>

    <section class="surface-card">
      <form
        class="form-grid"
        @submit.prevent="submit"
      >
        <label class="form-label">
          {{ copy.blogs.titleLabel }}
          <input
            v-model.trim="form.title"
            type="text"
          >
        </label>

        <label class="form-label">
          {{ copy.blogs.contentLabel }}
          <textarea
            v-model.trim="form.content"
            rows="6"
          />
        </label>

        <label class="form-label">
          {{ copy.blogs.shopIdLabel }}
          <input
            v-model.number="form.shopId"
            type="number"
            min="1"
          >
        </label>

        <p
          v-if="message"
          class="message-text"
          :class="{ 'is-error': messageTone === 'error', 'is-success': messageTone === 'success' }"
          :role="messageTone === 'error' ? 'alert' : undefined"
          aria-live="polite"
        >
          {{ message }}
        </p>

        <div class="inline-actions">
          <BaseButton
            type="submit"
            :loading="submitting"
          >
            {{ submitting ? copy.blogs.submitting : copy.blogs.submit }}
          </BaseButton>
          <BaseButton
            type="button"
            variant="secondary"
            @click="goToFeed"
          >
            {{ copy.actions.exploreBlogs }}
          </BaseButton>
        </div>
      </form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { publishBlog } from '../api/blog'
import AppSectionHeader from '../components/AppSectionHeader.vue'
import BaseButton from '../components/BaseButton.vue'
import { copy } from '../constants/copy'

const router = useRouter()
const submitting = ref(false)
const message = ref('')
const messageTone = ref<'success' | 'error'>('success')
const form = reactive({
  title: '新店体验',
  content: '值得再来',
  shopId: 1,
})

async function submit() {
  submitting.value = true
  message.value = ''
  try {
    await publishBlog(form)
    messageTone.value = 'success'
    message.value = copy.blogs.publishSuccess
    form.title = ''
    form.content = ''
    form.shopId = 1
  } catch (error: any) {
    messageTone.value = 'error'
    message.value = error.response?.data?.message ?? copy.errors.blogPublish
  } finally {
    submitting.value = false
  }
}

async function goToFeed() {
  await router.push('/blogs')
}
</script>
