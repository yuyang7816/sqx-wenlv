<template>
	<view v-if="order" class="mod-page" :style="pageStyle">
		<view class="mod-status" :class="`mod-status--${order.status}`">
			<view class="mod-status__main">
				<text class="mod-status__label">{{ order.statusLabel }}</text>
				<text class="mod-status__type">{{ order.orderTypeLabel }}</text>
			</view>
			<view class="mod-status__no-row">
				<text class="mod-status__no">{{ moui.labelOrderNo }} {{ order.orderNo }}</text>
				<text class="mod-status__copy" @click="copyOrderNo">{{ moui.copyOrderNo }}</text>
			</view>
		</view>

		<view class="mod-body">
			<!-- 商品 / 预订摘要 -->
			<view class="mod-card">
				<view class="mod-product">
					<image :src="order.cover" mode="aspectFill" class="mod-product__cover ry-placeholder-img" />
					<view class="mod-product__info">
						<view class="mod-product__title-row">
							<text class="mod-product__name">{{ order.mainName }}</text>
							<text v-if="order.titleTag" class="mod-product__tag">{{ order.titleTag }}</text>
						</view>
						<text v-if="order.subSpec" class="mod-product__spec">{{ order.subSpec }}</text>
						<text v-if="order.highlightLine" class="mod-product__highlight">{{ order.highlightLine }}</text>
					</view>
				</view>
				<view v-if="bookingRows.length" class="mod-card__divider" />
				<view v-if="bookingRows.length" class="mod-kv-list">
					<view v-for="row in bookingRows" :key="row.label" class="mod-kv">
						<text class="mod-kv__label">{{ row.label }}</text>
						<text class="mod-kv__value">{{ row.value }}</text>
					</view>
				</view>
			</view>

			<!-- 预订人 -->
			<view v-if="showContactCard" class="mod-card">
				<text class="mod-card__title">{{ moui.labelContact }}</text>
				<view class="mod-contact">
					<view class="mod-contact__info">
						<view class="mod-contact__name">{{ order.customerName || '--' }}</view>
						<view class="mod-contact__phone">{{ displayPhone }}</view>
					</view>
					<view v-if="order.customerPhone" class="mod-contact__btn" :style="{ background: theme.primary }"
						@click="callPhone">
						<u-icon name="phone-fill" color="#FFF" :size="14" />
						<text>{{ moui.callPhone }}</text>
					</view>
				</view>
			</view>

			<!-- 核销码（酒店待核销） -->
			<view v-if="showVerifyBlock" class="mod-card mod-card--verify">
				<text class="mod-card__title">{{ moui.labelVerify }}</text>
				<text class="mod-verify__code">{{ order.verifyCode }}</text>
				<text class="mod-verify__hint">{{ moui.labelPendingVerify }}</text>
			</view>

			<!-- 订单信息 -->
			<view class="mod-card">
				<text class="mod-card__title">{{ moui.labelOrderInfo }}</text>
				<view class="mod-kv-list">
					<view v-for="row in metaRows" :key="row.label" class="mod-kv">
						<text class="mod-kv__label">{{ row.label }}</text>
						<text class="mod-kv__value">{{ row.value }}</text>
					</view>
					<view v-if="order.verified" class="mod-kv">
						<text class="mod-kv__label">{{ moui.labelVerifyStatus }}</text>
						<text class="mod-kv__value mod-kv__value--success">{{ moui.labelVerified }}</text>
					</view>
				</view>
			</view>

			<!-- 费用明细 -->
			<view v-if="feeRows.length" class="mod-card">
				<text class="mod-card__title">{{ moui.labelFee }}</text>
				<view class="mod-kv-list">
					<view v-for="row in feeRows" :key="row.label" class="mod-kv">
						<text class="mod-kv__label">{{ row.label }}</text>
						<text class="mod-kv__value" :class="{
								'mod-kv__value--discount': row.tone === 'discount',
								'mod-kv__value--total': row.tone === 'total'
							}">{{ row.value }}</text>
					</view>
				</view>
			</view>

			<!-- 物流信息 -->
			<view v-if="logisticsRows.length || canViewLogistics" class="mod-card">
				<view class="mod-logistics-head">
					<text class="mod-card__title mod-logistics-head__title">{{ moui.labelLogistics }}</text>
					<text v-if="canViewLogistics" class="mod-logistics-head__link" @click="openLogisticsPage">查看物流</text>
				</view>
				<view v-if="logisticsRows.length" class="mod-kv-list">
					<view v-for="row in logisticsRows" :key="row.label" class="mod-kv">
						<text class="mod-kv__label">{{ row.label }}</text>
						<text class="mod-kv__value">{{ row.value }}</text>
					</view>
				</view>
			</view>

			<!-- 用户备注 -->
			<view v-if="order.userRemark" class="mod-card">
				<text class="mod-card__title">{{ moui.labelRemark }}</text>
				<text class="mod-remark">{{ order.userRemark }}</text>
			</view>
		</view>

		<u-popup :show="shipPopupVisible" mode="bottom" :round="24" bg-color="transparent"
			:close-on-click-overlay="!submitting" @close="closeShipPopup">
			<view class="mod-ship-popup">
				<view class="mod-ship-popup__header">
					<text class="mod-ship-popup__title">{{ ui.shipPopupTitle }}</text>
					<view class="mod-ship-popup__close" @click="closeShipPopup">
						<u-icon name="close" :size="18" color="#999" />
					</view>
				</view>
				<view class="mod-ship-popup__field">
					<text class="mod-ship-popup__label">{{ ui.shipCompanyLabel }}</text>
					<input v-model="shipForm.company" class="mod-ship-popup__input" :placeholder="ui.shipCompanyPlaceholder"
						placeholder-class="mod-ship-popup__placeholder" />
				</view>
				<view class="mod-ship-popup__field">
					<text class="mod-ship-popup__label">{{ ui.shipNoLabel }}</text>
					<input v-model="shipForm.trackingNo" class="mod-ship-popup__input" :placeholder="ui.shipNoPlaceholder"
						placeholder-class="mod-ship-popup__placeholder" />
				</view>
				<view class="mod-ship-popup__actions">
					<view class="mod-ship-popup__btn mod-ship-popup__btn--ghost" @click="closeShipPopup">
						<text>{{ ui.shipPopupCancel }}</text>
					</view>
					<view class="mod-ship-popup__btn mod-ship-popup__btn--primary" :style="{ background: theme.primary }"
						@click="submitShip">
						<text>{{ submitting ? ui.shipPopupSubmitting : ui.shipPopupConfirm }}</text>
					</view>
				</view>
			</view>
		</u-popup>

		<view v-if="footerActions.length" class="mod-footer">
			<view class="mod-footer__inner" :class="{ 'mod-footer__inner--multi': footerActions.length > 1 }">
				<u-button v-for="action in footerActions" :key="action.key" :text="action.label"
					:block="footerActions.length === 1" :plain="action.tone !== 'primary'"
					:color="action.tone === 'primary' ? theme.primary : theme.primary"
					:custom-style="action.tone === 'primary' ? btnStyle : btnGhostStyle"
					:loading="submitting && action.tone === 'primary'" @click="onFooterAction(action.key)" />
			</view>
		</view>
	</view>
	<view v-else-if="loading" class="mod-loading">
		<u-loading-icon mode="circle" :color="theme.primary" />
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
		MERCHANT_BUSINESS_UI,
		getOrderPrimaryAction,
		maskPhone,
		requireMerchantDepositPaid
	} from '@/common/merchant-center.js';
	import {
		MERCHANT_ORDER_UI,
		mapMerchantOrder,
		buildBookingRows,
		buildMetaRows,
		buildFeeRows,
		buildLogisticsRows,
		showVerifyCard
	} from '@/common/merchant-order-ui.js';
	import {
		useMerchantTheme
	} from '@/common/useMerchantTheme.js';
	import {
		fetchMerchantOrderDetail,
		postMerchantOrderAction
	} from '@/common/api/merchant.js';
	import {
		canMerchantViewOrderLogistics,
		openOrderLogistics
	} from '@/common/order-ui.js';
	import {
		getMerchantOrderActions,
		runMerchantOrderAction
	} from '@/common/order-actions.js';

	const PACKAGE_STORE_TYPE = 'culture';
