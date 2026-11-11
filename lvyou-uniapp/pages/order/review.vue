<template>
	<view class="or-page" :class="`or-page--${category}`">
		<view class="or-card ry-card">
			<view class="or-product">
				<image class="or-product__cover ry-placeholder-img" :src="coverImage" mode="aspectFill" />
				<view class="or-product__info">
					<text class="or-product__title">{{ productTitle }}</text>
					<text v-if="productSub" class="or-product__sub">{{ productSub }}</text>
				</view>
			</view>
		</view>

		<view class="or-card ry-card">
			<text class="or-label">{{ ui.reviewScoreLabel }}</text>
			<view class="or-stars">
				<u-icon v-for="index in 5" :key="index" :name="index <= rating ? 'star-fill' : 'star'"
					:color="index <= rating ? accentColor : '#E5E7EB'" :size="28" @click="rating = index" />
			</view>
		</view>

		<view class="or-card ry-card">
			<text class="or-label">{{ ui.reviewContentLabel }}</text>
			<view class="or-textarea-wrap">
				<u-textarea v-model="content" :placeholder="ui.reviewPlaceholder" :maxlength="512" :count="true" border="none"
					height="180" color="#1a1a1a" placeholder-style="color:#999999" />
			</view>
		</view>

		<view class="or-card ry-card">
			<view class="or-images-head">
				<text class="or-label or-label--inline">{{ ui.reviewImagesLabel }}</text>
				<text class="or-images-tip">{{ ui.reviewImagesTip }}</text>
			</view>
			<view class="or-images">
				<view v-for="(img, index) in images" :key="`${img}-${index}`" class="or-images__item">
					<image class="or-images__img" :src="img" mode="aspectFill" @click="previewImage(index)" />
					<view class="or-images__del" @click.stop="removeImage(index)">
						<u-icon name="close" color="#FFFFFF" :size="12" />
					</view>
				</view>
				<view v-if="images.length < maxImages" class="or-images__add" @click="chooseImage">
					<u-icon name="camera-fill" color="#CCCCCC" :size="28" />
					<text class="or-images__add-text">{{ ui.reviewImagesAdd }}</text>
				</view>
			</view>
		</view>

		<view class="or-footer">
			<u-button :text="ui.reviewSubmit" block :color="accentColor" :custom-style="submitStyle" :loading="submitting"
				:disabled="!canSubmit" @click="onSubmit" />
		</view>
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
		PLACEHOLDER_LOGO,
		getModuleColor
	} from '@/common/theme.js';
	import {
		hasToken
	} from '@/common/api/auth.js';
	import {
		fetchOrderDetail
	} from '@/common/api/order.js';
	import {
		submitHotelReview
	} from '@/common/api/hotel.js';
	import {
		submitGoodsReview
	} from '@/common/api/heritage.js';
	import {
		submitRouteStoreReview
	} from '@/common/api/route.js';
	import {
		ORDER_UI,
		ORDER_CATEGORY_HOTEL,
		ORDER_CATEGORY_GOODS,
		ORDER_CATEGORY_ROUTE,
		formatHotelStayLine,
		inferOrderCategory,
		mapOrderVo
	} from '@/common/order-ui.js';
	import {
		isRestaurantFlow
	} from '@/common/order-type.js';
	import {
		uploadFeedbackFile,
		resolveUploadUrl,
		isLocalTempPath
	} from '@/common/api/feedback.js';

	const ui = ORDER_UI;
	const orderId = ref('');
	const record = ref({});
	const rating = ref(5);
	const content = ref('');
	const images = ref([]);
	const uploading = ref(false);
	const maxImages = 6;
	const submitting = ref(false);
	const submitStyle = {
		height: '48px',
		borderRadius: '24px',
		fontWeight: '600'
	};

	const category = computed(() => inferOrderCategory(record.value) || ORDER_CATEGORY_HOTEL);
	const accentColor = computed(() => {
		if (category.value === ORDER_CATEGORY_GOODS) return getModuleColor('heritageShop');
		if (category.value === ORDER_CATEGORY_ROUTE) return getModuleColor('route');
		return getModuleColor('hotel');
	});

	const coverImage = computed(() => {
		if (category.value === ORDER_CATEGORY_HOTEL) {
			return record.value.hotelCover || record.value.goodsCover || PLACEHOLDER_LOGO;
		}
		if (category.value === ORDER_CATEGORY_ROUTE) {
			return record.value.goodsCover || PLACEHOLDER_LOGO;
		}
		return record.value.goodsCover || PLACEHOLDER_LOGO;
	});

	const productTitle = computed(() => {
		if (category.value === ORDER_CATEGORY_HOTEL) {
			return record.value.merchantName || record.value.destinationName || '酒店订单';
		}
		if (category.value === ORDER_CATEGORY_ROUTE) {
			return record.value.routeName || record.value.merchantName || '线路订单';
		}
		return record.value.goodsName || record.value.merchantName || '商品订单';
	});

	const productSub = computed(() => {
		if (category.value === ORDER_CATEGORY_HOTEL) {
			const parts = [];
			if (record.value.merchantAddress) parts.push(record.value.merchantAddress);
			const stayLine = formatHotelStayLine(record.value);
			if (stayLine) parts.push(stayLine);
			return parts.join(' · ') || '';
		}
		if (category.value === ORDER_CATEGORY_ROUTE) {
			const parts = [];
			if (record.value.tripDate) parts.push(`出行 ${record.value.tripDate}`);
			if (record.value.guestCount) parts.push(`${record.value.guestCount}人`);
			return parts.join(' · ') || '';
		}
		return record.value.goodsSpec || '';
	});

	const canSubmit = computed(() => rating.value >= 1 && content.value.trim().length > 0 && !submitting.value);

	function requireLogin() {
		if (hasToken()) return true;
		uni.showToast({
			title: ui.loginTip,
			icon: 'none'
		});
		setTimeout(() => uni.navigateTo({
			url: '/pages/login/login'
		}), 600);
		return false;
	}

	async function loadOrder() {
		if (!orderId.value) return;
		try {
			const res = await fetchOrderDetail(orderId.value);
			if (res.code !== 0 || !res.data) {
				uni.showToast({
					title: res.msg || ui.loadFail,
					icon: 'none'
				});
				return;
			}
			record.value = mapOrderVo(res.data);
			if (isRestaurantFlow(record.value.orderType)) {
				uni.showToast({
					title: ui.reviewNotAllowed,
					icon: 'none'
				});
				setTimeout(() => uni.navigateBack(), 800);
				return;
			}
			if (record.value.reviewed) {
				uni.showToast({
					title: ui.reviewDuplicate,
					icon: 'none'
				});
				setTimeout(() => uni.navigateBack(), 800);
				return;
			}
			if (!record.value.canReview) {
				uni.showToast({
					title: ui.reviewNotAllowed,
					icon: 'none'
				});
				setTimeout(() => uni.navigateBack(), 800);
			}
		} catch (e) {
			uni.showToast({
				title: ui.loadFail,
				icon: 'none'
			});
		}
	}

	async function ensureImagesUploaded() {
		const urls = [];
		for (const img of images.value) {
			if (isLocalTempPath(img)) {
				const uploadRes = await uploadFeedbackFile(img);
				const url = resolveUploadUrl(uploadRes);
				if (!url) throw new Error(ui.reviewImagesUploadFail);
				urls.push(url);
			} else {
				urls.push(img);
			}
		}
		return urls;
	}

	function chooseImage() {
		if (uploading.value || submitting.value) return;
		const remain = maxImages - images.value.length;
		if (remain <= 0) {
			uni.showToast({ title: ui.reviewImagesLimit, icon: 'none' });
			return;
		}
		uni.chooseImage({
			count: remain,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: async (res) => {
				const paths = (res.tempFilePaths || []).slice(0, remain);
				if (!paths.length) return;
				uploading.value = true;
				uni.showLoading({ title: ui.reviewImagesUploading, mask: true });
				try {
					const uploaded = [];
					for (const path of paths) {
						const uploadRes = await uploadFeedbackFile(path);
						const url = resolveUploadUrl(uploadRes);
						if (!url) {
							uni.showToast({ title: ui.reviewImagesUploadFail, icon: 'none' });
							return;
						}
						uploaded.push(url);
					}
					images.value = images.value.concat(uploaded).slice(0, maxImages);
				} catch (e) {
					uni.showToast({ title: ui.reviewImagesUploadFail, icon: 'none' });
				} finally {
					uploading.value = false;
					uni.hideLoading();
				}
			}
		});
	}

	function removeImage(index) {
		images.value.splice(index, 1);
	}

	function previewImage(index) {
		uni.previewImage({
			current: images.value[index],
			urls: images.value
		});
	}

	async function onSubmit() {
		if (!canSubmit.value || !record.value.id || uploading.value) return;
		submitting.value = true;
		try {
			const imageUrls = await ensureImagesUploaded();
			const payload = {
				orderId: Number(record.value.id),
				rating: rating.value,
				content: content.value.trim(),
				images: imageUrls
			};
			let res;
			if (category.value === ORDER_CATEGORY_HOTEL) {
				if (!record.value.hotelId) {
					uni.showToast({
						title: ui.reviewNotAllowed,
						icon: 'none'
					});
					return;
				}
				res = await submitHotelReview(record.value.hotelId, payload);
			} else if (category.value === ORDER_CATEGORY_GOODS) {
				if (!record.value.goodsId) {
					uni.showToast({
						title: ui.reviewNotAllowed,
						icon: 'none'
					});
					return;
				}
				res = await submitGoodsReview(record.value.goodsId, payload);
			} else if (category.value === ORDER_CATEGORY_ROUTE) {
				if (!record.value.merchantId) {
					uni.showToast({
						title: ui.reviewNotAllowed,
						icon: 'none'
					});
					return;
				}
				res = await submitRouteStoreReview(record.value.merchantId, payload);
			} else {
				uni.showToast({
					title: ui.reviewNotAllowed,
					icon: 'none'
				});
				return;
			}
			if (res.code === 0) {
				uni.showToast({
					title: ui.reviewSuccess,
					icon: 'success'
				});
				setTimeout(() => uni.navigateBack(), 800);
				return;
			}
			uni.showToast({
				title: res.msg || ui.reviewFail,
				icon: 'none'
			});
		} catch (e) {
			uni.showToast({
				title: ui.reviewFail,
				icon: 'none'
			});
		} finally {
			submitting.value = false;
		}
	}

	onLoad((options) => {
		orderId.value = options?.id ? String(options.id) : '';
		uni.setNavigationBarTitle({
			title: ui.reviewTitle
		});
	});

	onShow(() => {
		if (!requireLogin()) return;
		loadOrder();
	});
