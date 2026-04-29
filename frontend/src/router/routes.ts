export const publicRouteNames = [
  'home',
  'shop-list',
  'shop-detail',
  'nearby-shop',
  'voucher-list',
  'blog-feed',
  'hot-blog-list',
  'login',
] as const

export const protectedRouteNames = [
  'blog-publish',
  'common-follow',
] as const

export const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
    meta: { requiresAuth: false, title: '首页' },
  },
  {
    path: '/shops',
    name: 'shop-list',
    component: () => import('../views/ShopListView.vue'),
    meta: { requiresAuth: false, title: '商户列表' },
  },
  {
    path: '/shops/:shopId',
    name: 'shop-detail',
    component: () => import('../views/ShopDetailView.vue'),
    meta: { requiresAuth: false, title: '商户详情' },
  },
  {
    path: '/nearby',
    name: 'nearby-shop',
    component: () => import('../views/NearbyShopView.vue'),
    meta: { requiresAuth: false, title: '附近商户' },
  },
  {
    path: '/vouchers',
    name: 'voucher-list',
    component: () => import('../views/VoucherListView.vue'),
    meta: { requiresAuth: false, title: '优惠活动' },
  },
  {
    path: '/blogs',
    name: 'blog-feed',
    component: () => import('../views/BlogFeedView.vue'),
    meta: { requiresAuth: false, title: '探店内容流' },
  },
  {
    path: '/blogs/hot',
    name: 'hot-blog-list',
    component: () => import('../views/HotBlogListView.vue'),
    meta: { requiresAuth: false, title: '点赞热榜' },
  },
  {
    path: '/blogs/publish',
    name: 'blog-publish',
    component: () => import('../views/BlogPublishView.vue'),
    meta: { requiresAuth: true, title: '发布探店笔记' },
  },
  {
    path: '/follows/common/:targetUserId',
    name: 'common-follow',
    component: () => import('../views/CommonFollowView.vue'),
    meta: { requiresAuth: true, title: '共同关注' },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false, title: '短信登录' },
  },
]
