<template>
	<view class="ai-chat-page" :style="{ '--ai-accent': accentColor }">
		<scroll-view
			class="ai-chat-scroll"
			scroll-y
			:scroll-into-view="scrollIntoView"
			:scroll-with-animation="true"
		>
			<view class="ai-chat-body">
				<text class="ai-chat-time">{{ chatTime }}</text>

				<view
					v-for="msg in messages"
					:key="msg.id"
					:id="'msg-' + msg.id"
					class="ai-chat-row"
					:class="{ 'ai-chat-row--user': msg.role === 'user' }"
				>
					<view
						v-if="msg.role === 'assistant'"
						class="ai-chat-avatar ai-chat-avatar--ai"
					>
						<u-icon name="chat-fill" color="#FFFFFF" :size="20" />
					</view>
					<view
						v-else
						class="ai-chat-avatar ai-chat-avatar--user"
					>
						<image class="ai-chat-avatar__img" :src="userAvatar" mode="aspectFill" />
					</view>

					<view
						class="ai-chat-bubble"
						:class="msg.role === 'user' ? 'ai-chat-bubble--user' : 'ai-chat-bubble--ai'"
					>
						<text v-if="msg.type === 'welcome'" class="ai-chat-bubble__text ai-chat-bubble__text--title">
							{{ msg.title }}
						</text>
						<text class="ai-chat-bubble__text" :class="{ 'ai-chat-bubble__text--outro': msg.type === 'welcome' }">
							{{ msg.content }}
						</text>

						<template v-if="msg.type === 'welcome' && msg.hotQuestions?.length">
							<text class="ai-chat-bubble__hot-title">{{ ui.hotTitle }}</text>
							<view
								v-for="(item, index) in msg.hotQuestions"
								:key="item.id"
								class="ai-chat-bubble__hot-item"
								@click="onHotQuestion(item.text)"
							>
								<text class="ai-chat-bubble__hot-num">{{ index + 1 }}.</text>
								<text class="ai-chat-bubble__hot-text">{{ item.text }}</text>
							</view>
						</template>

						<view v-if="msg.links?.length" class="ai-chat-bubble__links">
							<view
								v-for="link in msg.links"
								:key="link.path"
								class="ai-chat-bubble__link"
								@click.stop="openLink(link.path)"
							>
								<text class="ai-chat-bubble__link-text">{{ link.label }}</text>
								<u-icon name="arrow-right" :color="accentColor" :size="12" />
							</view>
						</view>
					</view>
				</view>

				<view v-if="typing" id="msg-typing" class="ai-chat-row">
					<view class="ai-chat-avatar ai-chat-avatar--ai">
						<u-icon name="chat-fill" color="#FFFFFF" :size="20" />
					</view>
					<view class="ai-chat-bubble ai-chat-bubble--ai ai-chat-bubble--typing">
						<text class="ai-chat-bubble__text">{{ ui.typing }}</text>
					</view>
				</view>
			</view>
		</scroll-view>

		<view class="ai-chat-input-wrap">
			<view class="ai-chat-input-bar">
				<view class="ai-chat-input-bar__field">
					<input
						v-model="inputText"
						class="ai-chat-input-bar__input"
						type="text"
						:placeholder="ui.placeholder"
						placeholder-class="ai-chat-input-bar__placeholder"
						confirm-type="send"
						:disabled="typing"
						@confirm="onSend"
					/>
				</view>
				<view
					class="ai-chat-input-bar__send"
					:class="{ 'ai-chat-input-bar__send--disabled': !inputText.trim() || typing }"
					@click="onSend"
				>
					<u-icon name="arrow-right" color="#FFFFFF" :size="18" />
				</view>
			</view>
			<text class="ai-chat-disclaimer">{{ ui.disclaimer }}</text>
		</view>
	</view>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getModuleColor } from '@/common/theme.js';
import { postAiChat } from '@/common/api/ai.js';
import { getProductName } from '@/common/brand.js';
import { getDefaultAvatar, resolveAvatar } from '@/common/avatar.js';
import { getUserInfo, hasToken } from '@/common/api/auth.js';

const productName = getProductName();

