import { expect, test } from './fixtures/auth'

test('已登录用户可浏览活动、抢券并完成签到', async ({ authenticatedPage: page }) => {
  await page.route('**/api/vouchers/seckill', async (route) => {
    await route.fulfill({
      json: {
        success: true,
        data: [
          {
            id: 1,
            title: '双人晚餐套餐',
            subTitle: '限时秒杀，先到先得',
            payValue: 9900,
            actualValue: 19900,
            stock: 12,
            status: 1,
          },
        ],
      },
    })
  })
  await page.route('**/api/vouchers/seckill/1/claim', async (route) => {
    await route.fulfill({ json: { success: true, message: '成功', data: { orderId: 101 } } })
  })
  await page.route('**/api/sign', async (route) => {
    if (route.request().method() === 'POST') {
      await route.fulfill({ json: { success: true, message: '成功', data: null } })
      return
    }
    await route.fulfill({ json: { success: true, message: '成功', data: 3 } })
  })
  await page.route('**/api/sign/streak', async (route) => {
    await route.fulfill({ json: { success: true, message: '成功', data: 3 } })
  })

  await page.goto('/vouchers')
  await expect(page.getByRole('heading', { name: '限时优惠' })).toBeVisible()
  await expect(page.getByText('双人晚餐套餐')).toBeVisible()

  await page.getByRole('button', { name: '立即抢券' }).click()
  await expect(page.getByText('领取成功')).toBeVisible()

  await page.getByRole('button', { name: '今日签到' }).click()
  await expect(page.getByText('已连续签到 3 天')).toBeVisible()
})
