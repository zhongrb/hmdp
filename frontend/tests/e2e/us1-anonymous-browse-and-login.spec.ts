import { test, expect } from '@playwright/test'

test('匿名用户可浏览公开页面并进入登录页', async ({ page }) => {
  await page.route('**/api/shops/types', async (route) => {
    await route.fulfill({ json: { success: true, data: [{ id: 1, name: '美食', sort: 1 }] } })
  })
  await page.route('**/api/shops?page=1&typeId=1', async (route) => {
    await route.fulfill({ json: { success: true, data: [{ id: 1, name: '城南小馆', avgPrice: 88, comments: 126, score: 4.7 }] } })
  })
  await page.route('**/api/shops/1', async (route) => {
    await route.fulfill({ json: { success: true, data: { id: 1, name: '城南小馆', address: '上海市黄浦区云南南路 88 号', avgPrice: 88, score: 4.7, openHours: '10:00-22:00' } } })
  })
  await page.route('**/api/shops/nearby**', async (route) => {
    await route.fulfill({ json: { success: true, data: [{ id: 1, name: '城南小馆', distance: 0.36 }] } })
  })

  await page.goto('/')
  await expect(page.getByRole('heading', { name: '发现身边好店与优惠' })).toBeVisible()

  await page.getByRole('link', { name: '去看商户' }).click()
  await expect(page.getByRole('heading', { name: '精选商户' })).toBeVisible()
  await expect(page.getByText('城南小馆')).toBeVisible()

  await page.getByRole('link', { name: '城南小馆' }).click()
  await expect(page.getByRole('heading', { name: '城南小馆' })).toBeVisible()

  await page.goto('/nearby')
  await expect(page.getByRole('heading', { name: '附近商户' })).toBeVisible()
  await expect(page.getByText('距离：0.36 km')).toBeVisible()

  await page.goto('/blogs/publish')
  await expect(page).toHaveURL(/\/login/)
  await expect(page.getByRole('heading', { name: '短信登录' })).toBeVisible()
})
