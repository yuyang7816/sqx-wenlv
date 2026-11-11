<template>
	<view class="scenic-detail-page">
		<view class="scenic-cover">
			<swiper
				class="scenic-cover__swiper"
				:indicator-dots="bannerList.length > 1"
				indicator-color="rgba(255,255,255,0.45)"
				indicator-active-color="#FFFFFF"
				:autoplay="bannerList.length > 1"
				:circular="true"
				:interval="4000"
				duration="300"
				@change="onBannerChange"
			>
				<swiper-item v-for="(item, index) in bannerList" :key="index" class="scenic-cover__item">
					<image
						class="scenic-cover__img ry-placeholder-img"
						:src="item"
						mode="aspectFill"
						@click="previewBannerImages(index)"
					/>
				</swiper-item>
			</swiper>
			<view class="scenic-cover__mask" />
			<view v-if="scenic.levelLabel" class="scenic-cover__level">
				<text class="scenic-cover__level-text">{{ scenic.levelLabel }}</text>
			</view>
			<view class="scenic-cover__favorite" @click="onFavorite">
				<u-icon
					:name="favorited ? 'heart-fill' : 'heart'"
					:color="favorited ? '#FF4757' : '#FFFFFF'"
					:size="22"
				/>
			</view>
			<view v-if="bannerList.length > 1" class="scenic-cover__count">
				<text class="scenic-cover__count-text">{{ bannerCurrent + 1 }}/{{ bannerList.length }}</text>
			</view>
		</view>

		<view class="scenic-detail-body">
			<view class="scenic-info ry-card">
				<view class="scenic-info__head">
					<text class="scenic-info__name">{{ scenic.name }}</text>
					<view class="scenic-info__price">
						<text
							class="scenic-info__price-value"
							:class="{ 'scenic-info__price-value--paid': !scenic.ticketFree }"
						>{{ scenic.ticketPrice }}</text>
						<text class="scenic-info__price-label">门票</text>
					</view>
				</view>

				<view v-if="scenic.tags && scenic.tags.length" class="scenic-info__tags">
					<view
						v-for="tag in scenic.tags"
						:key="tag.id"
						class="scenic-info__tag"
						:class="`scenic-info__tag--${tag.type}`"
					>
						<text class="scenic-info__tag-text">{{ tag.label }}</text>
					</view>
				</view>

				<view class="scenic-info__addr" @click="onNavigate">
					<view class="scenic-info__addr-icon">
						<u-icon name="map-fill" :color="accentColor" :size="14" />
					</view>
					<text class="scenic-info__addr-text">{{ scenic.address }}</text>
					<u-icon name="arrow-right" :size="12" color="#CCCCCC" />
				</view>

				<view class="scenic-info__meta">
					<view class="scenic-info__meta-item">
						<u-icon name="clock-fill" :color="accentColor" :size="15" />
						<text class="scenic-info__meta-label">开放时间</text>
						<text class="scenic-info__meta-value">{{ scenic.openTime }}</text>
					</view>
					<view class="scenic-info__meta-divider" />
					<view class="scenic-info__meta-item scenic-info__meta-item--phone" @click="onPhoneCall">
						<u-icon name="phone-fill" :color="accentColor" :size="15" />
						<text class="scenic-info__meta-label">咨询电话</text>
						<text class="scenic-info__meta-value scenic-info__meta-value--link">{{ scenic.phone }}</text>
					</view>
				</view>
			</view>

			<view class="scenic-section ry-card">
				<view class="scenic-section__head">
					<view class="scenic-section__icon">
						<u-icon name="star-fill" :color="accentColor" :size="16" />
					</view>
					<text class="scenic-section__title">景区特色</text>
				</view>
				<text class="scenic-section__desc">{{ scenic.featureDesc }}</text>
				<view v-if="featureImageList.length" class="scenic-section__imgs">
					<image
						v-for="(img, index) in featureImageList"
						:key="`${img}-${index}`"
						class="scenic-section__img ry-placeholder-img"
						:src="resolveImage(img)"
						mode="aspectFill"
						@click="previewFeatureImages(index)"
					/>
				</view>
			</view>

			<view v-if="scenic.tips && scenic.tips.length" class="scenic-section ry-card">
				<view class="scenic-section__head">
					<view class="scenic-section__icon">
						<u-icon name="info-circle-fill" :color="accentColor" :size="16" />
					</view>
					<text class="scenic-section__title">游玩提示</text>
				</view>
				<view class="scenic-tips">
					<view v-for="(tip, index) in scenic.tips" :key="index" class="scenic-tips__item">
						<view class="scenic-tips__dot" />
						<text class="scenic-tips__text">{{ tip }}</text>
					</view>
				</view>
			</view>

			<text class="scenic-footer">智慧文旅 · 舒心出行</text>
		</view>

		<view class="scenic-bar">
			<view class="scenic-bar__phone" @click="onPhoneCall">
				<u-icon name="phone-fill" :color="accentColor" :size="22" />
				<text class="scenic-bar__phone-text">电话咨询</text>
			</view>
			<view class="scenic-bar__navigate" @click="onNavigate">
				<u-icon name="map-fill" color="#FFFFFF" :size="16" />
				<text class="scenic-bar__navigate-text">导航前往</text>
				<u-icon name="arrow-right" color="#FFFFFF" :size="14" />
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue';
	import { onLoad, onShow } from '@dcloudio/uni-app';
	import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
	import { checkFavorited, toggleFavoriteItem, logFootprint } from '@/common/asset-detail.js';
	import { fetchScenicDetail } from '@/common/api/scenic.js';

	const accentColor = getModuleColor('scenic');

	const emptyScenic = () => ({
		id: '',
		name: '',
		ticketPrice: '',
		ticketFree: true,
		levelLabel: '',
		address: '',
		latitude: 0,
		longitude: 0,
		phone: '',
		openTime: '',
		bannerImage: '',
		bannerImages: [],
		featureImage: '',
		featureImages: [],
		tags: [],
		tips: [],
		featureDesc: ''
	});

	const scenicId = ref('');
	const scenic = ref(emptyScenic());
	const favorited = ref(false);
	const bannerCurrent = ref(0);
	const pageLoading = ref(false);

	const bannerList = computed(() => {
		const images = (scenic.value.bannerImages || []).filter(Boolean);
		if (images.length) return images;
		if (scenic.value.bannerImage) return [scenic.value.bannerImage];
		if (scenic.value.featureImage) return [scenic.value.featureImage];
		return [PLACEHOLDER_LOGO];
	});

	const featureImageList = computed(() => {
		const images = (scenic.value.featureImages || []).filter(Boolean);
		if (images.length) return images;
		if (scenic.value.featureImage) return [scenic.value.featureImage];
		return [];
	});

	const favoriteItem = () => ({
		moduleType: 'scenic',
		id: scenic.value.id,
		name: scenic.value.name,
		address: scenic.value.address,
		path: `/mobile-design/scenic/detail?id=${scenic.value.id}`,
		cover: scenic.value.bannerImage || scenic.value.bannerImages?.[0] || scenic.value.featureImages?.[0] || scenic.value.featureImage || '',
		desc: scenic.value.featureDesc || ''
	});

	const resolveImage = (url) => url || PLACEHOLDER_LOGO;

	function mapDetailData(data) {
		return {
			id: data.id,
			name: data.name || '',
			ticketPrice: data.ticketPrice || '',
			ticketFree: !!data.ticketFree,
			levelLabel: data.levelLabel || '',
			address: data.address || '',
			latitude: data.latitude != null ? Number(data.latitude) : 0,
			longitude: data.longitude != null ? Number(data.longitude) : 0,
			phone: data.phone || '',
			openTime: data.openTime || '',
			bannerImage: data.bannerImage || data.coverImage || data.cover || '',
			bannerImages: data.bannerImages || [],
			featureImage: data.featureImage || (Array.isArray(data.featureImages) ? data.featureImages[0] : '') || '',
			featureImages: (data.featureImages || []).filter(Boolean),
			tags: (data.tags || []).map((tag, index) => ({
				id: tag.id ?? index,
				type: tag.type || 'green',
				label: tag.label || tag
			})),
			tips: data.tips || [],
			featureDesc: data.featureDesc || ''
		};
	}

	/** onShow 拉取景区详情（公开接口） */
	async function loadScenicData() {
		if (!scenicId.value || pageLoading.value) {
			return;
		}
		pageLoading.value = true;
		try {
			const res = await fetchScenicDetail(scenicId.value);
			if (res.code !== 0 || !res.data) {
				uni.showToast({ title: res.msg || '景区详情加载失败', icon: 'none' });
				return;
			}
			scenic.value = mapDetailData(res.data);
			favorited.value = await checkFavorited('scenic', scenic.value.id);
			logFootprint();
		} catch (e) {
			uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
		} finally {
			pageLoading.value = false;
		}
	}

	onLoad((options) => {
		scenicId.value = options?.id ? String(options.id) : '';
		if (options?.name) {
			uni.setNavigationBarTitle({ title: decodeURIComponent(options.name) });
		}
	});

	onShow(() => {
		if (scenicId.value) {
			loadScenicData();
		}
	});

	function onBannerChange(e) {
		bannerCurrent.value = e.detail.current;
	}

	function previewBannerImages(index) {
		const urls = bannerList.value.map((url) => resolveImage(url)).filter(Boolean);
		if (!urls.length) return;
		uni.previewImage({
			current: urls[index] || urls[0],
			urls
		});
	}

	function previewFeatureImages(index) {
		const urls = featureImageList.value.map((url) => resolveImage(url)).filter(Boolean);
		if (!urls.length) return;
		uni.previewImage({
			current: urls[index] || urls[0],
			urls
		});
	}

	async function onFavorite() {
		const next = await toggleFavoriteItem(favoriteItem());
		if (next == null) return;
		favorited.value = next;
		uni.showToast({
			title: favorited.value ? '已收藏' : '已取消收藏',
			icon: 'none'
		});
	}

	function onPhoneCall() {
		const phoneNumber = String(scenic.value.phone || '').trim();
		if (!phoneNumber) {
			uni.showToast({ title: '暂无电话', icon: 'none' });
			return;
		}
		// #ifdef H5
		window.location.href = `tel:${phoneNumber}`;
		return;
		// #endif
		uni.makePhoneCall({
			phoneNumber,
			fail: () => {
				uni.showToast({ title: `请拨打：${phoneNumber}`, icon: 'none' });
			}
		});
	}

	function onNavigate() {
		const { latitude, longitude, address, name } = scenic.value;
		if (!latitude || !longitude) {
			uni.showToast({ title: address, icon: 'none' });
			return;
		}
		uni.openLocation({
			latitude,
			longitude,
			name,
			address,
			fail: () => {
				uni.showToast({ title: address, icon: 'none' });
			}
		});
	}
