import { post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

/** 意见反馈分页（对齐 AdminFeedbackController POST admin/feedback/page） */
export function getFeedbackPage(data = {}) {
  return post('admin/feedback/page', data).then(resolveBusinessResponse).then(resolvePageData)
}

/** 标记意见反馈已处理（对齐 AdminFeedbackController POST admin/feedback/process） */
export function processFeedback(data) {
  return post('admin/feedback/process', data).then(resolveBusinessResponse)
}
