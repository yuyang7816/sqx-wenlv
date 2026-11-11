import { post, resolveBusinessResponse } from '@/api/http'

/** 业务概览：GMV / 订单 / 商户 / 用户等指标 */
export function getStatsOverview() {
  return post('admin/stats/overview', {}).then(resolveBusinessResponse)
}

/** 业务趋势：近 N 日订单数与 GMV 折线 */
export function getStatsTrend(days = 30) {
  return post('admin/stats/trend', { days }).then(resolveBusinessResponse)
}

/** 侧边栏菜单角标：待审核/待处理等待办数量 */
export function getMenuBadges() {
  return post('admin/stats/menu-badges', {}).then(resolveBusinessResponse)
}
