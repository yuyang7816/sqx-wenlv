/**
 * 邀请分销 API（路径对齐 AppInviteController）
 */
import { get } from './request.js';

const PREFIX = '/app/invite';
const PAGE_SIZE = 10;

/** 邀请展示配置（需 App JWT） */
export function fetchInviteConfig() {
	return get(`${PREFIX}/config`);
}

/** 邀请统计与用户展示信息（需 App JWT） */
export function fetchInviteSummary() {
	return get(`${PREFIX}/summary`);
}

/** 团队汇总（需 App JWT） */
export function fetchInviteTeamSummary() {
	return get(`${PREFIX}/team/summary`);
}

/** 团队成员分页（level: 1|2） */
export function fetchInviteTeamMembers(params = {}) {
	return get(`${PREFIX}/team/members`, {
		level: params.level || 1,
		page: params.page || 1,
		limit: params.limit || PAGE_SIZE
	});
}

/** 收益概览（需 App JWT） */
export function fetchInviteCommissionSummary() {
	return get(`${PREFIX}/commission/summary`);
}

/** 收益明细分页（level: 1|2） */
export function fetchInviteCommissionDetails(params = {}) {
	return get(`${PREFIX}/commission/details`, {
		level: params.level || 1,
		page: params.page || 1,
		limit: params.limit || PAGE_SIZE
	});
}

export { PAGE_SIZE as INVITE_PAGE_SIZE };
