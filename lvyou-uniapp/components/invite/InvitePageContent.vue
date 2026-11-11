<template>
	<view class="invite-page">
		<scroll-view scroll-y class="invite-scroll">
			<view class="invite-poster">
				<view class="invite-poster__fallback" />
				<image v-if="config.posterImage" class="invite-poster__img" :src="config.posterImage" mode="aspectFill" />
				<view class="invite-poster__mask" />
				<view class="invite-poster__brand">
					<text class="invite-poster__brand-name">{{ productName }}</text>
					<text class="invite-poster__brand-slogan">{{ brandSlogan }}</text>
				</view>
			</view>

			<view class="invite-card ry-card">
				<image class="invite-card__avatar" :src="resolveAvatar(userInfo.avatar)" mode="aspectFill" />
				<view class="invite-card__info">
					<text class="invite-card__phone">{{ displayPhone }}</text>
					<text class="invite-card__code">{{ ui.inviteCodePrefix }}{{ userInfo.inviteCode || '------' }}</text>
				</view>
				<view class="invite-card__qr">
					<up-qrcode v-if="qrContent" :key="'main-' + qrContent" :val="qrContent" :size="qrSizeMain" />
				</view>
			</view>

			<view class="invite-actions">
				<view class="invite-actions__btn invite-actions__btn--copy" @click="onCopyText">
					<text>{{ ui.copyBtn }}</text>
				</view>
				<view class="invite-actions__btn invite-actions__btn--poster" @click="openPosterPreview">
					<text>{{ ui.posterBtn }}</text>
				</view>
			</view>

			<view class="invite-links">
				<text class="invite-links__item" @click="goTeam">{{ ui.myTeamLink }}</text>
				<text class="invite-links__dot">·</text>
				<text class="invite-links__item" @click="goCommission">{{ ui.commissionLink }}</text>
			</view>
		</scroll-view>

		<view v-if="posterVisible" class="invite-modal" @click.self="closePosterPreview">
			<view class="invite-modal__panel">
				<text class="invite-modal__title">{{ ui.posterPreviewTitle }}</text>
				<view id="poster-export-area" class="invite-modal__poster">
					<view class="invite-modal__poster-fallback" />
					<image v-if="config.posterImage" class="invite-modal__poster-img" :src="config.posterImage"
						mode="aspectFill" />
					<view class="invite-modal__poster-mask" />
					<view class="invite-modal__poster-brand">
						<text class="invite-modal__poster-brand-name">{{ productName }}</text>
						<text class="invite-modal__poster-brand-slogan">{{ brandSlogan }}</text>
					</view>
					<view class="invite-modal__card">
						<image class="invite-modal__avatar" :src="resolveAvatar(userInfo.avatar)" mode="aspectFill" />
						<view class="invite-modal__info">
							<text class="invite-modal__phone">{{ displayPhone }}</text>
							<text class="invite-modal__code">{{ ui.inviteCodePrefix }}{{ userInfo.inviteCode }}</text>
						</view>
						<view class="invite-modal__qr">
							<up-qrcode v-if="qrContent" :key="'modal-' + qrContent" :val="qrContent" :size="qrSizeModal" />
						</view>
					</view>
				</view>
				<view class="invite-modal__footer">
					<view class="invite-modal__btn invite-modal__btn--ghost" @click="closePosterPreview">
						<text>{{ ui.closeBtn }}</text>
					</view>
					<view class="invite-modal__btn invite-modal__btn--primary" @click="onSavePoster">
						<text>{{ ui.savePosterBtn }}</text>
					</view>
				</view>
			</view>
		</view>

		<up-poster ref="posterRef" :json="posterJson" />
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app';
	import {
		getDefaultAvatar,
		resolveAvatar
	} from '@/common/avatar.js';
	import {
		INVITE_UI,
		buildInviteShareUrl,
		formatInviteCopyText
	} from '@/common/invite-ui.js';
	import {
		hasToken
	} from '@/common/api/auth.js';
	import {
		fetchInviteConfig,
		fetchInviteSummary
	} from '@/common/api/invite.js';
	import {
		getProductName,
		loadProductName
	} from '@/common/brand.js';

	const ui = INVITE_UI;
	const productName = ref(getProductName());
	const defaultAvatar = getDefaultAvatar();
	const config = ref({});
	const userInfo = ref({});
	const posterVisible = ref(false);
	const posterRef = ref(null);
	const loading = ref(false);
	const qrSizeMain = 100;
	const qrSizeModal = 88;

	const displayPhone = computed(() => {
		const phone = userInfo.value.phone;
		if (!phone) return '未登录';
		return phone;
	});

	const brandSlogan = computed(() => `${productName.value} · 一站式出行`);

	const qrContent = computed(() => {
		const fromApi = String(userInfo.value.qrContent || '').trim();
		if (fromApi) {
			return fromApi;
		}
		return buildInviteShareUrl(config.value.shareLink, userInfo.value.inviteCode);
	});

	const posterJson = computed(() => ({
		css: {
			width: '750rpx',
			height: '2200rpx',
			background: '#FFFFFF'
		},
		views: [{
				type: 'image',
				src: config.value.posterImage || defaultAvatar,
				css: {
					left: '0rpx',
					top: '0rpx',
					width: '750rpx',
					height: '1520rpx'
				}
			},
			{
				type: 'text',
				text: productName.value,
				css: {
					left: '40rpx',
					top: '60rpx',
					fontSize: '48rpx',
					color: '#FFFFFF',
					fontWeight: 'bold'
				}
			},
			{
				type: 'text',
				text: brandSlogan.value,
				css: {
					left: '40rpx',
					top: '130rpx',
					fontSize: '28rpx',
					color: 'rgba(255,255,255,0.9)'
				}
			},
			{
				type: 'view',
				css: {
					left: '32rpx',
					top: '1400rpx',
					width: '686rpx',
					height: '160rpx',
					background: '#FFFFFF',
					borderRadius: '24rpx'
				}
			},
			{
				type: 'image',
				src: resolveAvatar(userInfo.value.avatar),
				css: {
					left: '56rpx',
					top: '1430rpx',
					width: '100rpx',
					height: '100rpx',
					borderRadius: '50%'
				}
			},
			{
				type: 'text',
				text: displayPhone.value,
				css: {
					left: '180rpx',
					top: '1440rpx',
					fontSize: '32rpx',
					color: '#333333',
					fontWeight: 'bold'
				}
			},
			{
				type: 'text',
				text: `${ui.inviteCodePrefix}${userInfo.value.inviteCode || ''}`,
				css: {
					left: '180rpx',
					top: '1500rpx',
					fontSize: '26rpx',
					color: '#666666'
				}
			},
			{
				type: 'qrcode',
				text: qrContent.value,
				css: {
					left: '560rpx',
					top: '1420rpx',
					width: '120rpx',
					height: '120rpx'
				}
			}
		]
	}));

	function requireLogin() {
		if (hasToken()) return true;
		uni.showToast({
			title: ui.loginTip,
			icon: 'none'
		});
		setTimeout(() => uni.navigateTo({
			url: '/pages/my/login'
		}), 400);
		return false;
	}

	async function reloadData() {
		if (loading.value) return;
		loading.value = true;
		try {
			const [configRes, summaryRes] = await Promise.all([
				fetchInviteConfig(),
				fetchInviteSummary()
			]);
			if (configRes.code === 0 && configRes.data) {
				config.value = configRes.data;
			}
			if (summaryRes.code === 0 && summaryRes.data) {
				userInfo.value = summaryRes.data;
			} else if (summaryRes.code !== 0) {
				uni.showToast({
					title: summaryRes.msg || ui.loadFail,
					icon: 'none'
				});
			}
		} catch (e) {
			uni.showToast({
				title: ui.networkError,
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	async function onCopyText() {
		if (!requireLogin()) return;
		if (!userInfo.value.inviteCode || !qrContent.value) {
			await reloadData();
		}
		const shareUrl = qrContent.value || buildInviteShareUrl(config.value.shareLink, userInfo.value.inviteCode);
		const text = formatInviteCopyText(config.value, userInfo.value.inviteCode, shareUrl);
		if (!text || !shareUrl) {
			uni.showToast({
				title: ui.loadFail,
				icon: 'none'
			});
			return;
		}
		uni.setClipboardData({
			data: text,
			success: () => uni.showToast({
				title: ui.copySuccess,
				icon: 'success'
			}),
			fail: () => uni.showToast({
				title: ui.copyFail,
				icon: 'none'
			})
		});
	}

	function openPosterPreview() {
		if (!requireLogin()) return;
		if (!qrContent.value) {
			uni.showToast({
				title: ui.loadFail,
				icon: 'none'
			});
			return;
		}
		posterVisible.value = true;
	}

	function closePosterPreview() {
		posterVisible.value = false;
	}

	async function onSavePoster() {
		if (!posterRef.value?.exportImage) {
			uni.showToast({
				title: ui.saveFail,
				icon: 'none'
			});
			return;
		}
		try {
			const tempPath = await posterRef.value.exportImage();
			// #ifdef H5
			const link = document.createElement('a');
			link.href = tempPath;
			link.download = 'rongeyou-invite-poster.png';
			link.click();
			uni.showToast({
				title: ui.saveSuccess,
				icon: 'success'
			});
			closePosterPreview();
			return;
			// #endif
			uni.saveImageToPhotosAlbum({
				filePath: tempPath,
				success: () => {
					uni.showToast({
						title: ui.saveSuccess,
						icon: 'success'
					});
					closePosterPreview();
				},
				fail: () => uni.showToast({
					title: ui.saveFail,
					icon: 'none'
				})
			});
		} catch (e) {
			uni.showToast({
				title: ui.saveFail,
				icon: 'none'
			});
		}
	}

	function goTeam() {
		uni.navigateTo({
			url: '/pages/my/team'
		});
	}

	function goCommission() {
		uni.navigateTo({
			url: '/pages/my/commission'
		});
	}

	onLoad(() => {
		uni.setNavigationBarTitle({
			title: ui.inviteTitle
		});
		uni.setNavigationBarColor({
			frontColor: '#000000',
			backgroundColor: '#FFFFFF'
		});
	});

	onShow(async () => {
		productName.value = await loadProductName();
		if (!requireLogin()) return;
		reloadData();
	});
</script>

<style lang="scss" scoped>
	.invite-page {
		min-height: 100vh;
		background-color: var(--ry-page-bg);
	}

	.invite-scroll {
		height: 100vh;
	}

	.invite-poster {
		position: relative;
		width: 100%;
		height: 1040rpx;
		overflow: hidden;

		&__fallback {
			position: absolute;
			inset: 0;
			background: linear-gradient(135deg, #FF6600 0%, #FF8A3D 55%, #FFB366 100%);
		}

		&__img {
			position: relative;
			z-index: 1;
			width: 100%;
			height: 100%;
		}

		&__mask {
			position: absolute;
			inset: 0;
			z-index: 2;
			background: linear-gradient(180deg, rgba(0, 0, 0, 0.15) 0%, rgba(0, 0, 0, 0.35) 100%);
		}

		&__brand {
			position: absolute;
			left: 32rpx;
			bottom: 80rpx;
			z-index: 3;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__brand-name {
			font-size: 44rpx;
			font-weight: 700;
			color: #FFFFFF;
			text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.25);
		}

		&__brand-slogan {
			font-size: 26rpx;
			color: rgba(255, 255, 255, 0.92);
		}
	}

	.invite-card {
		position: relative;
		margin: -48rpx 24rpx 0;
		padding: 28rpx 24rpx;
		display: flex;
		align-items: center;
		gap: 20rpx;
		z-index: 2;

		&__avatar {
			width: 96rpx;
			height: 96rpx;
			border-radius: 50%;
			flex-shrink: 0;
			background-color: #F5F5F5;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__phone {
			font-size: 32rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
		}

		&__code {
			font-size: 24rpx;
			color: var(--ry-text-secondary);
		}

		&__qr {
			flex-shrink: 0;
			width: 60px;
			height: 60px;
			padding: 8rpx;
			background: #FFFFFF;
			border-radius: 12rpx;
			border: 1rpx solid #F0F0F0;
			display: flex;
			align-items: center;
			justify-content: center;
			overflow: hidden;
		}
	}

	.invite-actions {
		display: flex;
		gap: 20rpx;
		padding: 32rpx 24rpx 16rpx;

		&__btn {
			flex: 1;
			height: 88rpx;
			border-radius: var(--ry-radius-pill);
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 30rpx;
			font-weight: 600;
			color: #FFFFFF;

			&:active {
				opacity: 0.88;
			}

			&--copy {
				background: linear-gradient(135deg, #FF6600 0%, #FF8A3D 100%);
			}

			&--poster {
				background: linear-gradient(135deg, #2EAF6A 0%, #45C87E 100%);
			}
		}
	}

	.invite-links {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		padding: 24rpx 0 48rpx;

		&__item {
			font-size: 26rpx;
			color: var(--ry-text-hint);

			&:active {
				color: #FF6600;
			}
		}

		&__dot {
			font-size: 26rpx;
			color: #DDDDDD;
		}
	}

	.invite-modal {
		position: fixed;
		inset: 0;
		z-index: 999;
		background: rgba(0, 0, 0, 0.55);
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 40rpx 32rpx;

		&__panel {
			width: 100%;
			max-width: 640rpx;
			max-height: 90vh;
			overflow-y: auto;
			background: #FFFFFF;
			border-radius: 24rpx;
			padding: 32rpx 28rpx 28rpx;
		}

		&__title {
			display: block;
			text-align: center;
			font-size: 32rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
			margin-bottom: 24rpx;
		}

		&__poster {
			position: relative;
			border-radius: 20rpx;
			overflow: hidden;
			background: #F5F5F5;
		}

		&__poster-fallback {
			position: absolute;
			top: 0;
			left: 0;
			right: 0;
			height: 960rpx;
			background: linear-gradient(135deg, #FF6600 0%, #FF8A3D 55%, #FFB366 100%);
		}

		&__poster-img {
			position: relative;
			z-index: 1;
			width: 100%;
			height: 960rpx;
			display: block;
		}

		&__poster-mask {
			position: absolute;
			top: 0;
			left: 0;
			right: 0;
			z-index: 2;
			height: 960rpx;
			background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.3) 100%);
		}

		&__poster-brand {
			position: absolute;
			left: 24rpx;
			top: 640rpx;
			z-index: 3;
			display: flex;
			flex-direction: column;
			gap: 6rpx;
		}

		&__poster-brand-name {
			font-size: 36rpx;
			font-weight: 700;
			color: #FFFFFF;
		}

		&__poster-brand-slogan {
			font-size: 22rpx;
			color: rgba(255, 255, 255, 0.9);
		}

		&__card {
			display: flex;
			align-items: center;
			gap: 16rpx;
			padding: 24rpx;
			background: #FFFFFF;
		}

		&__avatar {
			width: 80rpx;
			height: 80rpx;
			border-radius: 50%;
			flex-shrink: 0;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 6rpx;
		}

		&__phone {
			font-size: 28rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
		}

		&__code {
			font-size: 22rpx;
			color: var(--ry-text-secondary);
		}

		&__qr {
			flex-shrink: 0;
			width: 104px;
			height: 104px;
			display: flex;
			align-items: center;
			justify-content: center;
			overflow: hidden;
		}

		&__footer {
			display: flex;
			gap: 20rpx;
			margin-top: 28rpx;
		}

		&__btn {
			flex: 1;
			height: 80rpx;
			border-radius: var(--ry-radius-pill);
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 28rpx;
			font-weight: 500;

			&--ghost {
				background: #F5F5F5;
				color: var(--ry-text-secondary);
			}

			&--primary {
				background: #FF6600;
				color: #FFFFFF;
			}
		}
	}
</style>