/** 页面 UI 文案（欢迎语与热门问题为本地展示，对话回复走接口） */
const ui = {
	navTitle: `${productName} AI 助手`,
	placeholder: '问我旅游、美食、酒店、线路相关问题...',
	disclaimer: 'AI 回答仅供参考，出行请以官方公告与订单信息为准',
	welcomeTitle: `您好，我是${productName} AI 助手`,
	welcomeDesc: '可以为您推荐景点、美食、酒店、线路与出行信息，想问什么就问什么',
	hotTitle: '您可以这样问：',
	typing: '正在思考...'
};

const accentColor = getModuleColor('ai');

const HOT_QUESTIONS = [
	{ id: 'h1', text: '推荐附近的特色美食' },
	{ id: 'h2', text: '有什么旅游线路？' },
	{ id: 'h3', text: '附近酒店推荐' }
];

function createWelcomeMessage() {
	return {
		id: 'welcome',
		role: 'assistant',
		type: 'welcome',
		content: ui.welcomeDesc,
		title: ui.welcomeTitle,
		hotQuestions: HOT_QUESTIONS
	};
}

function createUserMessage(id, content) {
	return { id, role: 'user', type: 'text', content };
}

function createAssistantMessage(id, payload) {
	return {
		id,
		role: 'assistant',
		type: 'text',
		content: payload.content,
		links: payload.links || []
	};
}
const inputText = ref('');
const scrollIntoView = ref('');
const typing = ref(false);
const chatTime = ref('');
const userAvatar = ref(getDefaultAvatar());
const messages = ref([createWelcomeMessage()]);
let msgId = 1;

async function loadUserAvatar() {
	if (!hasToken()) {
		userAvatar.value = getDefaultAvatar();
		return;
	}
	try {
		const res = await getUserInfo();
		if (res.code === 0 && res.data) {
			userAvatar.value = resolveAvatar(res.data.avatar);
			return;
		}
	} catch (e) {
		// ignore
	}
	userAvatar.value = getDefaultAvatar();
}

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

/** 调用 POST /app/ai/chat 获取规则 Mock 回复 */
async function appendAssistantReply(text) {
	const fallback = {
		content: '抱歉，暂时无法回复，请稍后重试。',
		links: []
	};
	try {
		const res = await postAiChat(text);
		if (res.code === 0 && res.data) {
			messages.value.push(createAssistantMessage(`a-${++msgId}`, {
				content: res.data.content || fallback.content,
				links: res.data.links || []
			}));
		} else {
			uni.showToast({ title: res.msg || '回复失败', icon: 'none' });
			messages.value.push(createAssistantMessage(`a-${++msgId}`, fallback));
		}
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
		messages.value.push(createAssistantMessage(`a-${++msgId}`, fallback));
	} finally {
		typing.value = false;
		scrollToBottom(`msg-a-${msgId}`);
	}
}

async function sendText(text) {
	const content = String(text || '').trim();
	if (!content || typing.value) return;

	messages.value.push(createUserMessage(`u-${++msgId}`, content));
	inputText.value = '';
	typing.value = true;
	scrollToBottom(`msg-u-${msgId}`);

	await appendAssistantReply(content);
}

function onSend() {
	sendText(inputText.value);
}

function onHotQuestion(text) {
	sendText(text);
}

function openLink(path) {
	if (!path) return;
	uni.navigateTo({
		url: path,
		fail: () => {
			uni.showToast({ title: '页面暂未开放', icon: 'none' });
		}
	});
}

onLoad(async () => {
	chatTime.value = formatChatTime();
	uni.setNavigationBarTitle({ title: ui.navTitle });
	await loadUserAvatar();
});
</script>

<style lang="scss" scoped>
.ai-chat-page {
	--ai-accent: #ff701f;
	min-height: 100vh;
	background-color: #fff9f3;
}

.ai-chat-scroll {
	height: 100vh;
	box-sizing: border-box;
}

.ai-chat-body {
	padding: 20rpx var(--ry-page-padding-x) calc(240rpx + env(safe-area-inset-bottom));
}

.ai-chat-time {
	display: block;
	text-align: center;
	font-size: 24rpx;
	color: #bbbbbb;
	margin-bottom: 28rpx;
	line-height: 1.4;
}

