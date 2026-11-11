/**
 * 首页 API（路径对齐 AppBannerController / AppHomeController / AppNoticeController）
 */
import { get, postJson } from './request.js';
import { appendLocationParams } from '../location-params.js';

/** 首页轮播（公开接口，classify=1 轮播图，state=1 正常） */
export function fetchBannerPage(params = {}) {
	return postJson('/app/base/banner/page', {
		page: params.page || 1,
		limit: params.limit || 10,
		classify: params.classify ?? 1,
		state: params.state ?? 1
	});
}

/** 首页推荐 Tab 聚合（公开接口，按 lat/lng 解析目的地） */
export function fetchHomeRecommend(locationParams = {}) {
	return get('/app/home/recommend', appendLocationParams({}, locationParams));
}

/** 首页宫格/Tab 模块配置（公开接口，type=grid|tab） */
export function fetchHomeModules(type, locationParams = {}) {
	return get('/app/home/modules', appendLocationParams({ type }, locationParams));
}

/** 公告跑马灯（公开接口，最新 3 条） */
export function fetchNoticeMarquee(locationParams = {}) {
	return get('/app/notice/marquee', appendLocationParams({}, locationParams));
}
