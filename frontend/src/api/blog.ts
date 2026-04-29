import { http } from './http'

export interface BlogCard {
  id: number
  userId: number
  shopId: number
  title: string
  content: string
  images?: string | null
  liked: number
  isLiked: boolean
  authorName: string
  authorIcon?: string | null
  shopName: string
  createTime?: string
}

export interface BlogScrollResult {
  list: BlogCard[]
  lastId?: number | null
  offset: number
  hasMore?: boolean
}

export interface BlogPublishPayload {
  title: string
  content: string
  shopId: number
  images?: string
}

export async function fetchBlogFeed(lastId?: number, offset?: number) {
  const { data } = await http.get('/blogs/feed', { params: { lastId, offset } })
  return data.data as BlogScrollResult
}

export async function fetchHotBlogs() {
  const { data } = await http.get('/blogs/hot')
  return data.data as BlogCard[]
}

export async function publishBlog(payload: BlogPublishPayload) {
  const { data } = await http.post('/blogs', payload)
  return data.data as BlogCard
}

export async function toggleBlogLike(blogId: number) {
  const { data } = await http.post(`/blogs/${blogId}/like`)
  return data.data as boolean
}
