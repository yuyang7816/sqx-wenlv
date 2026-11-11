<template>
	<view class="im-chat-page">
		<scroll-view
			class="im-chat-scroll"
			scroll-y
			:scroll-into-view="scrollIntoView"
			:scroll-with-animation="true"
		>
			<view class="im-chat-body">
				<text class="im-chat-time">{{ chatTime }}</text>
				<view
					v-for="msg in messages"
					:key="msg.id"
					:id="'msg-' + msg.id"
					class="im-chat-row"
					:class="{ 'im-chat-row--user': msg.role === 'user' }"
				>
					<view v-if="msg.role === 'staff'" class="im-chat-avatar im-chat-avatar--staff">
						<u-icon name="server-man" color="#FFFFFF" :size="20" />
					</view>
					<view v-else class="im-chat-avatar im-chat-avatar--user">
						<u-icon name="account-fill" color="#999999" :size="22" />
					</view>
					<view
						class="im-chat-bubble"
						:class="msg.role === 'user' ? 'im-chat-bubble--user' : 'im-chat-bubble--staff'"
					>
						<text class="im-chat-bubble__text">{{ msg.content }}</text>
					</view>
				</view>
			</view>
		</scroll-view>

		<view class="im-chat-input-wrap">
			<view class="im-chat-input-bar">
				<view class="im-chat-input-bar__field">
					<input
						v-model="inputText"
						class="im-chat-input-bar__input"
						type="text"
						:placeholder="ui.placeholder"
						placeholder-class="im-chat-input-bar__placeholder"
						confirm-type="send"
						:disabled="sending || pageLoading"
						@confirm="onSend"
					/>
				</view>
				<view
					class="im-chat-input-bar__send"
					:class="{ 'im-chat-input-bar__send--disabled': !inputText.trim() || sending || pageLoading }"
					@click="onSend"
				>
					<u-icon name="arrow-right" color="#FFFFFF" :size="18" />
				</view>
			</view>
			<text class="im-chat-tip">{{ ui.tip }}</text>
		</view>
	</view>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app';
import { getProductName } from '@/common/brand.js';
import { fetchConfigInfo } from '@/common/api/config.js';
import { hasToken } from '@/common/api/auth.js';
import { goLogin } from '@/common/navigation.js';
import {
	openKefuImSession,
	sendKefuImMessage,
	fetchKefuImMessages
} from '@/common/api/kefu-im.js';

const ui = {
	placeholder: '请输入您要咨询的问题...',
	tip: '人工客服在线时间 09:00-21:00，非工作时间留言后将尽快回复',
	welcome: '您好，欢迎咨询平台在线客服，请描述您的问题，我们将尽快为您解答。',
	needLogin: '请先登录后再使用在线客服'
};

const inputText = ref('');
const scrollIntoView = ref('');
const sending = ref(false);
const pageLoading = ref(true);
const chatTime = ref('');
const messages = ref([]);
const sessionId = ref(null);
let pollTimer = null;

function formatChatTime() {
	const now = new Date();
	const pad = (n) => String(n).padStart(2, '0');
	return `${pad(now.getHours())}:${pad(now.getMinutes())}`;
}

function scrollToBottom(targetId) {
	nextTick(() => {
		scrollIntoView.value = '';
		nextTick(() => {
			scrollIntoView.value = targetId;
		});
	});
}

function mapMessage(item) {
	return {
		id: String(item.id),
		role: item.senderType === 'staff' ? 'staff' : 'user',
		content: item.content || ''
	};
}

function appendMessages(list, replace = false) {
	if (!Array.isArray(list) || !list.length) {
		return;
	}
	const mapped = list.map(mapMessage);
	if (replace) {
		messages.value = mapped;
	} else {
		const exists = new Set(messages.value.map((item) => item.id));
		mapped.forEach((item) => {
			if (!exists.has(item.id)) {
				messages.value.push(item);
			}
		});
	}
	const last = messages.value[messages.value.length - 1];
	if (last) {
		scrollToBottom(`msg-${last.id}`);
	}
}

async function loadMessages(initial = false) {
	try {
		const sinceId = initial ? undefined : getLastMessageId();
		const res = await fetchKefuImMessages({
			sinceId,
			limit: 100
		});
		if (res.code === 0) {
			appendMessages(res.data || [], initial);
			if (initial && !messages.value.length) {
				messages.value = [{ id: 'welcome', role: 'staff', content: ui.welcome }];
			}
		}
	} catch (e) {
		if (initial) {
			uni.showToast({ title: '消息加载失败', icon: 'none' });
		}
	}
}

