import { expect, test } from '@playwright/test'

test('公开页面展示中文空状态，未登录互动时弹出登录引导', async ({ page }) => {
  await page.route('**/api/shops/types', async (route) => {
    await route.fulfill({
      json: {
        success: true,
        data: [{ id: 1, name: '美食', sort: 1 }],
      },
    })
  })

  await page.route('**/api/shops?page=1&typeId=1', async (route) => {
    await route.fulfill({ json: { success: true, data: [] } })
  })

  await page.route('**/api/shops/nearby**', async (route) => {
    await route.fulfill({ json: { success: true, data: [] } })
  })

  await page.route('**/api/vouchers/seckill', async (route) => {
    await route.fulfill({ json: { success: true, data: [] } })
  })

  await page.route('**/api/blogs/feed**', async (route) => {
    await route.fulfill({
      json: {
        success: true,
        data: {
          list: [
            {
              id: 1,
              userId: 1,
              shopId: 1,
              title: '晚餐值得再来',
              content: '环境舒服，菜品稳定。',
              liked: 12,
              isLiked: false,
              authorName: '阿星',
              shopName: '城南小馆',
              createTime: '2026-04-28T20:00:00',
            },
          ],
          lastId: 1,
          offset: 1,
        },
      },
    })
  })

  await page.route('**/api/blogs/hot', async (route) => {
    await route.fulfill({ json: { success: true, data: [] } })
  })

  await page.goto('/shops')
  await expect(page.getByText('当前分类还没有可展示商户')).toBeVisible()
  await expect(page.getByText('当前暂无可展示的商户，稍后再来看看。')).toBeVisible()

  await page.goto('/nearby')
  await expect(page.getByText('当前坐标附近暂无可展示结果')).toBeVisible()
  await expect(page.getByText('当前暂无可展示的商户，稍后再来看看。')).toBeVisible()

  await page.goto('/vouchers')
  await expect(page.getByText('暂无进行中的优惠活动。')).toBeVisible()

  await page.goto('/blogs/hot')
  await expect(page.getByText('暂时还没有新的探店内容。')).toBeVisible()

  await page.goto('/blogs')
  await page.getByRole('button', { name: '点赞' }).click()
  await expect(page.getByRole('dialog')).toBeVisible()
  await expect(page.getByRole('heading', { name: '登录后继续' })).toBeVisible()
  await expect(page.getByText('该操作需要先完成短信登录，你仍可继续浏览公开内容。')).toBeVisible()
  await page.getByRole('button', { name: '去登录' }).click()
  await expect(page).toHaveURL(/\/login\?redirect=%2Fblogs/)
})
