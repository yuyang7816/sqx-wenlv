<template>
	<view class="cd-page">
		<view v-if="loading && !record.id" class="cd-loading">
			<view class="cd-loading__spinner" />
			<text class="cd-loading__text">加载预约详情…</text>
		</view>

		<view v-else-if="!record.id" class="cd-empty">
			<view class="cd-empty__illus">
				<view class="cd-empty__circle">
					<u-icon name="file-text" :color="accentColor" :size="44" />
				</view>
			</view>
			<text class="cd-empty__title">预约不存在</text>
			<text class="cd-empty__desc">该预约可能已被删除，或链接已失效</text>
			<view class="cd-empty__btn" @click="goRecords">
				<text class="cd-empty__btn-text">查看预约记录</text>
			</view>
		</view>

		<template v-else>
			<view class="cd-hero" :class="`cd-hero--${record.status}`">
				<view class="cd-hero__blob cd-hero__blob--1" />
				<view class="cd-hero__blob cd-hero__blob--2" />
				<view class="cd-hero__inner">
					<view class="cd-hero__status-row">
						<view class="cd-hero__status-dot" />
						<text class="cd-hero__status">{{ record.statusLabel }}</text>
					</view>
					<text class="cd-hero__tip">{{ statusTip }}</text>
					<view class="cd-hero__no-row" @click="onCopyNo">
						<text class="cd-hero__no-label">预约编号</text>
						<text class="cd-hero__no-value">{{ record.reservationNo || record.id }}</text>
						<u-icon name="file-text" color="rgba(255,255,255,0.75)" :size="14" />
					</view>
				</view>
			</view>

			<view class="cd-body">
				<view class="cd-time ry-card">
					<view class="cd-time__head">
						<view class="cd-time__icon">
							<u-icon name="calendar-fill" :color="accentColor" :size="18" />
						</view>
						<text class="cd-time__label">到店时间</text>
					</view>
					<view class="cd-time__main">
						<view class="cd-time__date-block">
							<text class="cd-time__date">{{ reserveDate }}</text>
							<text v-if="reserveWeekday" class="cd-time__week">{{ reserveWeekday }}</text>
						</view>
						<view class="cd-time__divider" />
						<view class="cd-time__clock-block">
							<text class="cd-time__clock">{{ reserveClock || '--:--' }}</text>
							<text class="cd-time__clock-unit">预计到店</text>
						</view>
					</view>
				</view>

				<view v-if="record.status !== 'cancelled'" class="cd-steps ry-card">
					<view v-for="(step, index) in progressSteps" :key="step.key" class="cd-steps__item"
						:class="{ 'cd-steps__item--active': step.active, 'cd-steps__item--done': step.done }">
						<view class="cd-steps__node">
							<u-icon v-if="step.done" name="checkmark" color="#FFFFFF" :size="12" />
							<text v-else class="cd-steps__node-num">{{ index + 1 }}</text>
						</view>
						<text class="cd-steps__label">{{ step.label }}</text>
						<view v-if="index < progressSteps.length - 1" class="cd-steps__line" />
					</view>
				</view>

				<view v-else class="cd-cancelled ry-card">
					<u-icon name="close-circle-fill" color="#BBBBBB" :size="20" />
					<text class="cd-cancelled__text">预约已取消，如需用餐请重新提交预约</text>
				</view>

				<view class="cd-section ry-card">
					<view class="cd-section__head">
						<view class="cd-section__head-icon">
							<u-icon name="account-fill" :color="accentColor" :size="16" />
						</view>
						<text class="cd-section__title">联系人</text>
					</view>
					<view class="cd-contact">
						<view class="cd-contact__info">
							<text class="cd-contact__name">{{ record.contactName }}</text>
							<text v-if="record.phone" class="cd-contact__dot">·</text>
							<text v-if="record.phone" class="cd-contact__item">{{ record.phone }}</text>
							<text v-if="record.guestCount" class="cd-contact__dot">·</text>
							<text v-if="record.guestCount" class="cd-contact__item">{{ record.guestCount }} 人用餐</text>
						</view>
						<view class="cd-contact__action" @click="onCall">
							<u-icon name="phone-fill" :color="accentColor" :size="16" />
							<text class="cd-contact__action-text">拨打</text>
						</view>
					</view>
				</view>

				<view v-if="displayShopName || displayShopAddress" class="cd-shop-section">
					<!-- <view class="cd-shop-section__head">
						<view class="cd-shop-section__icon">
							<u-icon name="home-fill" :color="accentColor" :size="16" />
						</view>
						<text class="cd-shop-section__title">用餐门店</text>
					</view> -->
					<view class="cd-shop-card ry-card">
						<view class="cd-shop-card__body">
							<view class="cd-shop-card__cover">
								<image class="cd-shop-card__cover-img ry-placeholder-img"
									:src="shopCover || record.shopCover || PLACEHOLDER_LOGO" mode="aspectFill" />
							</view>
							<view class="cd-shop-card__main">
								<view class="cd-shop-card__top">
									<view class="cd-shop-card__head">
										<text class="cd-shop-card__name">{{ displayShopName }}</text>
										<view v-if="shopIsOpen != null" class="cd-shop-card__status"
											:class="{ 'cd-shop-card__status--off': !shopIsOpen }">
											<text class="cd-shop-card__status-text">{{ shopIsOpen ? '营业中' : '休息中' }}</text>
										</view>
									</view>
									<view v-if="shopMonthlySales != null" class="cd-shop-card__meta">
										<text class="cd-shop-card__meta-text">已预约数 {{ shopMonthlySales }}</text>
									</view>
								</view>
								<view v-if="displayShopAddress" class="cd-shop-card__addr">
									<view class="cd-shop-card__addr-icon">
										<u-icon name="map" color="#C8C8C8" :size="12" />
									</view>
									<text class="cd-shop-card__addr-text">{{ displayShopAddress }}</text>
								</view>
								<view class="cd-shop-card__actions">
									<view class="cd-shop-card__btn cd-shop-card__btn--ghost" @click="onNavigate">
										<u-icon name="map-fill" :color="accentColor" :size="14" />
										<text class="cd-shop-card__btn-text cd-shop-card__btn-text--ghost">导航</text>
									</view>
									<view class="cd-shop-card__btn cd-shop-card__btn--primary" @click="onCallMerchant">
										<u-icon name="phone-fill" color="#FFFFFF" :size="14" />
										<text class="cd-shop-card__btn-text">联系商家</text>
									</view>
								</view>
								<view v-if="record.dishName" class="cd-shop-card__bottom">
									<view class="cd-shop-card__dish">
										<u-icon name="bag-fill" :color="accentColor" :size="12" />
										<text class="cd-shop-card__dish-text">预约菜品：{{ record.dishName }}</text>
									</view>
								</view>
							</view>
						</view>
					</view>
				</view>

				<view v-if="record.createdAt" class="cd-meta ry-card">
					<view class="cd-meta__row">
						<text class="cd-meta__label">提交时间</text>
						<text class="cd-meta__value">{{ record.createdAt }}</text>
					</view>
				</view>

				<view class="cd-notice">
					<view class="cd-notice__head">
						<u-icon name="info-circle-fill" :color="accentColor" :size="16" />
						<text class="cd-notice__title">用餐须知</text>
					</view>
					<view class="cd-notice__list">
						<view v-for="(item, index) in noticeList" :key="index" class="cd-notice__item">
							<view class="cd-notice__index">{{ index + 1 }}</view>
							<text class="cd-notice__text">{{ item }}</text>
						</view>
					</view>
				</view>

				<text class="cd-footer">非遗美食 · 地道卤味</text>
			</view>
		</template>

		<view v-if="record.id && (record.status === 'pending' || record.status === 'confirmed')" class="cd-bar">
			<view class="cd-bar__ghost" @click="onCancel">
				<u-icon name="close-circle" :color="accentColor" :size="20" />
				<text class="cd-bar__ghost-text">取消预约</text>
			</view>
			<view class="cd-bar__primary" @click="onModify">
				<u-icon name="edit-pen-fill" color="#FFFFFF" :size="18" />
				<text class="cd-bar__primary-text">修改预约</text>
			</view>
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
		cancelCanteenReservation,
		fetchCanteenDetail,
		mapCanteenRecord
	} from '@/common/api/canteen.js';
	import {
		fetchGooseDetail,
		fetchGooseList
	} from '@/common/api/goose.js';

	const accentColor = getModuleColor('goose');
	const CANTEEN_RESERVE_PATH = '/mobile-design/canteen/reserve';
	const CANTEEN_RECORDS_PATH = '/mobile-design/canteen/records';

	const recordId = ref('');
	const record = ref({});
	const loading = ref(false);
	const shopCover = ref('');
	const shopPhone = ref('');
	const shopLatitude = ref(null);
	const shopLongitude = ref(null);
	const shopMonthlySales = ref(null);
	const shopIsOpen = ref(null);
	const shopNameLoaded = ref('');
	const shopAddressLoaded = ref('');

	const displayShopName = computed(() => shopNameLoaded.value || record.value.shopName || record.value.merchantName ||
		'');
	const displayShopAddress = computed(() => shopAddressLoaded.value || record.value.address || '');

	const reserveDate = computed(() => {
		const time = record.value.reserveTime || '';
		const parts = time.split(' ');
		return parts[0] || time;
	});

	const reserveClock = computed(() => {
		const time = record.value.reserveTime || '';
		const parts = time.split(' ');
		const clock = parts[1] || '';
		return clock ? clock.slice(0, 5) : '';
	});

	const reserveWeekday = computed(() => {
		const dateStr = reserveDate.value;
		if (!dateStr) return '';
		const d = new Date(`${dateStr}T00:00:00`);
		if (Number.isNaN(d.getTime())) return '';
		const labels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
		return labels[d.getDay()] || '';
	});

	const statusTip = computed(() => {
		const tips = {
			pending: '预约已提交，等待门店确认中',
			confirmed: '请按时到店，向工作人员出示预约信息即可用餐',
			completed: '本次用餐已完成，感谢您的光临',
			cancelled: '预约已取消，欢迎再次预约'
		};
		return tips[record.value.status] || '请留意预约时间，按时到店用餐';
	});

	const progressSteps = computed(() => {
		const status = record.value.status;
		const order = ['pending', 'confirmed', 'completed'];
		const labels = ['提交预约', '门店确认', '到店用餐'];
		const currentIndex = order.indexOf(status);
		return labels.map((label, index) => ({
			key: order[index],
			label,
			done: currentIndex > index,
			active: currentIndex === index || (status === 'completed' && index === 2)
		}));
	});

	const noticeList = [
		'请提前 10 分钟到达用餐地点，超时 15 分钟预约可能失效',
		'如需变更时间或人数，可使用「修改预约」或提前取消后重新预约',
		'到店后向工作人员报联系人姓名及预约编号即可入座'
	];

	function resetShopInfo() {
		shopCover.value = '';
		shopPhone.value = '';
		shopLatitude.value = null;
		shopLongitude.value = null;
		shopMonthlySales.value = null;
		shopIsOpen.value = null;
		shopNameLoaded.value = '';
		shopAddressLoaded.value = '';
	}

	function applyShopDetail(data = {}) {
		if (data.name) shopNameLoaded.value = data.name;
		if (data.address) shopAddressLoaded.value = data.address;
		if (data.cover) shopCover.value = data.cover;
		if (data.phone) shopPhone.value = data.phone;
		if (data.latitude != null) shopLatitude.value = Number(data.latitude);
		if (data.longitude != null) shopLongitude.value = Number(data.longitude);
		if (data.monthlySales != null) shopMonthlySales.value = data.monthlySales;
		if (data.isOpen != null) shopIsOpen.value = Number(data.isOpen) === 1;
	}

	async function loadShopInfo(mappedRecord = {}) {
		resetShopInfo();
		const fallbackName = mappedRecord.shopName || mappedRecord.merchantName || '';
		const fallbackAddress = mappedRecord.address || '';
		if (fallbackName) shopNameLoaded.value = fallbackName;
		if (fallbackAddress) shopAddressLoaded.value = fallbackAddress;
		if (mappedRecord.shopCover) shopCover.value = mappedRecord.shopCover;

		const merchantId = mappedRecord.merchantId;
		if (!merchantId && !fallbackName) return;

		try {
			if (merchantId) {
				const detailRes = await fetchGooseDetail(merchantId);
				if (detailRes.code === 0 && detailRes.data) {
					applyShopDetail(detailRes.data);
				}
				return;
			}

			const listRes = await fetchGooseList({
				keyword: fallbackName,
				limit: 20
			});
			if (listRes.code !== 0 || !listRes.data) return;
			const items = listRes.data.records || [];
			const matched = items.find((item) => item.name === fallbackName) ||
				(fallbackAddress ? items.find((item) => item.address === fallbackAddress) : null) ||
				items[0];
			if (!matched?.id) return;

			if (matched.cover && !shopCover.value) shopCover.value = matched.cover;
			const detailRes = await fetchGooseDetail(matched.id);
			if (detailRes.code === 0 && detailRes.data) {
				applyShopDetail(detailRes.data);
			}
		} catch (e) {
			// 门店详情加载失败不影响预约详情展示
		}
	}

	const loadRecord = async () => {
		if (!recordId.value) {
			record.value = {};
			resetShopInfo();
			return;
		}
		if (!hasToken()) {
			record.value = {};
			resetShopInfo();
			return;
		}
		loading.value = true;
		try {
			const res = await fetchCanteenDetail(recordId.value);
			if (res.code === 0 && res.data) {
				record.value = mapCanteenRecord(res.data);
				loadShopInfo(record.value);
			} else {
				record.value = {};
				resetShopInfo();
				if (res.msg) uni.showToast({
					title: res.msg,
					icon: 'none'
				});
			}
		} catch (e) {
			record.value = {};
			resetShopInfo();
			uni.showToast({
				title: '加载失败',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	};

	const navigateSafe = (url) => uni.navigateTo({
		url
	});

	const goRecords = () => navigateSafe(CANTEEN_RECORDS_PATH);

	const onCall = () => {
		const phone = record.value.phone;
		if (!phone) return;
		/* #ifdef H5 */
		uni.showToast({
			title: `请拨打 ${phone}`,
			icon: 'none'
		});
		return;
		/* #endif */
		uni.makePhoneCall({
			phoneNumber: String(phone).replace(/-/g, ''),
			fail: () => uni.showToast({
				title: `请拨打 ${phone}`,
				icon: 'none'
			})
		});
	};

	const onCopyNo = () => {
		const no = record.value.reservationNo || record.value.id;
		if (!no) return;
		uni.setClipboardData({
			data: String(no),
			success: () => uni.showToast({
				title: '编号已复制',
				icon: 'success'
			})
		});
	};

	function onNavigate() {
		const latitude = shopLatitude.value;
		const longitude = shopLongitude.value;
		const address = displayShopAddress.value;
		const name = displayShopName.value;
		if (!latitude || !longitude) {
			uni.showToast({
				title: address || '暂无地址信息',
				icon: 'none'
			});
			return;
		}
		uni.openLocation({
			latitude,
			longitude,
			name,
			address,
			fail: () => uni.showToast({
				title: address || '打开导航失败',
				icon: 'none'
			})
		});
	}

	function onCallMerchant() {
		const phone = shopPhone.value;
		if (!phone) {
			uni.showToast({
				title: '暂无商家联系电话',
				icon: 'none'
			});
			return;
		}
		/* #ifdef H5 */
		uni.showToast({
			title: `请拨打 ${phone}`,
			icon: 'none'
		});
		return;
		/* #endif */
		uni.makePhoneCall({
			phoneNumber: String(phone).replace(/-/g, ''),
			fail: () => uni.showToast({
				title: `请拨打 ${phone}`,
				icon: 'none'
			})
		});
	}

	const onCancel = () => {
		uni.showModal({
			title: '取消预约',
			content: `确定取消 ${record.value.contactName} 的餐饮预约吗？`,
			confirmColor: accentColor,
			success: async (res) => {
				if (!res.confirm) return;
				try {
					const cancelRes = await cancelCanteenReservation(recordId.value);
					if (cancelRes.code === 0) {
						uni.showToast({
							title: '预约已取消',
							icon: 'success'
						});
						await loadRecord();
						return;
					}
					uni.showToast({
						title: cancelRes.msg || '取消失败',
						icon: 'none'
					});
				} catch (e) {
					uni.showToast({
						title: '网络异常，请稍后重试',
						icon: 'none'
					});
				}
			}
		});
	};

	const onModify = () => {
		navigateSafe(`${CANTEEN_RESERVE_PATH}?mode=edit&id=${encodeURIComponent(recordId.value)}`);
	};

	onLoad((options) => {
		recordId.value = String(options?.id || '');
		if (!hasToken()) {
			uni.navigateTo({
				url: '/pages/my/login'
			});
			return;
		}
		loadRecord();
		uni.setNavigationBarTitle({
			title: '预约详情'
		});
	});

	onShow(() => {
		if (recordId.value && hasToken()) loadRecord();
	});
</script>

<style lang="scss" scoped>
	.cd-page {
		min-height: 100vh;
		background-color: #FFF9F2;
	}

	.cd-loading {
		min-height: 60vh;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 20rpx;

		&__spinner {
			width: 48rpx;
			height: 48rpx;
			border-radius: 50%;
			border: 4rpx solid #FFE8D6;
			border-top-color: #FF7A00;
			animation: cd-spin 0.8s linear infinite;
		}

		&__text {
			font-size: 26rpx;
			color: #999999;
		}
	}

	@keyframes cd-spin {
		to {
			transform: rotate(360deg);
		}
	}

	.cd-empty {
		min-height: 70vh;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 40rpx var(--ry-page-padding-x);

		&__illus {
			margin-bottom: 32rpx;
		}

		&__circle {
			width: 140rpx;
			height: 140rpx;
			border-radius: 50%;
			background: linear-gradient(145deg, #FFF6EF, #FFE8D6);
			display: flex;
			align-items: center;
			justify-content: center;
			// box-shadow: 0 12rpx 40rpx rgba(255, 122, 0, 0.12);
		}

		&__title {
			font-size: 32rpx;
			font-weight: 700;
			color: #333333;
			line-height: 1.4;
		}

		&__desc {
			margin-top: 12rpx;
			font-size: 24rpx;
			color: #999999;
			line-height: 1.6;
			text-align: center;
		}

		&__btn {
			margin-top: 40rpx;
			height: 80rpx;
			padding: 0 56rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #FF9A40, #FF7A00);
			// box-shadow: 0 10rpx 28rpx rgba(255, 122, 0, 0.3);
			display: flex;
			align-items: center;
			justify-content: center;

			&:active {
				opacity: 0.9;
			}
		}

		&__btn-text {
			font-size: 28rpx;
			font-weight: 700;
			color: #FFFFFF;
		}
	}

	.cd-hero {
		position: relative;
		overflow: hidden;
		padding: 32rpx var(--ry-page-padding-x) 88rpx;
		background: linear-gradient(135deg, #FF9A40 0%, #FF7A00 100%);

		&--pending,
		&--confirmed {
			background: linear-gradient(135deg, #FF9A40 0%, #FF7A00 100%);
		}

		&--completed {
			background: linear-gradient(135deg, #5FD68A 0%, #2EAF6A 100%);
		}

		&--cancelled {
			background: linear-gradient(135deg, #C8C8C8 0%, #999999 100%);
		}

		&__blob {
			position: absolute;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.12);
			pointer-events: none;

			&--1 {
				width: 280rpx;
				height: 280rpx;
				top: -80rpx;
				right: -60rpx;
			}

			&--2 {
				width: 180rpx;
				height: 180rpx;
				bottom: -40rpx;
				left: -30rpx;
			}
		}

		&__inner {
			position: relative;
			z-index: 1;
		}

		&__status-row {
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__status-dot {
			width: 12rpx;
			height: 12rpx;
			border-radius: 50%;
			background-color: #FFFFFF;
			// box-shadow: 0 0 0 6rpx rgba(255, 255, 255, 0.25);
		}

		&__status {
			font-size: 40rpx;
			font-weight: 800;
			color: #FFFFFF;
			line-height: 1.2;
			letter-spacing: 1rpx;
		}

		&__tip {
			display: block;
			margin-top: 12rpx;
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.88);
			line-height: 1.55;
		}

		&__no-row {
			display: inline-flex;
			align-items: center;
			gap: 10rpx;
			margin-top: 24rpx;
			padding: 10rpx 20rpx;
			border-radius: 999rpx;
			background: rgba(255, 255, 255, 0.18);

			&:active {
				opacity: 0.85;
			}
		}

		&__no-label {
			font-size: 22rpx;
			color: rgba(255, 255, 255, 0.75);
		}

		&__no-value {
			font-size: 24rpx;
			font-weight: 600;
			color: #FFFFFF;
			font-family: ui-monospace, monospace;
		}
	}

	.cd-body {
		margin-top: -56rpx;
		padding: 0 var(--ry-page-padding-x) calc(160rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 20rpx;
		position: relative;
		z-index: 2;
	}

	.cd-time {
		padding: 28rpx;

		&__head {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 20rpx;
		}

		&__icon {
			width: 44rpx;
			height: 44rpx;
			border-radius: 12rpx;
			background-color: #FFF6EF;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__label {
			font-size: 26rpx;
			font-weight: 500;
			color: #888888;
		}

		&__main {
			display: flex;
			align-items: center;
			gap: 24rpx;
		}

		&__date-block {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 6rpx;
		}

		&__date {
			font-size: 34rpx;
			font-weight: 600;
			color: #333333;
			line-height: 1.2;
		}

		&__week {
			font-size: 22rpx;
			font-weight: 400;
			color: #FF7A00;
		}

		&__divider {
			flex-shrink: 0;
			width: 2rpx;
			height: 72rpx;
			background: linear-gradient(180deg, transparent, #FFE8D6, transparent);
		}

		&__clock-block {
			flex-shrink: 0;
			display: flex;
			flex-direction: column;
			align-items: flex-end;
			gap: 4rpx;
		}

		&__clock {
			font-size: 44rpx;
			font-weight: 800;
			color: #FF7A00;
			line-height: 1;
			font-variant-numeric: tabular-nums;
		}

		&__clock-unit {
			font-size: 20rpx;
			color: #BBBBBB;
		}
	}

	.cd-steps {
		padding: 28rpx 20rpx;
		display: flex;
		align-items: flex-start;
		justify-content: space-between;

		&__item {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: center;
			position: relative;
			min-width: 0;

			&--done {
				.cd-steps__node {
					background: linear-gradient(135deg, #FF9A40, #FF7A00);
					border-color: transparent;
				}

				.cd-steps__node-num {
					color: #FFFFFF;
				}

				.cd-steps__label {
					color: #FF7A00;
					font-weight: 500;
				}

				.cd-steps__line {
					background: linear-gradient(90deg, #FF9A40, #FFD4B8);
				}
			}

			&--active {
				.cd-steps__node {
					background: linear-gradient(135deg, #FF9A40, #FF7A00);
					border-color: transparent;
					// box-shadow: 0 4rpx 16rpx rgba(255, 122, 0, 0.35);
				}

				.cd-steps__node-num {
					color: #FFFFFF;
				}

				.cd-steps__label {
					color: #FF7A00;
					font-weight: 600;
				}
			}
		}

		&__node {
			width: 44rpx;
			height: 44rpx;
			border-radius: 50%;
			background-color: #FFF6EF;
			display: flex;
			align-items: center;
			justify-content: center;
			position: relative;
			z-index: 1;
		}

		&__node-num {
			font-size: 22rpx;
			font-weight: 700;
			color: #CCCCCC;
			line-height: 1;
		}

		&__label {
			margin-top: 12rpx;
			font-size: 22rpx;
			color: #BBBBBB;
			line-height: 1.3;
			text-align: center;
		}

		&__line {
			position: absolute;
			top: 22rpx;
			left: calc(50% + 22rpx);
			width: calc(100% - 44rpx);
			height: 4rpx;
			background-color: #FFF0E6;
			border-radius: 2rpx;
			z-index: 0;
		}
	}

	.cd-cancelled {
		padding: 24rpx 28rpx;
		display: flex;
		align-items: center;
		gap: 14rpx;
		background-color: #FAFAFA;

		&__text {
			flex: 1;
			font-size: 24rpx;
			color: #999999;
			line-height: 1.55;
		}
	}

	.cd-section {
		padding: 24rpx 28rpx;

		&__head {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 16rpx;
		}

		&__head-icon {
			width: 40rpx;
			height: 40rpx;
			border-radius: 10rpx;
			background-color: #FFF6EF;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__title {
			font-size: 28rpx;
			font-weight: 500;
			color: #666666;
		}
	}

	.cd-contact {
		display: flex;
		align-items: center;
		gap: 16rpx;

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			flex-wrap: nowrap;
			overflow: hidden;
		}

		&__name {
			flex-shrink: 0;
			max-width: 36%;
			font-size: 28rpx;
			font-weight: 600;
			color: #333333;
			line-height: 1.3;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__dot {
			flex-shrink: 0;
			margin: 0 10rpx;
			font-size: 22rpx;
			color: #DDDDDD;
			line-height: 1;
		}

		&__item {
			flex-shrink: 1;
			min-width: 0;
			font-size: 24rpx;
			color: #999999;
			line-height: 1.3;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__action {
			flex-shrink: 0;
			display: inline-flex;
			flex-direction: row;
			align-items: center;
			justify-content: center;
			gap: 6rpx;
			height: 56rpx;
			padding: 0 20rpx;
			border-radius: 999rpx;
			background-color: #FFF6EF;

			&:active {
				opacity: 0.8;
			}
		}

		&__action-text {
			font-size: 24rpx;
			font-weight: 600;
			color: #FF7A00;
			line-height: 1;
		}
	}

	.cd-shop-section {
		display: flex;
		flex-direction: column;
		gap: 16rpx;

		&__head {
			display: flex;
			align-items: center;
			gap: 10rpx;
			padding: 0 4rpx;
		}

		&__icon {
			width: 44rpx;
			height: 44rpx;
			border-radius: 12rpx;
			background-color: #FFF6EF;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__title {
			font-size: 28rpx;
			font-weight: 500;
			color: #666666;
			line-height: 1;
		}
	}

	.cd-shop-card {
		display: flex;
		flex-direction: column;
		padding: 20rpx;
		overflow: hidden;

		&__body {
			display: flex;
			align-items: stretch;
			gap: 20rpx;
		}

		&__cover {
			flex-shrink: 0;
			width: 160rpx;
			height: 160rpx;
			border-radius: 16rpx;
			overflow: hidden;
			background-color: #FFF3EB;
		}

		&__cover-img {
			width: 100%;
			height: 100%;
			display: block;
		}

		&__main {
			flex: 1;
			min-width: 0;
			min-height: 160rpx;
			display: flex;
			flex-direction: column;
			justify-content: flex-start;
			gap: 8rpx;
		}

		&__top {
			display: flex;
			flex-direction: column;
			gap: 6rpx;
			flex-shrink: 0;
		}

		&__bottom {
			margin-top: 10rpx;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
			flex-shrink: 0;
		}

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 12rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 28rpx;
			font-weight: 600;
			color: #333333;
			line-height: 1.35;
			overflow: hidden;
			text-overflow: ellipsis;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
		}

		&__status {
			flex-shrink: 0;
			display: inline-flex;
			align-items: center;
			justify-content: center;
			min-height: 36rpx;
			padding: 0 12rpx;
			border-radius: 999rpx;
			background-color: #E8F8EF;
			box-sizing: border-box;

			&--off {
				background-color: #F5F5F5;

				.cd-shop-card__status-text {
					color: #999999;
				}
			}
		}

		&__status-text {
			font-size: 20rpx;
			font-weight: 600;
			color: #1FA855;
			line-height: 1;
		}

		&__meta {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 6rpx;
		}

		&__meta-item {
			display: flex;
			align-items: center;
			gap: 4rpx;
		}

		&__meta-text {
			font-size: 22rpx;
			color: #999999;
			line-height: 1.2;
		}

		&__meta-dot {
			font-size: 22rpx;
			color: #DDDDDD;
			line-height: 1.2;
		}

		&__addr {
			flex: 1;
			min-height: 0;
			display: flex;
			align-items: flex-start;
			gap: 8rpx;
		}

		&__addr-icon {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 24rpx;
			height: 33rpx;
		}

		&__addr-text {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: #999999;
			line-height: 1.5;
			overflow: hidden;
			text-overflow: ellipsis;
			display: -webkit-box;
			-webkit-line-clamp: 3;
			-webkit-box-orient: vertical;
		}

		&__dish {
			display: inline-flex;
			align-items: center;
			align-self: flex-start;
			gap: 6rpx;
			padding: 6rpx 14rpx;
			border-radius: 999rpx;
			background-color: #FFF6EF;
			max-width: 100%;
		}

		&__dish-text {
			flex: 1;
			min-width: 0;
			font-size: 20rpx;
			color: #E86A00;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__actions {
			display: flex;
			align-items: center;
			gap: 12rpx;
			margin-top: auto;
			padding-top: 12rpx;
			flex-shrink: 0;
		}

		&__btn {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 6rpx;
			height: 52rpx;
			border-radius: 999rpx;
			box-sizing: border-box;

			&--ghost {
				background-color: #FFF6EF;
				border: 1rpx solid #FFE8D6;
			}

			&--primary {
				background: linear-gradient(135deg, #FF9A40, #FF7A00);
				// box-shadow: 0 4rpx 12rpx rgba(255, 122, 0, 0.22);
			}

			&:active {
				opacity: 0.88;
			}
		}

		&__btn-text {
			font-size: 22rpx;
			font-weight: 600;
			color: #FFFFFF;
			line-height: 1;

			&--ghost {
				font-weight: 500;
				color: #FF7A00;
			}
		}
	}

	.cd-meta {
		padding: 24rpx 28rpx;

		&__row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 24rpx;
		}

		&__label {
			font-size: 24rpx;
			color: #999999;
		}

		&__value {
			font-size: 24rpx;
			color: #BBBBBB;
			line-height: 1.4;
		}
	}

	.cd-notice {
		padding: 28rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		// box-shadow: 0 4rpx 20rpx rgba(255, 122, 0, 0.04);

		&__head {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 20rpx;
		}

		&__title {
			font-size: 28rpx;
			font-weight: 500;
			color: #666666;
		}

		&__list {
			display: flex;
			flex-direction: column;
			gap: 16rpx;
		}

		&__item {
			display: flex;
			align-items: flex-start;
			gap: 14rpx;
		}

		&__index {
			flex-shrink: 0;
			width: 36rpx;
			height: 36rpx;
			border-radius: 10rpx;
			background: linear-gradient(135deg, #FFF6EF, #FFE8D6);
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 20rpx;
			font-weight: 700;
			color: #FF7A00;
			line-height: 1;
		}

		&__text {
			flex: 1;
			font-size: 24rpx;
			color: #666666;
			line-height: 1.65;
		}
	}

	.cd-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding: 8rpx 0 4rpx;
	}

	.cd-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 20rpx var(--ry-page-padding-x);
		padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
		background-color: #FFFFFF;
		// box-shadow: 0 -8rpx 32rpx rgba(255, 122, 0, 0.08);
		box-sizing: border-box;

		&__ghost {
			flex-shrink: 0;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			width: 120rpx;
			gap: 4rpx;

			&:active {
				opacity: 0.75;
			}
		}

		&__ghost-text {
			font-size: 20rpx;
			font-weight: 500;
			color: #FF7A00;
		}

		&__primary {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			height: 88rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #FF9A40, #FF7A00);
			// box-shadow: 0 8rpx 24rpx rgba(255, 122, 0, 0.32);

			&--full {
				flex: 1;
				width: 100%;
			}

			&:active {
				opacity: 0.9;
			}
		}

		&__primary-text {
			font-size: 30rpx;
			font-weight: 700;
			color: #FFFFFF;
			letter-spacing: 1rpx;
		}
	}
</style>
