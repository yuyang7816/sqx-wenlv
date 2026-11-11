import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi, resolvePageData } from '@/api/admin'

/** 酒店管理 CRUD（对齐 AdminHotelController POST admin/hotel/*） */
export const hotelApi = createPageApi('admin/hotel')

/** 房型管理（对齐 AdminHotelController POST admin/hotel/room/*） */
export const hotelRoomApi = {
  page(data = {}) {
    return post('admin/hotel/room/page', data).then(resolveBusinessResponse).then(resolvePageData)
  },
  save(data) {
    return post('admin/hotel/room/save', data).then(resolveBusinessResponse)
  },
  update(data) {
    return post('admin/hotel/room/update', data).then(resolveBusinessResponse)
  },
  delete(data) {
    return post('admin/hotel/room/delete', data).then(resolveBusinessResponse)
  },
}
