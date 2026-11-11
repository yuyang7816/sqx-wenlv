<template>
	<view class="msg-page ry-page">
		<view class="msg-body">
			<view v-if="loggedIn" class="msg-toolbar">
				<text class="msg-toolbar__hint">{{ unreadTotal > 0 ? `${unreadTotal} 条未读` : ui.markAllReadEmpty }}</text>
				<view
					class="msg-toolbar__btn"
					:class="{
						'msg-toolbar__btn--loading': markingAll,
						'msg-toolbar__btn--disabled': unreadTotal <= 0
					}"
					@click="onMarkAllRead"
				>
					<text class="msg-toolbar__btn-text">{{ ui.markAllRead }}</text>
				</view>
			</view>

			<view class="msg-list">
				<view v-if="!loggedIn" class="msg-empty">
					<view class="msg-empty__icon">
						<u-icon name="email" color="#CCCCCC" :size="80" />
					</view>
					<text class="msg-empty__text">{{ ui.emptyNotLoggedIn }}</text>
					<text class="msg-empty__hint">{{ ui.emptyNotLoggedInHint }}</text>
				</view>
				<view v-else-if="messageList.length === 0 && !loading" class="msg-empty">
					<view class="msg-empty__icon">
						<u-icon name="email" color="#CCCCCC" :size="80" />
					</view>
					<text class="msg-empty__text">{{ ui.emptyNoMessagePrefix }}{{ ui.emptyNoMessageSuffix }}</text>
					<text class="msg-empty__hint">{{ ui.emptyHint }}</text>
				</view>
				<view v-for="item in messageList" :key="item.id" class="msg-card ry-card" @click="onMsgClick(item)">
					<view class="msg-card__left">
						<view class="msg-card__icon-wrap">
							<u-icon
								:name="item.category === 'system' ? 'bell-fill' : 'calendar-fill'"
								color="#FFFFFF"
								:size="26"
							/>
						</view>
					</view>
					<view class="msg-card__content">
						<view class="msg-card__head">
							<text class="msg-card__title"
								:class="{ 'msg-card__title--unread': item.unread }">{{ item.title }}</text>
							<view v-if="item.unread" class="msg-card__dot"></view>
						</view>
						<text class="msg-card__summary">{{ item.summary }}</text>
						<view class="msg-card__foot">
							<text class="msg-card__time">{{ item.time }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
	import { hasToken } from '@/common/api/auth.js';
	import {
		fetchMessagePage,
		fetchMessageUnreadCount,
		markAllMessagesRead,
		mapMessageVo,
		MSG_DETAIL_PATH,
		MESSAGE_PAGE_SIZE
	} from '@/common/api/messages.js';
	import { MSG_UI } from '@/common/messages-ui.js';

	const ui = MSG_UI;
	const loggedIn = ref(false);
	const loading = ref(false);
	const messageList = ref([]);
	const page = ref(1);
	const hasMore = ref(true);
	const unreadTotal = ref(0);
	const markingAll = ref(false);

	onShow(() => {
		loggedIn.value = hasToken();
		if (!loggedIn.value) {
			messageList.value = [];
			unreadTotal.value = 0;
			return;
		}
		loadMessages(true);
		loadUnreadCount();
	});

	onPullDownRefresh(async () => {
		if (!loggedIn.value) {
			uni.stopPullDownRefresh();
			return;
		}
		await Promise.all([loadMessages(true), loadUnreadCount()]);
		uni.stopPullDownRefresh();
	});

	onReachBottom(() => {
		if (!loggedIn.value || loading.value || !hasMore.value) return;
		page.value += 1;
		loadMessages(false);
	});

	function resetUnreadCounts() {
		unreadTotal.value = 0;
	}

	async function loadUnreadCount() {
		if (!hasToken()) {
			resetUnreadCounts();
			return;
		}
		try {
			const res = await fetchMessageUnreadCount();
			if (res.code === 0 && res.data) {
				unreadTotal.value = Number(res.data.count) || 0;
			} else {
				resetUnreadCounts();
			}
		} catch (e) {
			resetUnreadCounts();
		}
	}

	async function loadMessages(reset = false) {
		if (!hasToken()) {
			messageList.value = [];
			return;
		}
		if (loading.value) return;
		if (!reset && !hasMore.value) return;

		if (reset) {
			page.value = 1;
			hasMore.value = true;
		}

		loading.value = true;
		const params = {
			page: page.value,
			limit: MESSAGE_PAGE_SIZE
		};

		try {
			const res = await fetchMessagePage(params);
			if (res.code === 0 && res.data) {
				const list = (res.data.records || []).map(mapMessageVo);
				messageList.value = reset ? list : [...messageList.value, ...list];
				const total = Number(res.data.total ?? 0);
				hasMore.value = list.length >= MESSAGE_PAGE_SIZE && messageList.value.length < total;
			} else {
				if (reset) messageList.value = [];
				uni.showToast({ title: res.msg || ui.toastLoadFail, icon: 'none' });
			}
		} catch (e) {
			if (reset) messageList.value = [];
			uni.showToast({ title: ui.toastNetworkError, icon: 'none' });
		} finally {
			loading.value = false;
		}
	}

	function onMsgClick(item) {
		const query = `id=${item.id}`;
		uni.navigateTo({ url: `${MSG_DETAIL_PATH}?${query}` });
	}

	async function onMarkAllRead() {
		if (!loggedIn.value || markingAll.value) return;
		if (unreadTotal.value <= 0) {
			uni.showToast({ title: ui.markAllReadEmpty, icon: 'none' });
			return;
		}
		markingAll.value = true;
		try {
			const res = await markAllMessagesRead();
			if (res.code === 0) {
				const count = Number(res.data?.count ?? 0);
				messageList.value = messageList.value.map((item) => ({ ...item, unread: false }));
				await Promise.all([loadUnreadCount(), loadMessages(true)]);
				uni.showToast({
					title: count > 0 ? ui.markAllReadSuccess : ui.markAllReadEmpty,
					icon: 'none'
				});
			} else {
				uni.showToast({ title: res.msg || ui.toastLoadFail, icon: 'none' });
			}
		} catch (e) {
			uni.showToast({ title: ui.toastNetworkError, icon: 'none' });
		} finally {
			markingAll.value = false;
		}
	}
</script>

<style lang="scss" scoped>
	.msg-page {
		min-height: 100vh;
		background: linear-gradient(180deg, #FFF8F3 0%, #F5F7FF 460rpx);
	}

	.msg-body {
		padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
	}

	/* #ifdef MP-WEIXIN */
	.msg-body {
		padding-bottom: 120rpx;
	}

	/* #endif */

	.msg-toolbar {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx var(--ry-page-padding-x) 12rpx;
		gap: 16rpx;

		&__hint {
			font-size: 24rpx;
			color: #999999;
			line-height: 1.4;
		}

		&__btn {
			display: inline-flex;
			align-items: center;
			justify-content: center;
			height: 56rpx;
			padding: 0 24rpx;
			border-radius: 999rpx;
			background-color: #FFFFFF;
			border: 2rpx solid #FFE4D1;
			transition: all 0.2s ease;

			&:active {
				transform: scale(0.98);
				opacity: 0.85;
			}

			&--loading {
				opacity: 0.6;
				pointer-events: none;
			}

			&--disabled {
				opacity: 0.45;
			}
		}

		&__btn-text {
			font-size: 24rpx;
			font-weight: 600;
			color: #FF6600;
			line-height: 1;
		}
	}

	.msg-list {
		padding: 8rpx var(--ry-page-padding-x) 0;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.msg-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 48rpx 120rpx;

		&__icon {
			margin-bottom: 24rpx;
		}

		&__text {
			font-size: 28rpx;
			font-weight: 600;
			color: #999999;
			line-height: 1.5;
			margin-bottom: 12rpx;
		}

		&__hint {
			font-size: 24rpx;
			color: #CCCCCC;
			line-height: 1.5;
		}
	}

	.msg-card {
		display: flex;
		align-items: flex-start;
		padding: 26rpx 24rpx 24rpx;
		gap: 20rpx;
		transition: all 0.2s ease;

		&:active {
			transform: scale(0.98);
			opacity: 0.9;
		}
	}

	.msg-card__left {
		flex-shrink: 0;
		padding-top: 4rpx;
	}

	.msg-card__icon-wrap {
		width: 92rpx;
		height: 92rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #FF8A3D, #FF6600);
		box-shadow: 0 4rpx 16rpx rgba(255, 102, 0, 0.25);
	}

	.msg-card__content {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.msg-card__head {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.msg-card__title {
		flex: 1;
		min-width: 0;
		font-size: 30rpx;
		font-weight: 500;
		color: #333333;
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.msg-card__title--unread {
		font-weight: 700;
		color: #1A1A1A;
	}

	.msg-card__dot {
		flex-shrink: 0;
		width: 16rpx;
		height: 16rpx;
		border-radius: 50%;
		background-color: #FF4D4F;
	}

	.msg-card__summary {
		font-size: 26rpx;
		color: #666666;
		line-height: 1.6;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.msg-card__foot {
		display: flex;
		align-items: center;
		justify-content: flex-end;
		margin-top: 6rpx;
	}

	.msg-card__time {
		flex-shrink: 0;
		font-size: 24rpx;
		color: #999999;
		line-height: 1.4;
		white-space: nowrap;
	}
</style>