const {
		theme,
		pageStyle,
		initTheme,
		applyNavBarColor,
		applyStoreType
	} = useMerchantTheme();
	const ui = MERCHANT_BUSINESS_UI;
	const moui = MERCHANT_ORDER_UI;
	const orderId = ref('');
	const order = ref(null);
	const loading = ref(false);
	const submitting = ref(false);
	const shipPopupVisible = ref(false);
	const autoOpenShip = ref(false);
	const shipForm = ref({
		company: '',
		trackingNo: ''
	});
	const btnStyle = {
		height: '96rpx',
		borderRadius: '48rpx',
		fontWeight: '700',
		fontSize: '30rpx'
	};
	const btnGhostStyle = {
		height: '96rpx',
		borderRadius: '48rpx',
		fontWeight: '600',
		fontSize: '28rpx'
	};

	const primaryAction = computed(() => (order.value ? getOrderPrimaryAction(order.value) : null));
	const footerActions = computed(() => (order.value ? getMerchantOrderActions(order.value) : []));
	const canViewLogistics = computed(() => canMerchantViewOrderLogistics(order.value));
	const bookingRows = computed(() => buildBookingRows(order.value));
	const metaRows = computed(() => buildMetaRows(order.value));
	const feeRows = computed(() => buildFeeRows(order.value));
	const showVerifyBlock = computed(() => showVerifyCard(order.value));
	const showContactCard = computed(() => Boolean(order.value?.customerName || order.value?.customerPhone));
	const logisticsRows = computed(() => buildLogisticsRows(order.value));
	const displayPhone = computed(() => {
		const phone = order.value?.customerPhone;
		if (!phone) return '--';
		return maskPhone(phone);
	});

	async function reload() {
		if (!orderId.value) return;
		loading.value = true;
		try {
			const res = await fetchMerchantOrderDetail(orderId.value);
			if (res.code === 0 && res.data) {
				order.value = mapMerchantOrder(res.data);
			} else {
				order.value = null;
				uni.showToast({
					title: res.msg || ui.loadFail,
					icon: 'none'
				});
			}
		} catch (e) {
			order.value = null;
			uni.showToast({
				title: ui.networkError,
				icon: 'none'
			});
		} finally {
			loading.value = false;
			tryOpenShipPopup();
		}
	}

	function copyOrderNo() {
		if (!order.value?.orderNo) return;
		uni.setClipboardData({
			data: order.value.orderNo,
			success: () => uni.showToast({
				title: moui.copySuccess,
				icon: 'success'
			})
		});
	}

	function callPhone() {
		const phone = order.value?.customerPhone;
		if (!phone) return;
		uni.makePhoneCall({
			phoneNumber: String(phone)
		});
	}

	function openShipPopup() {
		shipForm.value = {
			company: '',
			trackingNo: ''
		};
		shipPopupVisible.value = true;
	}

	function closeShipPopup() {
		if (submitting.value) return;
		shipPopupVisible.value = false;
	}

	async function submitShip() {
		if (submitting.value) return;
		const company = String(shipForm.value.company || '').trim();
		const trackingNo = String(shipForm.value.trackingNo || '').trim();
		if (!company) {
			uni.showToast({ title: ui.shipCompanyRequired, icon: 'none' });
			return;
		}
		if (!trackingNo) {
			uni.showToast({ title: ui.shipNoRequired, icon: 'none' });
			return;
		}
		submitting.value = true;
		try {
			const res = await postMerchantOrderAction(orderId.value, {
				action: 'ship',
				logisticsCompany: company,
				logisticsNo: trackingNo
			});
			if (res.code !== 0) {
				uni.showToast({
					title: res.msg || '操作失败',
					icon: 'none'
				});
				return;
			}
			shipPopupVisible.value = false;
			if (res.data) {
				order.value = mapMerchantOrder(res.data);
			} else {
				await reload();
			}
			uni.showToast({
				title: ui.orderHandled,
				icon: 'success'
			});
		} catch (e) {
			uni.showToast({
				title: ui.networkError,
				icon: 'none'
			});
		} finally {
			submitting.value = false;
		}
	}

	function openLogisticsPage() {
		openOrderLogistics(order.value, { scope: 'merchant' });
	}

	async function onFooterAction(actionKey) {
		if (!actionKey || submitting.value) return;
		if (actionKey === 'ship') {
			openShipPopup();
			return;
		}
		await runMerchantOrderAction(actionKey, order.value, {
			onChanged: async (data) => {
				if (data) {
					order.value = mapMerchantOrder(data);
				} else {
					await reload();
				}
			}
		});
	}

	function tryOpenShipPopup() {
		if (!autoOpenShip.value || !order.value) return;
		autoOpenShip.value = false;
		if (primaryAction.value?.action === 'ship') {
			openShipPopup();
		}
	}

	onLoad(async (options) => {
		orderId.value = options?.id || '';
		autoOpenShip.value = options?.ship === '1';
		applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
		uni.setNavigationBarTitle({
			title: ui.orderDetailTitle
		});
		applyNavBarColor();
	});

	onShow(async () => {
		if (!(await requireMerchantDepositPaid())) return;
		applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
		applyNavBarColor();
		await reload();
	});
