<template>
	<view class="service-page ry-page">
		<view class="service-body">
			<view class="service-hero ry-card">
				<view class="service-hero__icon">
					<u-icon name="kefu-ermai" color="#FF6600" :size="44" />
				</view>
				<text class="service-hero__title">{{ serviceTitle }}</text>
				<text class="service-hero__desc">{{ ui.desc }}</text>
			</view>

			<view v-if="pageLoading" class="service-loading">
				<u-loading-icon color="#FF6600" />
				<text class="service-loading__text">加载中...</text>
			</view>

			<view v-else-if="displayMode === 'wechat'" class="service-wechat">
				<view v-if="!kefuConfig.wechat && !kefuConfig.wechatQrcode" class="service-empty ry-card">
					<u-empty mode="list" text="暂无微信客服配置" />
				</view>
				<view v-if="kefuConfig.wechat" class="service-wechat-id ry-card">
					<text class="service-wechat-id__label">{{ ui.wechatIdLabel }}</text>
					<view class="service-wechat-id__row">
						<text class="service-wechat-id__value">{{ kefuConfig.wechat }}</text>
						<view class="service-wechat-id__btn" @click="copyWechat">
							<text class="service-wechat-id__btn-text">{{ ui.copyWechat }}</text>
						</view>
					</view>
					<text class="service-wechat-id__tip">{{ ui.wechatIdTip }}</text>
				</view>

				<view v-if="kefuConfig.wechatQrcode" class="service-qrcode ry-card">
					<text class="service-qrcode__title">{{ ui.wechatQrTitle }}</text>
					<image
						class="service-qrcode__img"
						:src="kefuConfig.wechatQrcode"
						mode="aspectFit"
						show-menu-by-longpress
						@click="previewWechatQr"
					/>
					<text class="service-qrcode__tip">{{ ui.wechatQrTip }}</text>
				</view>
			</view>

			<view v-else-if="displayMode === 'phone'" class="service-phone ry-card">
				<u-icon name="phone-fill" color="#2EAF6A" :size="48" />
				<text class="service-phone__title">{{ ui.phoneCallingTitle }}</text>
				<text class="service-phone__number">{{ kefuConfig.phone }}</text>
				<view class="service-phone__btn" @click="onCall">
					<text class="service-phone__btn-text">{{ ui.phoneRetry }}</text>
				</view>
			</view>

			<view v-else-if="displayMode === 'redirecting'" class="service-loading">
				<u-loading-icon color="#2979FF" />
				<text class="service-loading__text">{{ redirectHint }}</text>
			</view>

			<view v-else class="service-empty ry-card">
				<u-empty mode="list" text="暂无客服配置，请稍后再试" />
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchConfigInfo } from '@/common/api/config.js';
import { getProductName } from '@/common/brand.js';
import {
	callKefuPhone,
	copyKefuWechat,
	normalizeKefuConfig,
	openKefuIm,
	openKefuWework,
	resolveKefuType
} from '@/common/kefu-action.js';

const productName = ref(getProductName());
const serviceTitle = computed(() => `${productName.value}客服`);
const ui = {
	desc: '服务时间 09:00 - 21:00，节假日照常服务',
	wechatIdLabel: '客服微信号',
	wechatIdTip: '复制微信号后，在微信中添加好友',
	copyWechat: '复制微信号',
	wechatQrTitle: '微信客服二维码',
	wechatQrTip: '长按识别二维码，添加客服微信',
	phoneCallingTitle: '正在为您接通客服电话',
	phoneRetry: '重新拨打',
	redirectingIm: '正在进入在线客服...',
	redirectingWework: '正在打开企业微信客服...'
};

const pageLoading = ref(true);
/** loading | wechat | phone | redirecting | empty */
const displayMode = ref('loading');
const redirectHint = ref('');
const kefuConfig = ref({
	type: '',
	phone: '',
	wechat: '',
	wechatQrcode: '',
	weworkUrl: '',
	weworkCorpId: ''
});