</script>

<style lang="scss" scoped>
	:deep(.u-textarea),
	:deep(.u-textarea__count) {
		background-color: transparent !important;
	}

	:deep(.u-textarea__field),
	:deep(.u-textarea textarea) {
		color: #1a1a1a !important;
	}

	.or-page {
		min-height: 100vh;
		padding: 24rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 20rpx;
		box-sizing: border-box;
	}

	.or-page--hotel {
		background: #f4f7fc;
	}

	.or-page--goods {
		background: #f8f5f2;
	}

	.or-card {
		padding: 28rpx;
	}

	.or-product {
		display: flex;
		gap: 20rpx;
		align-items: center;

		&__cover {
			width: 120rpx;
			height: 120rpx;
			border-radius: 16rpx;
			flex-shrink: 0;
			background: #f3f4f6;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 600;
			color: #1a1a1a;
			line-height: 1.4;
		}

		&__sub {
			font-size: 24rpx;
			color: #999;
		}
	}

	.or-label {
		display: block;
		font-size: 28rpx;
		font-weight: 600;
		color: #1a1a1a;
		margin-bottom: 20rpx;
	}

	.or-stars {
		display: flex;
		gap: 16rpx;
	}

	.or-textarea-wrap {
		padding: 16rpx 20rpx;
		border-radius: 16rpx;
		background: #f7f9fc;
	}

	.or-label--inline {
		margin-bottom: 0;
	}

	.or-images-head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: 12rpx;
		margin-bottom: 20rpx;
	}

	.or-images-tip {
		font-size: 24rpx;
		color: #999;
	}

	.or-images {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}

	.or-images__item,
	.or-images__add {
		width: 160rpx;
		height: 160rpx;
		border-radius: 16rpx;
		overflow: hidden;
		position: relative;
	}

	.or-images__img {
		width: 100%;
		height: 100%;
		background: #f3f4f6;
	}

	.or-images__del {
		position: absolute;
		top: 8rpx;
		right: 8rpx;
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background: rgba(0, 0, 0, 0.45);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.or-images__add {
		border: 1rpx dashed #ddd;
		background: #fafafa;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
	}

	.or-images__add-text {
		font-size: 22rpx;
		color: #999;
	}

	.or-footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.04);
	}
</style>