function getLastMessageId() {
	const last = messages.value[messages.value.length - 1];
	if (!last) {
		return undefined;
	}
	const numeric = Number(last.id);
	return Number.isFinite(numeric) ? numeric : undefined;
}

async function initSession() {
	pageLoading.value = true;
	try {
		const res = await openKefuImSession();
		if (res.code === 0 && res.data) {
			sessionId.value = res.data.id;
		}
		await loadMessages(true);
	} catch (e) {
		uni.showToast({ title: '会话初始化失败', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
}

async function onSend() {
	const text = inputText.value.trim();
	if (!text || sending.value || pageLoading.value) {
		return;
	}
	inputText.value = '';
	sending.value = true;
	try {
		const res = await sendKefuImMessage(text);
		if (res.code === 0) {
			await loadMessages(false);
		} else {
			uni.showToast({ title: res.msg || '发送失败', icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: '发送失败，请稍后重试', icon: 'none' });
	} finally {
		sending.value = false;
	}
}

function startPolling() {
	stopPolling();
	pollTimer = setInterval(() => {
		loadMessages(false);
	}, 5000);
}

function stopPolling() {
	if (pollTimer) {
		clearInterval(pollTimer);
		pollTimer = null;
	}
}

onLoad(async () => {
	if (!hasToken()) {
		uni.showToast({ title: ui.needLogin, icon: 'none' });
		goLogin();
		return;
	}
	chatTime.value = formatChatTime();
	let productName = getProductName();
	try {
		const res = await fetchConfigInfo();
		if (res.code === 0 && res.data?.productName) {
			productName = res.data.productName;
		}
	} catch (e) { /* 使用本地品牌名 */ }
	uni.setNavigationBarTitle({ title: `${productName}在线客服` });
	await initSession();
});

onShow(() => {
	if (hasToken() && sessionId.value) {
		loadMessages(false);
		startPolling();
	}
});

onHide(() => {
	stopPolling();
});

onUnload(() => {
	stopPolling();
});
</script>

<style lang="scss" scoped>
.im-chat-page {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background: #f5f7ff;
}

.im-chat-scroll {
	flex: 1;
	min-height: 0;
}

.im-chat-body {
	padding: 24rpx var(--ry-page-padding-x, 32rpx) 32rpx;
}

.im-chat-time {
	display: block;
	text-align: center;
	font-size: 22rpx;
	color: #b7b7c4;
	margin-bottom: 24rpx;
}

.im-chat-row {
	display: flex;
	align-items: flex-start;
	gap: 16rpx;
	margin-bottom: 24rpx;

	&--user {
		flex-direction: row-reverse;
	}
}

.im-chat-avatar {
	flex-shrink: 0;
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;

	&--staff {
		background: linear-gradient(135deg, #2979ff, #5c9dff);
	}

	&--user {
		background: #eeeeee;
	}
}

.im-chat-bubble {
	max-width: 72%;
	padding: 20rpx 24rpx;
	border-radius: 20rpx;

	&--staff {
		background: #ffffff;
		border-top-left-radius: 6rpx;
		box-shadow: 0 4rpx 16rpx rgba(41, 121, 255, 0.08);
	}

	&--user {
		background: linear-gradient(135deg, #2979ff, #5c9dff);
		border-top-right-radius: 6rpx;
	}

	&__text {
		font-size: 28rpx;
		line-height: 1.5;
		color: #333333;
		word-break: break-word;
	}

	&--user &__text {
		color: #ffffff;
	}
}

.im-chat-input-wrap {
	flex-shrink: 0;
	padding: 16rpx var(--ry-page-padding-x, 32rpx) calc(16rpx + env(safe-area-inset-bottom));
	background: #ffffff;
	border-top: 1rpx solid #f0f0f0;
}

.im-chat-input-bar {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.im-chat-input-bar__field {
	flex: 1;
	min-width: 0;
	height: 72rpx;
	padding: 0 24rpx;
	border-radius: 999rpx;
	background: #f5f6fa;
}

.im-chat-input-bar__input {
	height: 72rpx;
	font-size: 28rpx;
	color: #333333;
}

.im-chat-input-bar__placeholder {
	color: #b7b7c4;
	font-size: 28rpx;
}

.im-chat-input-bar__send {
	flex-shrink: 0;
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #2979ff, #5c9dff);
	display: flex;
	align-items: center;
	justify-content: center;

	&--disabled {
		opacity: 0.45;
	}
}

.im-chat-tip {
	display: block;
	margin-top: 12rpx;
	font-size: 22rpx;
	color: #b7b7c4;
	line-height: 1.4;
	text-align: center;
}
</style>
