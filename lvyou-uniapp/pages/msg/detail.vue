<template>
	<view class="msg-detail-page ry-page">
		<view v-if="loading" class="msg-detail-loading">
			<u-loading-icon mode="circle" color="#FF6600" />
			<text class="msg-detail-loading__text">加载中...</text>
		</view>
		<view v-else-if="loadError" class="msg-detail-empty">
			<text class="msg-detail-empty__text">{{ loadError }}</text>
		</view>
		<view v-else class="msg-detail-body">
			<view class="msg-header">
				<view class="msg-header__icon-wrap">
					<u-icon
						:name="message.category === 'system' ? 'bell-fill' : 'calendar-fill'"
						color="#FFFFFF"
						:size="34"
					/>
				</view>
			</view>
			<view class="msg-content ry-card">
				<view class="msg-content__head">
					<text class="msg-content__title">{{ message.title }}</text>
					<view class="msg-content__meta">
						<u-icon name="clock" color="#CCCCCC" :size="14" />
						<text class="msg-content__time">{{ message.time }}</text>
					</view>
				</view>
				<view class="msg-content__divider"></view>
				<text class="msg-content__text">{{ message.content }}</text>
				<view v-if="message.linkUrl" class="msg-content__action">
					<view class="msg-action-btn" @click="onActionClick">
						<text class="msg-action-btn__text">{{ message.linkText }}</text>
						<u-icon name="arrow-right" color="#FF6600" :size="16" />
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { hasToken } from '@/common/api/auth.js';
import { fetchMessageInfo, markMessageRead, mapMessageVo } from '@/common/api/messages.js';

const loading = ref(true);
const loadError = ref('');
const messageId = ref(null);
const message = ref({
	category: 'system',
	categoryLabel: '系统通知',
	title: '',
	content: '',
	time: '',
	linkUrl: '',
	linkText: ''
});

onLoad((options) => {
	messageId.value = options?.id ? Number(options.id) : null;
	if (!hasToken()) {
		loadError.value = '请先登录后查看消息';
		loading.value = false;
		setTimeout(() => {
			uni.navigateTo({ url: '/pages/my/login', fail: () => {} });
		}, 400);
		return;
	}
	if (!messageId.value) {
		loadError.value = '消息不存在';
		loading.value = false;
		return;
	}
	loadDetail();
});

async function loadDetail() {
	loading.value = true;
	loadError.value = '';
	try {
		const res = await fetchMessageInfo(messageId.value);
		if (res.code === 0 && res.data) {
			message.value = mapMessageVo(res.data);
			if (message.value.unread) {
				await markMessageRead(messageId.value).catch(() => {});
			}
		} else {
			loadError.value = res.msg || '消息不存在';
		}
	} catch (e) {
		loadError.value = '网络异常，请稍后重试';
	} finally {
		loading.value = false;
	}
}

function onActionClick() {
	if (!message.value.linkUrl) return;
	uni.navigateTo({ url: message.value.linkUrl });
}
</script>

<style lang="scss" scoped>
	.msg-detail-page {
		min-height: 100vh;
		background: linear-gradient(180deg, #FFF8F3 0%, #F5F7FF 560rpx);
	}

	.msg-detail-body {
		padding: 0 var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	}

	.msg-detail-loading,
	.msg-detail-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		min-height: 60vh;
		gap: 16rpx;
	}

	.msg-detail-loading__text,
	.msg-detail-empty__text {
		font-size: 28rpx;
		color: #999999;
	}

	.msg-header {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 40rpx 0 48rpx;

		&__icon-wrap {
			width: 120rpx;
			height: 120rpx;
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			background: linear-gradient(135deg, #FF8A3D, #FF6600);
			box-shadow: 0 12rpx 32rpx rgba(255, 102, 0, 0.25);
		}
	}

	.msg-content {
		padding: 32rpx 28rpx 36rpx;

		&__head {
			margin-bottom: 24rpx;
		}

		&__title {
			display: block;
			font-size: 36rpx;
			font-weight: 700;
			color: #1A1A1A;
			line-height: 1.5;
			margin-bottom: 16rpx;
		}

		&__meta {
			display: flex;
			align-items: center;
			gap: 8rpx;
		}

		&__time {
			font-size: 24rpx;
			color: #999999;
			line-height: 1.4;
		}

		&__divider {
			height: 1rpx;
			background: linear-gradient(90deg,
				transparent 0%,
				#E8E8E8 20%,
				#E8E8E8 80%,
				transparent 100%
			);
			margin-bottom: 28rpx;
		}

		&__text {
			display: block;
			font-size: 28rpx;
			color: #555555;
			line-height: 1.85;
			letter-spacing: 0.5rpx;
		}

		&__action {
			margin-top: 36rpx;
			padding-top: 28rpx;
			border-top: 2rpx dashed #F0F0F0;
		}
	}

	.msg-action-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		height: 88rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #FFF8F3, #FFF3EB);
		border: 2rpx solid #FFE4D1;
		transition: all 0.2s ease;

		&:active {
			transform: scale(0.98);
			opacity: 0.9;
		}

		&__text {
			font-size: 28rpx;
			font-weight: 700;
			color: #FF6600;
			line-height: 1;
		}
	}
</style>
