<template>
	<view class="canteen-reserve-page">
		<view class="canteen-reserve-body">
			<view class="canteen-shop-section">
				<view class="canteen-shop-section__head">
					<view class="canteen-shop-section__icon">
						<u-icon name="home-fill" :color="accentColor" :size="16" />
					</view>
					<text class="canteen-shop-section__title">预约门店</text>
				</view>
				<view class="canteen-shop-card ry-card">
					<view class="canteen-shop-card__body">
						<view class="canteen-shop-card__cover">
							<image class="canteen-shop-card__cover-img ry-placeholder-img" :src="shopCover || PLACEHOLDER_LOGO"
								mode="aspectFill" />
						</view>
						<view class="canteen-shop-card__main">
							<view class="canteen-shop-card__top">
								<view class="canteen-shop-card__head">
									<text class="canteen-shop-card__name">{{ shopName }}</text>
									<view v-if="shopIsOpen != null" class="canteen-shop-card__status"
										:class="{ 'canteen-shop-card__status--off': !shopIsOpen }">
										<text class="canteen-shop-card__status-text">{{ shopIsOpen ? '营业中' : '休息中' }}</text>
									</view>
								</view>
								<view v-if="shopMonthlySales != null" class="canteen-shop-card__meta">
									<text class="canteen-shop-card__meta-text">已预约数 {{ shopMonthlySales }}</text>
								</view>
							</view>
							<view v-if="shopAddress" class="canteen-shop-card__addr">
								<view class="canteen-shop-card__addr-icon">
									<u-icon name="map" color="#C8C8C8" :size="12" />
								</view>
								<text class="canteen-shop-card__addr-text">{{ shopAddress }}</text>
							</view>
							<view v-if="dishName" class="canteen-shop-card__bottom">
								<view class="canteen-shop-card__dish">
									<u-icon name="bag-fill" :color="accentColor" :size="12" />
									<text class="canteen-shop-card__dish-text">预约菜品：{{ dishName }}</text>
								</view>
							</view>
						</view>
					</view>
					<view class="canteen-shop-card__actions">
						<view class="canteen-shop-card__action" @click="onNavigate">
							<u-icon name="map-fill" :color="accentColor" :size="16" />
							<text class="canteen-shop-card__action-text">导航</text>
						</view>
						<view class="canteen-shop-card__action-divider" />
						<view class="canteen-shop-card__action" @click="onCallMerchant">
							<u-icon name="phone-fill" :color="accentColor" :size="16" />
							<text class="canteen-shop-card__action-text">联系商家</text>
						</view>
					</view>
				</view>
			</view>

			<!-- <view class="canteen-steps">
				<view class="canteen-steps__item canteen-steps__item--active">
					<view class="canteen-steps__dot">1</view>
					<text class="canteen-steps__label">填写信息</text>
				</view>
				<view class="canteen-steps__line" />
				<view class="canteen-steps__item">
					<view class="canteen-steps__dot">2</view>
					<text class="canteen-steps__label">确认预约</text>
				</view>
			</view> -->

			<view class="canteen-section ry-card">
				<view class="canteen-section__head">
					<view class="canteen-section__icon">
						<u-icon name="account-fill" :color="accentColor" :size="18" />
					</view>
					<text class="canteen-section__title">联系人信息</text>
				</view>

				<view class="canteen-field">
					<text class="canteen-field__label">联系人</text>
					<view class="canteen-field__input-wrap">
						<input v-model="form.contactName" class="canteen-field__input" placeholder="请输入联系人姓名"
							placeholder-class="canteen-field__placeholder" />
					</view>
				</view>

				<view class="canteen-field">
					<text class="canteen-field__label">手机号</text>
					<view class="canteen-field__input-wrap">
						<input v-model="form.phone" class="canteen-field__input" type="number" maxlength="11"
							placeholder="请输入11位手机号" placeholder-class="canteen-field__placeholder" />
					</view>
				</view>
			</view>

			<view class="canteen-section ry-card">
				<view class="canteen-section__head">
					<view class="canteen-section__icon">
						<u-icon name="calendar-fill" :color="accentColor" :size="18" />
					</view>
					<text class="canteen-section__title">预约信息</text>
				</view>

				<view class="canteen-field">
					<text class="canteen-field__label">用餐人数</text>
					<view class="canteen-field__input-wrap">
						<input v-model="form.guestCount" class="canteen-field__input" type="number" maxlength="3"
							placeholder="请输入用餐人数" placeholder-class="canteen-field__placeholder" />
						<text class="canteen-field__suffix">人</text>
					</view>
				</view>

				<view class="canteen-field">
					<text class="canteen-field__label">预约时间</text>
					<view class="canteen-field__picker" @click="openDateTimePicker">
						<view class="canteen-field__picker-left">
							<u-icon name="clock-fill" :color="accentColor" :size="16" />
							<text class="canteen-field__picker-text"
								:class="{ 'canteen-field__picker-text--placeholder': !reserveTimeDisplay }">
								{{ reserveTimeDisplay || '请选择预约时间' }}
							</text>
						</view>
						<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
					</view>
				</view>
			</view>

			<view class="canteen-notice">
				<view class="canteen-notice__head">
					<u-icon name="info-circle-fill" :color="accentColor" :size="16" />
					<text class="canteen-notice__title">预约须知</text>
				</view>
				<view class="canteen-notice__list">
					<view v-for="(item, index) in noticeList" :key="index" class="canteen-notice__item">
						<view class="canteen-notice__bullet">
							<text class="canteen-notice__bullet-text">{{ index + 1 }}</text>
						</view>
						<text class="canteen-notice__text">{{ item }}</text>
					</view>
				</view>
			</view>

			<text class="canteen-footer">非遗美食 · 地道卤味</text>
		</view>

		<view class="canteen-bar">
			<view class="canteen-bar__records" @click="goRecords">
				<u-icon name="list" :color="accentColor" :size="22" />
				<text class="canteen-bar__records-text">预约记录</text>
			</view>
			<view class="canteen-bar__submit" @click="onSubmit">
				<u-icon name="checkmark-circle-fill" color="#FFFFFF" :size="18" />
				<text class="canteen-bar__submit-text">{{ submitText }}</text>
			</view>
		</view>

		<u-datetime-picker class="canteen-datetime-picker" v-model:show="showDateTimePicker" v-model="dateTimePickerValue"
			mode="datetime" :min-date="minDateTimestamp" :round="20" :confirm-color="accentColor" @confirm="onDateTimeConfirm"
			@cancel="showDateTimePicker = false" />
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
		getDefaultContactName,
		getDefaultPhone
	} from '@/common/user-prefill.js';
	import {
		hasToken
	} from '@/common/api/auth.js';
	import {
		fetchGooseDetail
	} from '@/common/api/goose.js';
	import {
		fetchCanteenDetail,
		submitCanteenReservation,
		updateCanteenReservation,
		mapCanteenRecord
	} from '@/common/api/canteen.js';

	const accentColor = getModuleColor('goose');
	const CANTEEN_RECORDS_PATH = '/mobile-design/canteen/records';

	const DEFAULT_SHOP_NAME = '老字号·卤鹅旗舰店';

	const noticeList = [
		'提交预约后请按时到店，超时 15 分钟预约可能失效',
		'如需变更可在预约记录中修改或取消后重新预约',
		'到店后向工作人员报联系人姓名即可入座'
	];

	const shopName = ref(DEFAULT_SHOP_NAME);
	const shopId = ref('');
	const shopAddress = ref('');
	const shopCover = ref('');
	const shopPhone = ref('');
	const shopLatitude = ref(null);
	const shopLongitude = ref(null);
	const shopMonthlySales = ref(null);
	const shopIsOpen = ref(null);
	const dishName = ref('');
	const editRecordId = ref('');
	const isEditMode = ref(false);
	const submitting = ref(false);

	const form = ref({
		contactName: getDefaultContactName(),
		phone: getDefaultPhone(),
		guestCount: '2',
		reserveTime: ''
	});

	const showDateTimePicker = ref(false);
	const dateTimePickerValue = ref(Date.now());
	const minDateTimestamp = Date.now();

	const submitText = computed(() => (isEditMode.value ? '保存修改' : '提交预约'));

	const reserveTimeDisplay = computed(() => {
		const timeStr = form.value.reserveTime;
		if (!timeStr) return '';
		const parts = String(timeStr).trim().split(' ');
		if (parts.length < 2) return timeStr;
		const [datePart, clockPart] = parts;
		const clock = clockPart.slice(0, 5);
		const weekday = getWeekdayLabel(datePart);
		return weekday ? `${datePart} ${weekday} ${clock}` : `${datePart} ${clock}`;
	});

	function getWeekdayLabel(dateStr) {
		const d = new Date(`${dateStr}T00:00:00`);
		if (Number.isNaN(d.getTime())) return '';
		const labels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
		return labels[d.getDay()] || '';
	}

	function formatDateTimeStr(timestamp) {
		const d = new Date(timestamp);
		const y = d.getFullYear();
		const m = String(d.getMonth() + 1).padStart(2, '0');
		const day = String(d.getDate()).padStart(2, '0');
		const h = String(d.getHours()).padStart(2, '0');
		const min = String(d.getMinutes()).padStart(2, '0');
		return `${y}-${m}-${day} ${h}:${min}`;
	}

	function getDefaultDateTimeStr() {
		return formatDateTimeStr(Date.now());
	}

	function parseReserveTime(timeStr = '') {
		const raw = String(timeStr).trim();
		if (!raw) {
			const defaultStr = getDefaultDateTimeStr();
			form.value.reserveTime = defaultStr;
			dateTimePickerValue.value = new Date(defaultStr.replace(' ', 'T') + ':00').getTime();
			return;
		}
		const normalized = raw.length >= 16 ? raw.slice(0, 16) : raw;
		form.value.reserveTime = normalized;
		const parsed = new Date(normalized.replace(' ', 'T') + ':00');
		if (!Number.isNaN(parsed.getTime())) {
			dateTimePickerValue.value = parsed.getTime();
		}
	}

	function openDateTimePicker() {
		if (form.value.reserveTime) {
			const parsed = new Date(form.value.reserveTime.replace(' ', 'T') + ':00');
			if (!Number.isNaN(parsed.getTime())) {
				dateTimePickerValue.value = parsed.getTime();
			}
		}
		showDateTimePicker.value = true;
	}

	function onDateTimeConfirm(e) {
		form.value.reserveTime = formatDateTimeStr(e.value);
		showDateTimePicker.value = false;
	}

	function applyShopDetail(data = {}) {
		if (data.name) shopName.value = data.name;
		if (data.address) shopAddress.value = data.address;
		if (data.cover) shopCover.value = data.cover;
		if (data.phone) shopPhone.value = data.phone;
		if (data.latitude != null) shopLatitude.value = Number(data.latitude);
		if (data.longitude != null) shopLongitude.value = Number(data.longitude);
		if (data.monthlySales != null) shopMonthlySales.value = data.monthlySales;
		if (data.isOpen != null) shopIsOpen.value = Number(data.isOpen) === 1;
	}

	async function loadShopInfo(fallbackRecord = {}) {
		if (fallbackRecord.shopCover) shopCover.value = fallbackRecord.shopCover;
		if (fallbackRecord.merchantId) shopId.value = fallbackRecord.merchantId;
		if (fallbackRecord.shopName && !shopName.value) shopName.value = fallbackRecord.shopName;
		if (fallbackRecord.address && !shopAddress.value) shopAddress.value = fallbackRecord.address;

		const merchantId = shopId.value || fallbackRecord.merchantId;
		if (!merchantId) return;

		try {
			const res = await fetchGooseDetail(merchantId);
			if (res.code === 0 && res.data) {
				applyShopDetail(res.data);
			}
		} catch (e) {
			// 门店信息加载失败不影响预约
		}
	}

	function onNavigate() {
		const latitude = shopLatitude.value;
		const longitude = shopLongitude.value;
		const address = shopAddress.value;
		const name = shopName.value;
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
				title: '暂无联系电话',
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

	const navigateSafe = (url) => uni.navigateTo({
		url
	});

	const goRecords = () => navigateSafe(CANTEEN_RECORDS_PATH);

	const loadEditRecord = async (id) => {
		try {
			const res = await fetchCanteenDetail(id);
			if (res.code !== 0 || !res.data) {
				uni.showToast({
					title: res.msg || '预约不存在',
					icon: 'none'
				});
				return;
			}
			const record = mapCanteenRecord(res.data);
			if (record.status !== 'pending' && record.status !== 'confirmed') {
				uni.showToast({
					title: '仅待确认/待到店预约可修改',
					icon: 'none'
				});
				setTimeout(() => navigateSafe(`/mobile-design/canteen/detail?id=${encodeURIComponent(id)}`), 800);
				return;
			}
			if (record.shopName) shopName.value = record.shopName;
			if (record.dishName) dishName.value = record.dishName;
			if (record.address) shopAddress.value = record.address;
			if (record.merchantId) shopId.value = record.merchantId;
			if (record.shopCover) shopCover.value = record.shopCover;
			form.value = {
				contactName: record.contactName,
				phone: record.phone,
				guestCount: String(record.guestCount),
				reserveTime: record.reserveTime
			};
			parseReserveTime(record.reserveTime);
			await loadShopInfo(record);
		} catch (e) {
			uni.showToast({
				title: '加载预约失败',
				icon: 'none'
			});
		}
	};

	const onSubmit = async () => {
		if (!hasToken()) {
			uni.navigateTo({
				url: '/pages/my/login'
			});
			return;
		}
		if (submitting.value) return;

		const {
			contactName,
			phone,
			guestCount,
			reserveTime
		} = form.value;
		if (!contactName.trim()) {
			uni.showToast({
				title: '请填写联系人',
				icon: 'none'
			});
			return;
		}
		if (!/^1\d{10}$/.test(phone)) {
			uni.showToast({
				title: '请填写正确的手机号',
				icon: 'none'
			});
			return;
		}
		const guestCountNum = Number(guestCount);
		if (!guestCount || !Number.isInteger(guestCountNum) || guestCountNum < 1) {
			uni.showToast({
				title: '请填写正确的用餐人数',
				icon: 'none'
			});
			return;
		}
		if (!reserveTime) {
			uni.showToast({
				title: '请选择预约时间',
				icon: 'none'
			});
			return;
		}
		const address = shopAddress.value.trim();
		if (!address) {
			uni.showToast({
				title: '门店地址缺失，请从门店页重新进入',
				icon: 'none'
			});
			return;
		}
		if (!shopId.value) {
			uni.showToast({
				title: '请从餐饮门店页进入预约',
				icon: 'none'
			});
			return;
		}

		const payload = {
			contactName: contactName.trim(),
			phone,
			guestCount: guestCountNum,
			reserveTime,
			address,
			shopName: shopName.value,
			dishName: dishName.value || undefined
		};
		if (shopId.value) {
			payload.shopId = shopId.value;
			payload.merchantId = shopId.value;
		}

		submitting.value = true;
		try {
			const isEdit = isEditMode.value && editRecordId.value;
			const res = isEdit ?
				await updateCanteenReservation(editRecordId.value, payload) :
				await submitCanteenReservation(payload);
			if (res.code === 0) {
				uni.showToast({
					title: isEdit ? '修改成功' : '预约成功',
					icon: 'success'
				});
				const detailId = isEdit ?
					editRecordId.value :
					(res.data?.id ? String(res.data.id) : '');
				setTimeout(() => {
					if (detailId) {
						navigateSafe(`/mobile-design/canteen/detail?id=${encodeURIComponent(detailId)}`);
					} else {
						navigateSafe(CANTEEN_RECORDS_PATH);
					}
				}, 500);
			} else {
				uni.showToast({
					title: res.msg || (isEditMode.value ? '修改失败' : '预约失败'),
					icon: 'none'
				});
			}
		} catch (e) {
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			submitting.value = false;
		}
	};

	const pageInitialized = ref(false);

	function initPageFromOptions(options = {}) {
		isEditMode.value = options?.mode === 'edit';
		if (options?.shopId) shopId.value = String(options.shopId);
		if (options?.shopName) shopName.value = decodeURIComponent(options.shopName);
		if (options?.dishName) dishName.value = decodeURIComponent(options.dishName);
		if (options?.shopAddress) shopAddress.value = decodeURIComponent(options.shopAddress);

		if (isEditMode.value && options?.id) {
			editRecordId.value = String(options.id);
			loadEditRecord(editRecordId.value);
			uni.setNavigationBarTitle({
				title: '修改预约'
			});
		} else {
			parseReserveTime('');
			loadShopInfo();
			uni.setNavigationBarTitle({
				title: '食堂预约'
			});
		}
		pageInitialized.value = true;
	}

	onLoad((options) => {
		if (!hasToken()) {
			uni.navigateTo({
				url: '/pages/my/login'
			});
			return;
		}
		initPageFromOptions(options || {});
	});

	/** 登录返回后补初始化表单（onLoad 未登录时会跳过） */
	onShow(() => {
		if (!hasToken()) return;
		if (!pageInitialized.value) {
			const pages = getCurrentPages();
			const current = pages[pages.length - 1];
			initPageFromOptions(current?.options || {});
		}
	});
</script>

<style lang="scss" scoped>
	.canteen-reserve-page {
		min-height: 100vh;
		background-color: #FFF9F2;
	}

	.canteen-reserve-body {
		padding: 20rpx var(--ry-page-padding-x) calc(160rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 24rpx;
	}

	.canteen-shop-section {
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
			font-weight: 700;
			color: #333333;
			line-height: 1;
		}
	}

	.canteen-shop-card {
		display: flex;
		flex-direction: column;
		padding: 20rpx;
		overflow: hidden;

		&__body {
			display: flex;
			align-items: flex-start;
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
			margin-top: auto;
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
			font-size: 30rpx;
			font-weight: 700;
			color: #1A1A1A;
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
			border: 1rpx solid #B8EBCE;
			box-sizing: border-box;

			&--off {
				background-color: #F5F5F5;
				border-color: #E8E8E8;

				.canteen-shop-card__status-text {
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
			margin-top: 40rpx;
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
			border: 1rpx solid #FFE8D6;
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
			margin-top: 20rpx;
			padding-top: 18rpx;
			border-top: 1rpx solid #FFF0E6;
		}

		&__action {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;

			&:active {
				opacity: 0.75;
			}
		}

		&__action-divider {
			width: 1rpx;
			height: 32rpx;
			background-color: #FFE8D6;
			flex-shrink: 0;
		}

		&__action-text {
			font-size: 24rpx;
			color: #666666;
			font-weight: 500;
		}
	}

	.canteen-steps {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 8rpx 0 4rpx;
		gap: 0;

		&__item {
			display: flex;
			align-items: center;
			gap: 10rpx;

			&--active {
				.canteen-steps__dot {
					background: linear-gradient(135deg, #FF9A40, #FF7A00);
					color: #FFFFFF;
					box-shadow: 0 4rpx 12rpx rgba(255, 122, 0, 0.35);
				}

				.canteen-steps__label {
					color: #FF7A00;
					font-weight: 700;
				}
			}
		}

		&__dot {
			width: 40rpx;
			height: 40rpx;
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 22rpx;
			font-weight: 700;
			color: #BBBBBB;
			background-color: #FFFFFF;
			border: 1rpx solid #FFE8D6;
		}

		&__label {
			font-size: 24rpx;
			color: #999999;
		}

		&__line {
			width: 80rpx;
			height: 2rpx;
			margin: 0 20rpx;
			background: linear-gradient(90deg, #FFD4B8, #FFE8D6);
		}
	}

	.canteen-section {
		padding: 28rpx;
		display: flex;
		flex-direction: column;
		gap: 24rpx;

		&__head {
			display: flex;
			align-items: center;
			gap: 12rpx;
			padding-bottom: 8rpx;
			border-bottom: 1rpx solid #FFF0E6;
		}

		&__icon {
			width: 48rpx;
			height: 48rpx;
			border-radius: 14rpx;
			background-color: #FFF3EB;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 700;
			color: #333333;
		}
	}

	.canteen-field {
		display: flex;
		flex-direction: column;
		gap: 12rpx;

		&__label {
			font-size: 26rpx;
			font-weight: 500;
			color: #666666;
		}

		&__input-wrap {
			display: flex;
			align-items: center;
			padding: 0 24rpx;
			min-height: 88rpx;
			background-color: #FFF9F2;
			border-radius: var(--ry-radius-lg);
			border: 1rpx solid #FFE8D6;
		}

		&__input {
			flex: 1;
			width: 100%;
			font-size: 28rpx;
			color: #333333;
			line-height: 1.5;
		}

		&__placeholder {
			color: #BBBBBB;
			font-size: 28rpx;
		}

		&__suffix {
			flex-shrink: 0;
			margin-left: 8rpx;
			font-size: 28rpx;
			color: #999999;
		}

		&__picker {
			display: flex;
			align-items: center;
			justify-content: space-between;
			min-height: 88rpx;
			padding: 0 24rpx;
			background-color: #FFF9F2;
			border-radius: var(--ry-radius-lg);
			border: 1rpx solid #FFE8D6;

			&:active {
				background-color: #FFF3EB;
			}
		}

		&__picker-left {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__picker-text {
			flex: 1;
			min-width: 0;
			font-size: 28rpx;
			color: #333333;
			line-height: 1.4;

			&--placeholder {
				color: #BBBBBB;
			}
		}
	}

	.canteen-notice {
		padding: 28rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		border: 1rpx solid #FFE8D6;
		box-shadow: 0 4rpx 16rpx rgba(255, 122, 0, 0.06);

		&__head {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 20rpx;
		}

		&__title {
			font-size: 28rpx;
			font-weight: 700;
			color: #FF7A00;
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

		&__bullet {
			flex-shrink: 0;
			width: 36rpx;
			height: 36rpx;
			border-radius: 50%;
			background-color: #FFF3EB;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__bullet-text {
			font-size: 20rpx;
			font-weight: 700;
			color: #FF7A00;
		}

		&__text {
			flex: 1;
			font-size: 24rpx;
			color: #666666;
			line-height: 1.65;
		}
	}

	.canteen-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding-top: 4rpx;
	}

	.canteen-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 20rpx var(--ry-page-padding-x);
		padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
		background-color: #FFFFFF;
		border-top: 1rpx solid #FFF0E6;
		box-shadow: 0 -8rpx 24rpx rgba(255, 122, 0, 0.08);
		box-sizing: border-box;

		&__records {
			flex-shrink: 0;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			width: 100rpx;
			gap: 4rpx;

			&:active {
				opacity: 0.75;
			}
		}

		&__records-text {
			font-size: 20rpx;
			color: #FF7A00;
			font-weight: 500;
		}

		&__submit {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			height: 88rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #FF9A40, #FF7A00);
			box-shadow: 0 8rpx 24rpx rgba(255, 122, 0, 0.32);

			&:active {
				opacity: 0.9;
			}
		}

		&__submit-text {
			font-size: 30rpx;
			font-weight: 700;
			color: #FFFFFF;
			letter-spacing: 2rpx;
		}
	}

	.canteen-datetime-picker {

		:deep(.u-picker),
		:deep(.u-picker__view) {
			background-color: #FFFFFF;
		}

		:deep(.u-picker__view__column__item) {
			color: #666666;
		}

		:deep(.u-picker__view__column__item--selected) {
			color: #FF7A00;
			font-weight: 600;
		}
	}
</style>