</script>

<style lang="scss" scoped>
	.mod-page {
		min-height: 100vh;
		background: #F4F6FA;
		padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
	}

	.mod-loading {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #F4F6FA;
	}

	.mod-status {
		margin: 16rpx var(--ry-page-padding-x) 0;
		padding: 28rpx 28rpx 24rpx;
		border-radius: 24rpx;
		background: linear-gradient(135deg, #5B9BF5, #2979FF);
		color: #FFF;

		&--completed {
			background: linear-gradient(135deg, #3BCB7E, #2EAF6A);
		}

		&--cancelled,
		&--refunding {
			background: linear-gradient(135deg, #FF7B7B, #FF4D4F);
		}

		&--pending_confirm,
		&--pending_use,
		&--pending_checkin,
		&--pending_ship {
			background: linear-gradient(135deg, #FF9A4D, #FF6600);
		}

		&__main {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__label {
			font-size: 36rpx;
			font-weight: 800;
			line-height: 1.3;
		}

		&__type {
			font-size: 22rpx;
			padding: 6rpx 14rpx;
			border-radius: 999rpx;
			background: rgba(255, 255, 255, 0.18);
		}

		&__no-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-top: 16rpx;
		}

		&__no {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: rgba(255, 255, 255, 0.82);
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__copy {
			flex-shrink: 0;
			font-size: 22rpx;
			font-weight: 600;
			color: #FFF;
			padding: 4rpx 12rpx;
			border-radius: 999rpx;
			background: rgba(255, 255, 255, 0.16);
		}
	}

	.mod-body {
		padding: 16rpx var(--ry-page-padding-x) 0;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.mod-card {
		background: #FFF;
		border-radius: 24rpx;
		padding: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.05);

		&--verify {
			background: linear-gradient(180deg, #FFF8F0 0%, #FFFFFF 100%);
			border: 1rpx solid #FFE8D6;
		}

		&__title {
			display: block;
			font-size: 28rpx;
			font-weight: 700;
			color: #1A1A22;
			margin-bottom: 20rpx;
			line-height: 1.3;
		}

		&__divider {
			height: 1rpx;
			margin: 20rpx 0;
			background: #F0F1F5;
		}
	}

	.mod-product {
		display: flex;
		gap: 20rpx;

		&__cover {
			width: 128rpx;
			height: 128rpx;
			border-radius: 16rpx;
			flex-shrink: 0;
			background: #F0F0F0;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__title-row {
			display: flex;
			align-items: flex-start;
			gap: 10rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 30rpx;
			font-weight: 700;
			color: #1A1A22;
			line-height: 1.45;
		}

		&__tag {
			flex-shrink: 0;
			padding: 2rpx 10rpx;
			border-radius: 8rpx;
			background: #F2F3F7;
			font-size: 20rpx;
			color: #8A8A96;
		}

		&__spec {
			font-size: 24rpx;
			color: #9A9AA6;
			line-height: 1.4;
		}

		&__highlight {
			font-size: 24rpx;
			color: #2979FF;
			font-weight: 600;
			line-height: 1.4;
		}
	}

	.mod-kv-list {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.mod-kv {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 24rpx;

		&__label {
			flex-shrink: 0;
			width: 140rpx;
			font-size: 24rpx;
			color: #9A9AA6;
			line-height: 1.5;
		}

		&__value {
			flex: 1;
			min-width: 0;
			font-size: 26rpx;
			color: #1A1A22;
			line-height: 1.5;
			text-align: right;
			word-break: break-all;

			&--discount {
				color: #FF6600;
			}

			&--total {
				color: #FF6600;
				font-size: 32rpx;
				font-weight: 800;
			}

			&--success {
				color: #2EAF6A;
				font-weight: 600;
			}
		}
	}

	.mod-contact {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;

		&__info {
			// flex: 1;
			min-width: 0;
			display: flex;
			// flex-direction: column;
			// gap: 8rpx;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 600;
			color: #1A1A22;
		}

		&__phone {
			font-size: 26rpx;
			color: #36363d;
			margin-left: 20rpx;
		}

		&__btn {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			gap: 8rpx;
			padding: 12rpx 24rpx;
			border-radius: 999rpx;
			font-size: 24rpx;
			font-weight: 600;
			color: #FFF;
		}
	}

	.mod-verify {
		&__code {
			display: block;
			font-size: 48rpx;
			font-weight: 800;
			color: #FF6600;
			letter-spacing: 6rpx;
			line-height: 1.2;
			margin-bottom: 8rpx;
		}

		&__hint {
			font-size: 22rpx;
			color: #9A9AA6;
		}
	}

	.mod-remark {
		display: block;
		font-size: 26rpx;
		color: #4A4A56;
		line-height: 1.65;
	}

	.mod-logistics-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		margin-bottom: 16rpx;

		&__title {
			margin-bottom: 0;
		}

		&__link {
			flex-shrink: 0;
			font-size: 24rpx;
			font-weight: 600;
			color: #2979ff;
		}
	}

	.mod-footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
		background: rgba(255, 255, 255, 0.96);
		backdrop-filter: blur(12rpx);
		box-shadow: 0 -8rpx 32rpx rgba(15, 23, 42, 0.08);

		&__inner--multi {
			display: flex;
			gap: 16rpx;
			align-items: center;

			:deep(.u-button) {
				flex: 1;
				min-width: 0;
			}
		}
	}

	.mod-ship-popup {
		padding: 32rpx var(--ry-page-padding-x) calc(24rpx + env(safe-area-inset-bottom));
		background: #fff;
		border-radius: 24rpx 24rpx 0 0;
		box-sizing: border-box;

		&__header {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-bottom: 28rpx;
		}

		&__title {
			font-size: 32rpx;
			font-weight: 700;
			color: #1A1A22;
		}

		&__close {
			width: 56rpx;
			height: 56rpx;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__field {
			margin-bottom: 24rpx;
		}

		&__label {
			display: block;
			margin-bottom: 12rpx;
			font-size: 26rpx;
			font-weight: 600;
			color: #333;
		}

		&__input {
			width: 100%;
			height: 88rpx;
			padding: 0 24rpx;
			border-radius: 16rpx;
			background: #F5F6F8;
			font-size: 28rpx;
			color: #1A1A22;
			box-sizing: border-box;
		}

		&__placeholder {
			color: #B0B0B8;
		}

		&__actions {
			display: flex;
			gap: 16rpx;
			margin-top: 12rpx;
		}

		&__btn {
			flex: 1;
			height: 88rpx;
			border-radius: 44rpx;
			display: flex;
			align-items: center;
			justify-content: center;

			text {
				font-size: 28rpx;
				font-weight: 600;
			}

			&--ghost {
				background: #F5F6F8;

				text {
					color: #666;
				}
			}

			&--primary text {
				color: #fff;
			}
		}
	}
</style>