</script>

<style lang="scss" scoped>
	.scenic-detail-page {
		min-height: 100vh;
		background-color: #F0FAF4;
		padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
	}

	.scenic-cover {
		position: relative;
		width: 100%;
		height: 420rpx;
		overflow: hidden;
		background-color: #E8F8EF;

		&__swiper {
			width: 100%;
			height: 420rpx;
		}

		&__item {
			width: 100%;
			height: 420rpx;
			overflow: hidden;
		}

		&__img {
			width: 100%;
			height: 420rpx;
			display: block;
			transform: scale(2.2);
			transform-origin: center center;
		}

		&__mask {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			height: 140rpx;
			background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.32));
			pointer-events: none;
			z-index: 2;
		}

		&__level {
			position: absolute;
			top: 20rpx;
			left: 24rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 44rpx;
			padding: 0 22rpx;
			box-sizing: border-box;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(46, 175, 106, 0.92);
			z-index: 3;
		}

		&__level-text {
			font-size: 22rpx;
			font-weight: 700;
			line-height: 1;
			color: #FFFFFF;
		}

		&__favorite {
			position: absolute;
			top: 20rpx;
			right: 24rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 72rpx;
			height: 72rpx;
			border-radius: 50%;
			background-color: rgba(0, 0, 0, 0.35);
			z-index: 3;

			&:active {
				opacity: 0.88;
			}
		}

		&__count {
			position: absolute;
			right: 24rpx;
			bottom: 20rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 40rpx;
			padding: 0 16rpx;
			box-sizing: border-box;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(0, 0, 0, 0.38);
			z-index: 3;
		}

		&__count-text {
			font-size: 20rpx;
			line-height: 1;
			color: #FFFFFF;
		}
	}

	.scenic-detail-body {
		padding: 0 var(--ry-page-padding-x) calc(24rpx + env(safe-area-inset-bottom));
		margin-top: -48rpx;
		position: relative;
		z-index: 4;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.scenic-info {
		padding: 28rpx 28rpx 24rpx;
		border-radius: 28rpx;
		box-shadow: 0 8rpx 28rpx rgba(46, 175, 106, 0.1);

		&__head {
			display: flex;
			align-items: flex-start;
			justify-content: space-between;
			gap: 20rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 36rpx;
			font-weight: 700;
			line-height: 1.35;
			color: #1A1A1A;
		}

		&__price {
			flex-shrink: 0;
			display: flex;
			flex-direction: column;
			align-items: flex-end;
			gap: 4rpx;
		}

		&__price-value {
			font-size: 34rpx;
			font-weight: 700;
			line-height: 1.1;
			color: #2EAF6A;
		}

		&__price-value--paid {
			color: #FF6600;
		}

		&__price-label {
			font-size: 20rpx;
			line-height: 1;
			color: #AAAAAA;
		}

		&__tags {
			display: flex;
			flex-wrap: wrap;
			gap: 12rpx;
			margin-top: 20rpx;
		}

		&__tag {
			display: flex;
			align-items: center;
			justify-content: center;
			height: 40rpx;
			padding: 0 18rpx;
			box-sizing: border-box;
			border-radius: var(--ry-radius-pill);

			&--green {
				background-color: #E8F8EF;
			}

			&--green .scenic-info__tag-text {
				color: #2EAF6A;
			}

			&--orange {
				background-color: #FFF3E8;
			}

			&--orange .scenic-info__tag-text {
				color: #B86A2A;
			}
		}

		&__tag-text {
			font-size: 20rpx;
			font-weight: 500;
			line-height: 1;
		}

		&__addr {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-top: 20rpx;
			padding: 16rpx 18rpx;
			border-radius: 16rpx;
			background-color: #F7FCF9;
		}

		&__addr-icon {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__addr-text {
			flex: 1;
			min-width: 0;
			font-size: 24rpx;
			line-height: 1.5;
			color: #666666;
		}

		&__meta {
			display: flex;
			align-items: stretch;
			margin-top: 20rpx;
			padding-top: 20rpx;
			border-top: 1rpx solid #F0F0F0;
		}

		&__meta-item {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 8rpx;
		}

		&__meta-label {
			font-size: 20rpx;
			line-height: 1;
			color: #AAAAAA;
		}

		&__meta-value {
			font-size: 22rpx;
			font-weight: 600;
			line-height: 1.3;
			color: #333333;
			text-align: center;
		}

		&__meta-value--link {
			color: #2EAF6A;
		}

		&__meta-item--phone:active {
			opacity: 0.8;
		}

		&__meta-divider {
			width: 1rpx;
			background-color: #F0F0F0;
			margin: 4rpx 0;
		}
	}

	.scenic-section {
		padding: 28rpx;
		border-radius: 28rpx;
		box-shadow: 0 8rpx 28rpx rgba(46, 175, 106, 0.08);

		&__head {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 20rpx;
		}

		&__icon {
			display: flex;
			align-items: center;
			justify-content: center;
			width: 44rpx;
			height: 44rpx;
			border-radius: 12rpx;
			background-color: #E8F8EF;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 700;
			line-height: 1.4;
			color: #1A1A1A;
		}

		&__desc {
			display: block;
			font-size: 26rpx;
			line-height: 1.75;
			color: #666666;
			text-align: justify;
		}

		&__imgs {
			display: flex;
			flex-wrap: wrap;
			gap: 16rpx;
			margin-top: 24rpx;
		}

		&__img {
			width: calc(50% - 8rpx);
			height: 220rpx;
			border-radius: 16rpx;
			display: block;
			background-color: #E8F8EF;
		}
	}

	.scenic-tips {
		display: flex;
		flex-direction: column;
		gap: 16rpx;

		&__item {
			display: flex;
			align-items: flex-start;
			gap: 12rpx;
		}

		&__dot {
			flex-shrink: 0;
			width: 10rpx;
			height: 10rpx;
			margin-top: 12rpx;
			border-radius: 50%;
			background-color: #2EAF6A;
		}

		&__text {
			flex: 1;
			font-size: 24rpx;
			line-height: 1.6;
			color: #666666;
		}
	}

	.scenic-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding: 8rpx 0 4rpx;
	}

	.scenic-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
		background-color: #FFFFFF;
		box-shadow: 0 -4rpx 20rpx rgba(46, 175, 106, 0.08);

		&__phone {
			flex-shrink: 0;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			width: 120rpx;
			gap: 4rpx;
		}

		&__phone-text {
			font-size: 20rpx;
			line-height: 1;
			color: #666666;
		}

		&__navigate {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			height: 88rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #5FD68A, #2EAF6A);
			box-shadow: 0 6rpx 20rpx rgba(46, 175, 106, 0.3);

			&:active {
				opacity: 0.88;
			}
		}

		&__navigate-text {
			font-size: 30rpx;
			font-weight: 700;
			line-height: 1;
			color: #FFFFFF;
		}
	}
</style>
