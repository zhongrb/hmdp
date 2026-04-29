import { expect, test } from './fixtures/auth'

test('已登录用户可发布探店、点赞并查看共同关注', async ({ authenticatedPage: page }) => {
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
              content: '环境舒服，菜品稳定，适合朋友小聚。',
              liked: 12,
              isLiked: false,
              authorName: '阿星',
              shopName: '城南小馆',
              createTime: '2026-04-28T20:00:00',
            },
          ],
          minTime: 1,
          offset: 1,
        },
      },
    })
  })
  await page.route('**/api/blogs/hot', async (route) => {
    await route.fulfill({
      json: {
        success: true,
        data: [
          {
            id: 1,
            title: '晚餐值得再来',
            liked: 13,
            isLiked: true,
            authorName: '阿星',
            shopName: '城南小馆',
          },
        ],
      },
    })
  })
  await page.route('**/api/blogs', async (route) => {
    if (route.request().method() === 'POST') {
      await route.fulfill({
        json: {
          success: true,
          data: { id: 3, title: '新店体验', content: '值得再来', shopId: 1 },
        },
      })
      return
    }
    await route.fallback()
  })
  await page.route('**/api/blogs/1/like', async (route) => {
    await route.fulfill({ json: { success: true, data: true } })
  })
  await page.route('**/api/follows/common/2', async (route) => {
    await route.fulfill({
      json: {
        success: true,
        data: [{ id: 3, nickName: '团团', icon: null }],
      },
    })
  })

  await page.goto('/blogs')
  await expect(page.getByRole('heading', { name: '探店内容流' })).toBeVisible()
  await expect(page.getByText('晚餐值得再来')).toBeVisible()

  await page.goto('/blogs/publish')
  await page.getByLabel('标题').fill('新店体验')
  await page.getByLabel('内容').fill('值得再来')
  await page.getByLabel('关联商户 ID').fill('1')
  await page.getByRole('button', { name: '发布笔记' }).click()
  await expect(page.getByText('发布成功')).toBeVisible()

  await page.goto('/blogs/hot')
  await page.getByRole('button', { name: '点赞' }).click()
  await expect(page.getByText('已点赞')).toBeVisible()

  await page.goto('/follows/common/2')
  await expect(page.getByText('团团')).toBeVisible()
})
