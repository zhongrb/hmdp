export const copy = {
  appName: '本地生活',
  nav: {
    home: '首页',
    shops: '商户',
    nearby: '附近',
    vouchers: '优惠',
    blogs: '探店',
    hot: '热榜',
    login: '登录',
  },
  login: {
    title: '短信登录',
    phoneLabel: '手机号',
    codeLabel: '验证码',
    sendCode: '发送验证码',
    submit: '立即登录',
    agreement: '登录即表示同意平台服务协议与隐私说明。',
    success: '登录成功，欢迎回来。',
    required: '请先完成登录后再继续操作。',
  },
  home: {
    heroTitle: '发现身边好店与优惠',
    heroSubtitle: '先逛再决定，登录后即可抢券、签到和互动。',
  },
  shops: {
    listTitle: '精选商户',
    detailTitle: '商户详情',
    nearbyTitle: '附近商户',
    empty: '当前暂无可展示的商户，稍后再来看看。',
    locationDenied: '未获得定位权限，已为你展示默认商户列表。',
  },
  vouchers: {
    title: '限时优惠',
    empty: '暂无进行中的优惠活动。',
  },
  blogs: {
    feedTitle: '探店内容流',
    publishTitle: '发布探店笔记',
    hotTitle: '点赞热榜',
    empty: '暂时还没有新的探店内容。',
  },
  common: {
    loading: '加载中，请稍候…',
    empty: '暂无数据',
    loginPromptTitle: '登录后继续',
    loginPromptBody: '该操作需要先完成短信登录，你仍可继续浏览公开内容。',
    loginPromptAction: '去登录',
  },
  errors: {
    network: '网络开小差了，请稍后重试。',
    unauthorized: '当前操作需要登录后才能进行。',
    invalidPhone: '请输入正确的手机号。',
    invalidCode: '验证码无效或已过期。',
  },
} as const

export type AppCopy = typeof copy
