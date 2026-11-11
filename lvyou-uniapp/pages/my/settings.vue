<template>
	<view class="settings-page ry-page">
		<view class="settings-body">
			<view class="settings-group ry-card">
				<view class="settings-row" @click="onHelp">
					<text class="settings-row__label">帮助中心</text>
					<view class="settings-row__value"><u-icon name="arrow-right" color="#CCCCCC" :size="14" /></view>
				</view>
				<view
					v-for="item in agreementList"
					:key="item.key"
					class="settings-row"
					@click="onAgreement(item.key)"
				>
					<text class="settings-row__label">{{ item.title }}</text>
					<view class="settings-row__value"><u-icon name="arrow-right" color="#CCCCCC" :size="14" /></view>
				</view>
				<view class="settings-row" @click="onAbout">
					<text class="settings-row__label">关于{{ productName }}</text>
					<view class="settings-row__value"><text class="settings-row__text">版本 1.0.0</text><u-icon name="arrow-right" color="#CCCCCC" :size="14" /></view>
				</view>
				<view v-if="loggedIn" class="settings-row settings-row--danger" @click="onCancelAccount">
					<text class="settings-row__label settings-row__label--danger">注销账号</text>
					<view class="settings-row__value"><u-icon name="arrow-right" color="#CCCCCC" :size="14" /></view>
				</view>
			</view>
			<u-button v-if="loggedIn" text="退出登录" block plain color="#FF6600" :custom-style="logoutStyle" @click="onLogout" />
			<u-button v-else text="去登录" block color="#FF6600" :custom-style="logoutStyle" @click="onLogin" />
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { BRAND_COLOR } from '@/common/theme.js';
import { getProductName, loadProductName } from '@/common/brand.js';
import { AGREEMENT_LIST, openAgreement } from '@/common/agreement.js';
import { hasToken, clearLoginSession } from '@/common/api/auth.js';
import { submitCancelAccount } from '@/common/api/user.js';
import { afterLogout, goLogin, openPage, PAGES } from '@/common/navigation.js';

const agreementList = AGREEMENT_LIST;
const productName = ref(getProductName());
const loggedIn = ref(hasToken());
const logoutStyle = { height: '44px', borderRadius: '22px', marginTop: '40rpx' };

onShow(async () => {
	loggedIn.value = hasToken();
	productName.value = await loadProductName();
});
function onHelp() { openPage(PAGES.HELP); }
function onAgreement(key) { openAgreement(key); }
function onAbout() {
	const name = productName.value || getProductName();
	uni.showModal({ title: `关于${name}`, content: `${name}是面向多地区的文旅信息与服务平台，提供景区、酒店、特色美食、活动预约与出行服务。\n\n版本：1.0.0`, showCancel: false, confirmText: '知道了', confirmColor: BRAND_COLOR });
}
function onLogout() {
	uni.showModal({
		title: '退出登录',
		content: '确定要退出当前账号吗？',
		confirmColor: '#FF6600',
		success: (res) => {
			if (!res.confirm) return;
			clearLoginSession();
			loggedIn.value = false;
			uni.showToast({ title: '已退出登录', icon: 'none' });
			setTimeout(() => afterLogout(), 500);
		}
	});
}
function onLogin() { goLogin(); }
function onCancelAccount() {
	uni.showModal({
		title: '注销账号',
		content: '提交后将由平台审核，通过后账号数据将被清除且无法登录。确定继续吗？',
		confirmColor: '#FF6600',
		success: async (res) => {
			if (!res.confirm) return;
			try {
				const result = await submitCancelAccount('');
				if (result && result.code === 0) {
					clearLoginSession();
					loggedIn.value = false;
					uni.showToast({ title: '注销申请已提交', icon: 'none' });
					setTimeout(() => afterLogout(), 800);
					return;
				}
				uni.showToast({ title: (result && result.msg) || '提交失败', icon: 'none' });
			} catch (e) {
				uni.showToast({ title: '提交失败', icon: 'none' });
			}
		}
	});
}
</script>

<style lang="scss" scoped>
	.settings-page {
		min-height: 100vh;
		background-color: var(--ry-page-bg);
	}

	.settings-body {
		padding: 24rpx var(--ry-page-padding-x) 48rpx;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.settings-group {
		padding: 0 28rpx;
	}

	.settings-row {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: space-between;
		padding: 28rpx 0;
		border-bottom: 1rpx solid #f4f4f4;
	}

	.settings-row:last-child {
		border-bottom: none;
	}

	.settings-row__label {
		font-size: 28rpx;
		color: #333;
	}

	.settings-row__value {
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 12rpx;
	}

	.settings-row__text {
		font-size: 26rpx;
		color: #999;
	}

	.settings-row--danger {
		margin-top: 8rpx;
	}

	.settings-row__label--danger {
		color: #e54d42;
	}
</style>