.ai-chat-row {
	display: flex;
	align-items: flex-start;
	margin-bottom: 28rpx;
	gap: 20rpx;
}

.ai-chat-row--user {
	flex-direction: row-reverse;
}

.ai-chat-avatar {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
}

.ai-chat-avatar--ai {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	background-color: var(--ai-accent);
}

.ai-chat-avatar--user {
	width: 80rpx;
	height: 80rpx;
	border-radius: 16rpx;
	background-color: #e5e5e5;
}

.ai-chat-avatar__img {
	width: 100%;
	height: 100%;
	display: block;
	border-radius: inherit;
}

.ai-chat-bubble {
	max-width: calc(100% - 104rpx);
	padding: 24rpx 28rpx;
	border-radius: 24rpx;
}

.ai-chat-bubble--ai {
	background-color: var(--ry-card-bg);
	border: 1rpx solid #f0f0f0;
	border-top-left-radius: 8rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.03);
}

.ai-chat-bubble--user {
	background-color: var(--ai-accent);
	border-top-right-radius: 8rpx;
	box-shadow: none;
}

.ai-chat-bubble--typing {
	opacity: 0.85;
}

.ai-chat-bubble__text {
	display: block;
	font-size: 28rpx;
	line-height: 1.65;
	word-break: break-word;
}

.ai-chat-bubble__text--title {
	font-size: 30rpx;
	font-weight: 600;
	margin-bottom: 8rpx;
}

.ai-chat-bubble--ai .ai-chat-bubble__text {
	color: var(--ry-text-primary);
}

.ai-chat-bubble--user .ai-chat-bubble__text {
	color: var(--ry-text-inverse);
}

.ai-chat-bubble__text--outro {
	margin-top: 0;
}

.ai-chat-bubble__hot-title {
	display: block;
	margin-top: 16rpx;
	font-size: 28rpx;
	font-weight: 600;
	color: var(--ry-text-primary);
	line-height: 1.5;
}

.ai-chat-bubble__hot-item {
	display: flex;
	align-items: flex-start;
	margin-top: 12rpx;
	padding: 2rpx 0;
	gap: 6rpx;
}

.ai-chat-bubble__hot-num {
	flex-shrink: 0;
	font-size: 28rpx;
	color: var(--ai-accent);
	line-height: 1.65;
}

.ai-chat-bubble__hot-text {
	flex: 1;
	font-size: 28rpx;
	color: var(--ai-accent);
	line-height: 1.65;
	text-decoration: underline;
	text-underline-offset: 4rpx;
}

.ai-chat-bubble__links {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	margin-top: 16rpx;
}

.ai-chat-bubble__link {
	display: inline-flex;
	align-items: center;
	align-self: flex-start;
	gap: 6rpx;
	padding: 10rpx 20rpx;
	background-color: #fff3eb;
	border-radius: var(--ry-radius-pill);
}

.ai-chat-bubble__link-text {
	font-size: 24rpx;
	color: var(--ai-accent);
	line-height: 1.4;
}

.ai-chat-input-wrap {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 100;
	padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
	background-color: var(--ry-card-bg);
	border-top: 1rpx solid #f0f0f0;
}

.ai-chat-input-bar {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.ai-chat-input-bar__field {
	flex: 1;
	display: flex;
	align-items: center;
	height: 72rpx;
	padding: 0 28rpx;
	background-color: #ffffff;
	border: 1rpx solid #e8e8e8;
	border-radius: var(--ry-radius-pill);
	min-width: 0;
}

.ai-chat-input-bar__input {
	flex: 1;
	height: 72rpx;
	font-size: 28rpx;
	color: var(--ry-text-primary);
	min-width: 0;
}

.ai-chat-input-bar__placeholder {
	color: var(--ry-text-hint);
	font-size: 28rpx;
}

.ai-chat-input-bar__send {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	background-color: var(--ai-accent);
	flex-shrink: 0;
}

.ai-chat-input-bar__send--disabled {
	opacity: 0.45;
}

.ai-chat-disclaimer {
	display: block;
	margin-top: 16rpx;
	text-align: center;
	font-size: 22rpx;
	color: #bbbbbb;
	line-height: 1.4;
}
</style>
