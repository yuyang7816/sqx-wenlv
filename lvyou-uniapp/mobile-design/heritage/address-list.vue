<template>
	<view class="hal-page">
		<view class="hal-body">
			<!-- 空状态 -->
			<view v-if="!loading && !addressList.length" class="hal-empty">
				<view class="hal-empty__icon">
					<u-icon name="map" color="#D7CCC8" :size="64" />
				</view>
				<text class="hal-empty__title">{{ ui.emptyTitle }}</text>
				<text class="hal-empty__hint">{{ ui.emptyHint }}</text>
			</view>

			<!-- 地址列表 -->
			<view v-else class="hal-list">
				<view
					v-for="item in addressList"
					:key="item.id"
					class="hal-card"
					:class="{ 'hal-card--selected': selectedId === String(item.id) }"
				>
					<view class="hal-card__main" @click="selectAddress(item)">
						<view class="hal-card__head">
							<text class="hal-card__name">{{ item.name }}</text>
							<text class="hal-card__phone">{{ maskPhone(item.phone) }}</text>
							<view v-if="item.isDefault" class="hal-card__tag">
								<text class="hal-card__tag-text">{{ ui.tagDefault }}</text>
							</view>
						</view>
						<view class="hal-card__addr-row">
							<view class="hal-card__icon">
								<u-icon name="map-fill" color="#B45D2D" :size="14" />
							</view>
							<text class="hal-card__addr">{{ formatAddress(item) }}</text>
						</view>
					</view>
					<view class="hal-card__actions">
						<view
							v-if="!item.isDefault"
							class="hal-card__action"
							@click.stop="onSetDefault(item)"
						>
							<u-icon name="star" color="#999999" :size="14" />
							<text class="hal-card__action-text">{{ ui.btnSetDefault }}</text>
						</view>
						<view class="hal-card__action" @click.stop="goEdit(item)">
							<u-icon name="edit-pen" color="#999999" :size="14" />
							<text class="hal-card__action-text">{{ ui.btnEdit }}</text>
						</view>
						<view class="hal-card__action hal-card__action--danger" @click.stop="onDelete(item)">
							<u-icon name="trash" color="#E57373" :size="14" />
							<text class="hal-card__action-text hal-card__action-text--danger">{{ ui.btnDelete }}</text>
						</view>
					</view>
				</view>
			</view>

			<text class="hal-footer">{{ ui.footerList }}</text>
		</view>

		<!-- 底部新增 -->
		<view class="hal-bar">
			<view class="hal-bar__btn" @click="goAdd">
				<u-icon name="plus" color="#FFFFFF" :size="18" />
				<text class="hal-bar__btn-text">{{ ui.btnAdd }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { hasToken } from '@/common/api/auth.js';
import { fetchAddressList, saveAddress, deleteAddress as deleteAddressApi, mapAddressItem } from '@/common/api/address.js';
import { getSelectedAddressId, setSelectedAddressId, formatAddress } from '@/common/address-store.js';
import { HERITAGE_ADDRESS_UI, HERITAGE_ADDRESS_FORM_PATH } from '@/common/heritage-address-ui.js';

const ui = HERITAGE_ADDRESS_UI;
const addressList = ref([]);
const selectedId = ref('');
const loading = ref(false);

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: '请先登录', icon: 'none' });
	setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
	return false;
}

function maskPhone(phone) {
	const value = String(phone || '');
	if (value.length !== 11) return value;
	return `${value.slice(0, 3)}****${value.slice(7)}`;
}

