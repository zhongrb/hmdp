import { test as base } from '@playwright/test'

export const test = base.extend({
  authenticatedPage: async ({ page }, use) => {
    await page.addInitScript(() => {
      window.localStorage.setItem('hmdp-token', 'e2e-token')
      window.localStorage.setItem(
        'hmdp-user',
        JSON.stringify({ id: 1, nickName: '测试用户', icon: null }),
      )
    })
    await use(page)
  },
})

export const expect = test.expect
