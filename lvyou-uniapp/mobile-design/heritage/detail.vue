<template>
	<view class="heritage-detail-page">
		<view class="heritage-detail-body">
			<view class="heritage-detail-info ry-card">
				<text class="heritage-detail-info__name">{{ detail.name }}</text>
				<text class="heritage-detail-info__desc">{{ detail.desc }}</text>
			</view>
			<u-button text="了解更多" color="#FF6600" @click="goIntro" />
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { fetchCraftDetail, mapCraftItem } from '@/common/api/heritage.js';

const craftId = ref('');
const detail = ref({ name: '非遗文化', desc: '千年传承，活态保护' });
const loading = ref(false);

async function loadDetail() {
	if (!craftId.value || loading.value) {
		return;
	}
	loading.value = true;
	try {
		const res = await fetchCraftDetail(craftId.value);
		if (res.code === 0 && res.data) {
			const craft = mapCraftItem(res.data);
			detail.value = { name: craft.name, desc: craft.overview || craft.desc };
			uni.setNavigationBarTitle({ title: craft.name });
		} else {
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

onLoad((options) => {
	craftId.value = options?.id || '';
});

onShow(async () => {
	if (!craftId.value) {
		return;
	}
	await loadDetail();
});

function goIntro() {
	if (!craftId.value) return;
	uni.navigateTo({ url: `/mobile-design/heritage/craft-intro?id=${craftId.value}` });
}
</script>

<style lang="scss" scoped>
	.heritage-detail-page {
		min-height: 100vh;
		background-color: #FAF6F3;
	}

	/* �?bn� �?*/
	.detail-cover {
		position: relative;
		width: 100%;
		height: 520rpx;
		background-color: #F5F0EB;

		&__swiper {
			width: 100%;
			height: 520rpx;
		}

		&__item {
			width: 100%;
			height: 520rpx;
		}

		&__img {
			width: 100%;
			height: 520rpx;
			display: block;
		}

		&__mask {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			height: 120rpx;
			background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.3));
			pointer-events: none;
		}

		&__count {
			position: absolute;
			right: 24rpx;
			bottom: 72rpx;
			padding: 4rpx 16rpx;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(0, 0, 0, 0.35);
			z-index: 2;
		}

		&__count-text {
			font-size: 20rpx;
			color: rgba(255, 255, 255, 0.9);
		}
	}

	.heritage-detail-body {
		position: relative;
		margin-top: -48rpx;
		padding: 0 var(--ry-page-padding-x) calc(160rpx + env(safe-area-inset-bottom));
		z-index: 3;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	/* �?���o �?*/
	.detail-info {
		padding: 32rpx 28rpx 28rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		box-shadow: 0 12rpx 40rpx rgba(74, 55, 40, 0.12);

		&__head {
			display: flex;
			align-items: flex-start;
			gap: 12rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 36rpx;
			font-weight: 800;
			color: #4A3728;
			line-height: 1.4;
			letter-spacing: 2rpx;
		}

		&__level {
			flex-shrink: 0;
			padding: 6rpx 16rpx;
			border-radius: var(--ry-radius-pill);

			&--national {
				background-color: #EFEBE9;
			}

			&--municipal {
				background-color: #F5F0EB;
			}

			&--craft {
				background-color: #FBE9E7;
			}
		}

		&__level--national &__level-text {
			color: #5D4037;
		}

		&__level--municipal &__level-text {
			color: #6D4C41;
		}

		&__level--craft &__level-text {
			color: #B45D2D;
		}

		&__level-text {
			font-size: 20rpx;
			font-weight: 600;
		}

		&__tags {
			display: flex;
			flex-wrap: wrap;
			gap: 12rpx;
			margin-top: 16rpx;
		}

		&__tag {
			padding: 6rpx 16rpx;
			border-radius: var(--ry-radius-pill);
			border: 1rpx solid #F0EBE6;
			background-color: #FAF6F3;
		}

		&__tag-text {
			font-size: 22rpx;
			color: #8D6E63;
		}

		&__meta {
			margin-top: 24rpx;
			padding-top: 24rpx;
			border-top: 1rpx solid #F0EBE6;
			display: flex;
			flex-direction: column;
			gap: 16rpx;
		}

		&__meta-item {
			display: flex;
			align-items: flex-start;
			gap: 12rpx;
		}

		&__meta-icon {
			width: 40rpx;
			height: 40rpx;
			border-radius: 50%;
			background-color: #F5F0EB;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__meta-text {
			flex: 1;
			font-size: 24rpx;
			color: #666666;
			line-height: 1.6;
			padding-top: 4rpx;
		}
	}

	/* �?��:W �?*/
	.detail-section {
		padding: 24rpx 28rpx 28rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-bottom: 20rpx;
		}

		&__title-wrap {
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__deco {
			width: 6rpx;
			height: 28rpx;
			border-radius: 3rpx;
			background: linear-gradient(180deg, #B45D2D, #5D4037);
		}

		&__title {
			display: flex;
			align-items: center;
			font-size: 30rpx;
			font-weight: 700;
		}

		&__title-text {
			color: #4A3728;
			letter-spacing: 2rpx;
		}

		&__quote {
			color: #B45D2D;
			font-weight: 700;
		}

		&__more {
			display: flex;
			align-items: center;
			gap: 4rpx;
		}

		&__more-text {
			font-size: 24rpx;
			color: #8D6E63;
		}

		&__content {
			display: block;
			font-size: 26rpx;
			color: #666666;
			line-height: 1.9;
			text-align: justify;
		}
	}

	/* �?yrF� �?*/
	.detail-goods {
		width: 100%;
		white-space: nowrap;
	}

	.detail-goods__row {
		display: inline-flex;
		gap: 16rpx;
		padding: 4rpx;
	}

	.detail-goods__item {
		display: inline-flex;
		flex-direction: column;
		width: 200rpx;

		&:active {
			opacity: 0.88;
		}
	}

	.detail-goods__cover {
		width: 200rpx;
		height: 200rpx;
		border-radius: var(--ry-radius-md);
		overflow: hidden;
		background-color: #F5F0EB;
	}

	.detail-goods__img {
		width: 200rpx;
		height: 200rpx;
		display: block;
	}

	.detail-goods__name {
		margin-top: 12rpx;
		font-size: 24rpx;
		font-weight: 600;
		color: #4A3728;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.detail-goods__price {
		display: flex;
		align-items: baseline;
		margin-top: 6rpx;
	}

	.detail-goods__price-symbol {
		font-size: 20rpx;
		font-weight: 600;
		color: #B45D2D;
	}

	.detail-goods__price-value {
		margin-left: 2rpx;
		font-size: 28rpx;
		font-weight: 800;
		color: #B45D2D;
	}

	.detail-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding: 12rpx 0 8rpx;
	}

	/* �?���\�?�?*/
	.detail-actions {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 20rpx var(--ry-page-padding-x);
		padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
		background-color: #FFFFFF;
		border-top: 1rpx solid #F0EBE6;
		box-shadow: 0 -4rpx 16rpx rgba(74, 55, 40, 0.06);
		z-index: 99;

		&__item {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 4rpx;
			width: 96rpx;
			height: 80rpx;

			&:active {
				opacity: 0.7;
			}
		}

		&__text {
			font-size: 20rpx;
			color: #8D6E63;
		}

		&__btn {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			height: 80rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #8D6E63, #5D4037);
			box-shadow: 0 8rpx 24rpx rgba(93, 64, 55, 0.3);

			&:active {
				opacity: 0.9;
			}
		}

		&__btn-text {
			font-size: 28rpx;
			font-weight: 600;
			color: #FFFFFF;
		}
	}
</style>
