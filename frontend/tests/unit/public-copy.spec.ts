import { describe, expect, it } from 'vitest'
import { copy } from '../../src/constants/copy'

describe('public copy', () => {
  it('keeps navigation and core public-facing default copy in Chinese', () => {
    expect(copy.appName).toBe('本地生活')
    expect(copy.nav.home).toBe('首页')
    expect(copy.nav.shops).toBe('商户')
    expect(copy.nav.vouchers).toBe('优惠')
    expect(copy.nav.blogs).toBe('探店')
    expect(copy.nav.hot).toBe('热榜')
    expect(copy.nav.login).toBe('登录')
  })

  it('keeps browsing, login guidance, and empty-state copy in Chinese', () => {
    expect(copy.home.heroTitle).toBe('发现身边好店与优惠')
    expect(copy.login.title).toBe('短信登录')
    expect(copy.login.tips).toBe('未登录也可以先浏览首页、商户、附近和热榜内容。')
    expect(copy.shops.nearbyTitle).toBe('附近商户')
    expect(copy.shops.empty).toBe('当前暂无可展示的商户，稍后再来看看。')
    expect(copy.vouchers.empty).toBe('暂无进行中的优惠活动。')
    expect(copy.blogs.empty).toBe('暂时还没有新的探店内容。')
    expect(copy.blogs.commonEmpty).toBe('你和目标用户暂时还没有共同关注。')
    expect(copy.common.loginPromptTitle).toBe('登录后继续')
    expect(copy.common.loginPromptBody).toBe('该操作需要先完成短信登录，你仍可继续浏览公开内容。')
    expect(copy.common.loginPromptAction).toBe('去登录')
  })
})
