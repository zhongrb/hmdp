import { http } from './http'

export interface Shop {
  id: number
  name: string
  typeId: number
  address: string
  images?: string | null
  avgPrice?: number | null
  comments: number
  score: number
  openHours?: string | null
  distance?: number | null
}

export interface ShopType {
  id: number
  name: string
  sort: number
  icon?: string | null
}

export async function fetchShopTypes() {
  const { data } = await http.get('/shops/types')
  return data.data as ShopType[]
}

export async function fetchShopList(typeId?: number, page = 1) {
  const { data } = await http.get('/shops', { params: { typeId, page } })
  return data.data as Shop[]
}

export async function fetchShopDetail(shopId: string | number) {
  const { data } = await http.get(`/shops/${shopId}`)
  return data.data as Shop
}

export async function fetchNearbyShops(typeId: number, x: number, y: number, current = 1) {
  const { data } = await http.get('/shops/nearby', { params: { typeId, x, y, current } })
  return data.data as Shop[]
}
