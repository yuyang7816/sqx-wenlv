<template>
	<view class="notice-detail-page">
		<view class="notice-detail-body">
			<view v-if="notice" class="notice-detail-card ry-card">
				<view class="notice-detail-card__head">
					<view v-if="notice.pinned" class="notice-detail-card__tag">
						<text class="notice-detail-card__tag-text">{{ ui.tagPinned }}</text>
					</view>
					<text class="notice-detail-card__title">{{ notice.title }}</text>
				</view>
				<text class="notice-detail-card__time">{{ notice.time }} · {{ notice.source }}</text>
				<view class="notice-detail-card__content">
					<up-parse
						:content="notice.content"
						:preview-img="true"
						:tag-style="contentTagStyle"
					/>
				</view>
			</view>
			<view v-else-if="!pageLoading" class="notice-detail-empty">
				<text class="notice-detail-empty__text">公告不存在或已下架</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { NOTICE_UI, fetchNoticeDetail } from '@/common/api/notice.js';

const ui = NOTICE_UI;
const noticeId = ref('');
const notice = ref(null);
const pageLoading = ref(false);

/** 与管理端 Quill 富文本样式对齐 */
const contentTagStyle = {
	p: 'font-size:28rpx;line-height:1.8;color:#6B6B78;margin:0 0 16rpx;',
	h1: 'font-size:36rpx;font-weight:600;line-height:1.5;color:#1A1A1F;margin:24rpx 0 16rpx;',
	h2: 'font-size:32rpx;font-weight:600;line-height:1.5;color:#1A1A1F;margin:20rpx 0 12rpx;',
	h3: 'font-size:30rpx;font-weight:600;line-height:1.5;color:#1A1A1F;margin:16rpx 0 12rpx;',
	strong: 'font-weight:600;color:#1A1A1F;',
	a: 'color:#FF6600;text-decoration:underline;',
	img: 'max-width:100%;height:auto;display:block;margin:16rpx 0;border-radius:12rpx;',
	ul: 'padding-left:32rpx;margin:0 0 16rpx;',
	ol: 'padding-left:32rpx;margin:0 0 16rpx;',
	li: 'font-size:28rpx;line-height:1.8;color:#6B6B78;margin-bottom:8rpx;',
	blockquote: 'margin:16rpx 0;padding:16rpx 20rpx;background:#F7F8FA;border-left:6rpx solid #FF6600;color:#6B6B78;'
};

function mapDetailData(record) {
	return {
		id: record.id,
		title: record.title || '',
		content: record.content || record.summary || '',
		time: record.time || '',
		source: record.source || '',
		pinned: !!record.pinned
	};
}

/** onShow 拉取公告详情（公开接口） */
async function loadNoticeDetail() {
	if (!noticeId.value || pageLoading.value) {
		return;
	}
	pageLoading.value = true;
	try {
		const res = await fetchNoticeDetail(noticeId.value);
		if (res.code === 0 && res.data) {
			notice.value = mapDetailData(res.data);
		} else {
			notice.value = null;
			uni.showToast({ title: res.msg || '公告详情加载失败', icon: 'none' });
		}
	} catch (e) {
		notice.value = null;
		uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
}

onLoad((options) => {
	noticeId.value = options?.id || '';
	uni.setNavigationBarTitle({ title: ui.detailTitle });
});

onShow(() => {
	if (noticeId.value) {
		loadNoticeDetail();
	}
});
</script>

<style lang="scss" scoped>
.notice-detail-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
}

.notice-detail-body {
	padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
}

.notice-detail-card {
	padding: 28rpx 24rpx;
	overflow: hidden;
}

.notice-detail-card__head {
	display: flex;
	flex-direction: row;
	align-items: flex-start;
	gap: 12rpx;
	margin-bottom: 16rpx;
}

.notice-detail-card__tag {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	height: 36rpx;
	padding: 0 12rpx;
	margin-top: 4rpx;
	background-color: var(--ry-brand);
	border-radius: var(--ry-radius-xs);
}

.notice-detail-card__tag-text {
	font-size: 22rpx;
	font-weight: 500;
	color: #ffffff;
	line-height: 1;
}

.notice-detail-card__title {
	flex: 1;
	min-width: 0;
	font-size: 34rpx;
	font-weight: 600;
	color: var(--ry-text-primary);
	line-height: 1.5;
}

.notice-detail-card__time {
	display: block;
	margin-bottom: 24rpx;
	font-size: 24rpx;
	color: var(--ry-text-hint);
	line-height: 1.4;
}

.notice-detail-card__content {
	width: 100%;
	overflow: hidden;
	word-break: break-word;
}

.notice-detail-empty {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 120rpx 48rpx;
	background-color: var(--ry-card-bg);
	border-radius: var(--ry-radius-md);
	box-shadow: var(--ry-card-shadow);
}

.notice-detail-empty__text {
	font-size: 28rpx;
	color: var(--ry-text-hint);
	line-height: 1.5;
}
</style>