function applyKefuAction(type) {
	if (type === 'phone') {
		displayMode.value = 'phone';
		callKefuPhone(kefuConfig.value.phone);
		return;
	}
	if (type === 'im') {
		redirectHint.value = ui.redirectingIm;
		displayMode.value = 'redirecting';
		if (!openKefuIm({ replace: true })) {
			displayMode.value = 'empty';
		}
		return;
	}
	if (type === 'wework') {
		redirectHint.value = ui.redirectingWework;
		displayMode.value = 'redirecting';
		openKefuWework(kefuConfig.value);
		return;
	}
	if (type === 'wechat') {
		displayMode.value = 'wechat';
		return;
	}
	displayMode.value = 'empty';
}

async function loadConfigInfo() {
	pageLoading.value = true;
	try {
		const res = await fetchConfigInfo();
		if (res.code === 0 && res.data) {
			if (res.data.productName) {
				productName.value = res.data.productName;
			}
			kefuConfig.value = normalizeKefuConfig(res.data.kefu);
			applyKefuAction(resolveKefuType(kefuConfig.value));
		} else {
			displayMode.value = 'empty';
			uni.showToast({ title: res.msg || '客服配置加载失败', icon: 'none' });
		}
	} catch (e) {
		displayMode.value = 'empty';
		uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
}

function onCall() {
	callKefuPhone(kefuConfig.value.phone);
}

function copyWechat() {
	copyKefuWechat(kefuConfig.value.wechat);
}

function previewWechatQr() {
	if (!kefuConfig.value.wechatQrcode) {
		return;
	}
	uni.previewImage({
		urls: [kefuConfig.value.wechatQrcode],
		current: kefuConfig.value.wechatQrcode
	});
}

onLoad(() => {
	loadConfigInfo();
});
</script>

<style lang="scss" scoped>
.service-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
}

.service-body {
	padding: 24rpx var(--ry-page-padding-x);
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.service-hero {
	padding: 40rpx 32rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16rpx;
	background: linear-gradient(135deg, #fff6ee, #fff);
}

.service-hero__icon {
	width: 96rpx;
	height: 96rpx;
	border-radius: 28rpx;
	background-color: #fff3eb;
	display: flex;
	align-items: center;
	justify-content: center;
}

.service-hero__title {
	font-size: 34rpx;
	font-weight: 600;
	color: #333;
}

.service-hero__desc {
	font-size: 24rpx;
	color: #999;
	text-align: center;
}

.service-wechat {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.service-wechat-id {
	padding: 32rpx;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.service-wechat-id__label {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
}

.service-wechat-id__row {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.service-wechat-id__value {
	flex: 1;
	min-width: 0;
	font-size: 32rpx;
	font-weight: 600;
	color: #07c160;
	word-break: break-all;
}

.service-wechat-id__btn {
	flex-shrink: 0;
	padding: 12rpx 24rpx;
	border-radius: 999rpx;
	background: rgba(7, 193, 96, 0.12);

	&:active {
		opacity: 0.85;
	}
}

.service-wechat-id__btn-text {
	font-size: 24rpx;
	font-weight: 600;
	color: #07c160;
}

.service-wechat-id__tip {
	font-size: 24rpx;
	color: #999;
	line-height: 1.5;
}

.service-qrcode {
	padding: 32rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16rpx;
}

.service-qrcode__title {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
}

.service-qrcode__img {
	width: 360rpx;
	height: 360rpx;
	border-radius: 16rpx;
	background: #f8f8f8;
}

.service-qrcode__tip {
	font-size: 24rpx;
	color: #999;
	text-align: center;
	line-height: 1.5;
}

.service-phone {
	padding: 48rpx 32rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 20rpx;
}

.service-phone__title {
	font-size: 28rpx;
	color: #666;
}

.service-phone__number {
	font-size: 40rpx;
	font-weight: 700;
	color: #2eaf6a;
	letter-spacing: 2rpx;
}

.service-phone__btn {
	margin-top: 8rpx;
	padding: 16rpx 48rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #5fd68a, #2eaf6a);

	&:active {
		opacity: 0.88;
	}
}

.service-phone__btn-text {
	font-size: 28rpx;
	font-weight: 600;
	color: #fff;
}

.service-loading {
	padding: 48rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 16rpx;
}

.service-loading__text {
	font-size: 26rpx;
	color: #999;
}

.service-empty {
	padding: 48rpx 24rpx;
}
</style>