async function refreshList() {
	if (!hasToken()) {
		addressList.value = [];
		selectedId.value = '';
		return;
	}
	loading.value = true;
	try {
		const res = await fetchAddressList();
		if (res.code === 0 && Array.isArray(res.data)) {
			addressList.value = res.data.map(mapAddressItem);
			selectedId.value = getSelectedAddressId();
		} else {
			addressList.value = [];
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		addressList.value = [];
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function selectAddress(item) {
	setSelectedAddressId(item.id);
	selectedId.value = String(item.id);
	uni.showToast({ title: ui.toastSelected, icon: 'success' });
	setTimeout(() => uni.navigateBack(), 400);
}

function goAdd() {
	if (!requireLogin()) return;
	uni.navigateTo({ url: HERITAGE_ADDRESS_FORM_PATH });
}

function goEdit(item) {
	if (!requireLogin()) return;
	uni.navigateTo({ url: `${HERITAGE_ADDRESS_FORM_PATH}?id=${item.id}` });
}

async function onSetDefault(item) {
	if (!requireLogin()) return;
	try {
		const res = await saveAddress({
			id: item.id,
			name: item.name,
			phone: item.phone,
			region: item.region,
			detail: item.detail,
			isDefault: true
		});
		if (res.code === 0) {
			setSelectedAddressId(item.id);
			await refreshList();
			uni.showToast({ title: ui.toastDefault, icon: 'success' });
		} else {
			uni.showToast({ title: res.msg || '操作失败', icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	}
}

function onDelete(item) {
	if (!requireLogin()) return;
	uni.showModal({
		title: ui.deleteTitle,
		content: ui.deleteContent,
		confirmText: ui.deleteConfirm,
		cancelText: ui.deleteCancel,
		success: async (res) => {
			if (!res.confirm) return;
			try {
				const apiRes = await deleteAddressApi(item.id);
				if (apiRes.code === 0) {
					if (String(getSelectedAddressId()) === String(item.id)) {
						setSelectedAddressId('');
					}
					await refreshList();
					uni.showToast({ title: ui.toastDeleted, icon: 'success' });
				} else {
					uni.showToast({ title: apiRes.msg || '删除失败', icon: 'none' });
				}
			} catch (e) {
				uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
			}
		}
	});
}

onShow(() => {
	if (!requireLogin()) return;
	refreshList();
});
</script>

<style lang="scss" scoped>
.hal-page {
	min-height: 100vh;
	background-color: #faf6f3;
	padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.hal-body {
	padding: 24rpx var(--ry-page-padding-x) 0;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.hal-empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 80rpx 48rpx 100rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);

	&__icon {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		background-color: #f5f0eb;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 24rpx;
	}

	&__title {
		font-size: 28rpx;
		font-weight: 600;
		color: #8d6e63;
	}

	&__hint {
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #bbbbbb;
		text-align: center;
		line-height: 1.5;
	}
}

.hal-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.hal-card {
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);
	overflow: hidden;
	border: 2rpx solid transparent;

	&--selected {
		border-color: #b45d2d;
		box-shadow: 0 8rpx 28rpx rgba(180, 93, 45, 0.12);
	}

	&__main {
		padding: 24rpx 24rpx 16rpx;

		&:active {
			opacity: 0.92;
		}
	}

	&__head {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-bottom: 12rpx;
	}

	&__name {
		font-size: 30rpx;
		font-weight: 700;
		color: #4a3728;
	}

	&__phone {
		font-size: 28rpx;
		color: #666666;
	}

	&__tag {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 4rpx 12rpx;
		border-radius: 999rpx;
		background-color: #f5e8df;
	}

	&__tag-text {
		font-size: 20rpx;
		color: #b45d2d;
		font-weight: 600;
	}

	&__addr-row {
		display: flex;
		align-items: flex-start;
		gap: 10rpx;
	}

	&__icon {
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background-color: #f5e8df;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		margin-top: 2rpx;
	}

	&__addr {
		flex: 1;
		font-size: 26rpx;
		color: #666666;
		line-height: 1.6;
	}

	&__actions {
		display: flex;
		align-items: center;
		justify-content: flex-end;
		gap: 28rpx;
		padding: 12rpx 24rpx 20rpx;
		border-top: 1rpx solid #f5f0eb;
	}

	&__action {
		display: flex;
		align-items: center;
		gap: 6rpx;

		&:active {
			opacity: 0.7;
		}
	}

	&__action-text {
		font-size: 24rpx;
		color: #999999;

		&--danger {
			color: #e57373;
		}
	}
}

.hal-footer {
	display: block;
	text-align: center;
	font-size: 22rpx;
	color: #cccccc;
	padding: 8rpx 0 16rpx;
}

.hal-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 99;
	padding: 20rpx var(--ry-page-padding-x);
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	background-color: #ffffff;
	border-top: 1rpx solid #f0ebe6;
	box-shadow: 0 -4rpx 16rpx rgba(74, 55, 40, 0.06);

	&__btn {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		height: 80rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #8d6e63, #5d4037);
		box-shadow: 0 8rpx 24rpx rgba(93, 64, 55, 0.3);

		&:active {
			opacity: 0.9;
		}
	}

	&__btn-text {
		font-size: 28rpx;
		font-weight: 700;
		color: #ffffff;
	}
}
</style>
