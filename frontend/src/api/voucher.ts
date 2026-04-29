import { http } from './http'

export interface Voucher {
  id: number
  shopId: number
  title: string
  subTitle: string
  payValue: number
  actualValue: number
  stock: number
  beginTime: string
  endTime: string
  status: number
}

export async function fetchSeckillVouchers() {
  const { data } = await http.get('/vouchers/seckill')
  return data.data as Voucher[]
}

export async function claimSeckillVoucher(voucherId: number) {
  const { data } = await http.post(`/vouchers/seckill/${voucherId}/claim`)
  return data.data as number
}
