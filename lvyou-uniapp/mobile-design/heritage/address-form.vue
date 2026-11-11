<template>
	<view class="haf-page">
		<!-- 顶部提示 -->
		<view class="haf-banner">
			<view class="haf-banner__pattern" />
			<view class="haf-banner__content">
				<text class="haf-banner__title">{{ pageTitle }}</text>
				<text class="haf-banner__tip">{{ ui.formTip }}</text>
			</view>
		</view>

		<view class="haf-body">
			<!-- 表单卡片 -->
			<view class="haf-card">
				<view class="haf-field">
					<text class="haf-field__label">{{ ui.labelName }}</text>
					<view class="haf-field__control">
						<u-input
							v-model="form.name"
							:placeholder="ui.placeholderName"
							border="none"
							:clearable="true"
							:color="inputColor"
							:placeholder-style="placeholderStyle"
							:custom-style="inputStyle"
						/>
					</view>
				</view>

				<view class="haf-field">
					<text class="haf-field__label">{{ ui.labelPhone }}</text>
					<view class="haf-field__control">
						<u-input
							v-model="form.phone"
							type="number"
							maxlength="11"
							:placeholder="ui.placeholderPhone"
							border="none"
							:clearable="true"
							:color="inputColor"
							:placeholder-style="placeholderStyle"
							:custom-style="inputStyle"
						/>
					</view>
				</view>

				<ry-region-picker v-model="form.region" :placeholder="ui.placeholderRegion" confirm-color="#B45D2D">
					<view class="haf-field haf-field--picker">
						<text class="haf-field__label">{{ ui.labelRegion }}</text>
						<view class="haf-field__picker">
							<text
								class="haf-field__picker-text"
								:class="{ 'haf-field__picker-text--placeholder': !form.region }"
							>{{ form.region || ui.placeholderRegion }}</text>
							<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
						</view>
					</view>
				</ry-region-picker>

				<view class="haf-field haf-field--textarea">
					<text class="haf-field__label haf-field__label--block">{{ ui.labelDetail }}</text>
					<textarea
						class="haf-field__textarea"
						v-model="form.detail"
						:placeholder="ui.placeholderDetail"
						:maxlength="120"
						auto-height
					/>
				</view>
			</view>

			<!-- 默认地址开关 -->
			<view class="haf-switch-card">
				<view class="haf-switch-card__info">
					<text class="haf-switch-card__title">{{ ui.labelDefault }}</text>
				</view>
				<switch :checked="form.isDefault" color="#B45D2D" @change="onDefaultChange" />
			</view>

			<!-- 编辑模式：删除 -->
			<view v-if="isEdit" class="haf-delete" @click="onDelete">
				<u-icon name="trash" color="#E57373" :size="16" />
				<text class="haf-delete__text">{{ ui.btnDeleteForm }}</text>
			</view>

			<text class="haf-footer">{{ ui.footerForm }}</text>
		</view>

		<!-- 底部保存 -->
		<view class="haf-bar">
			<view class="haf-bar__btn" @click="onSave">
				<text class="haf-bar__btn-text">{{ ui.btnSave }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { hasToken } from '@/common/api/auth.js';
import { fetchAddressList, saveAddress, deleteAddress as deleteAddressApi, mapAddressItem } from '@/common/api/address.js';
import { setSelectedAddressId } from '@/common/address-store.js';
import { HERITAGE_ADDRESS_UI } from '@/common/heritage-address-ui.js';

const ui = HERITAGE_ADDRESS_UI;
const inputColor = '#1a1a1a';
const placeholderStyle = 'color:#BBBBBB;font-size:28rpx;';
const inputStyle = { fontSize: '28rpx', color: inputColor };

const editId = ref('');
const isEdit = computed(() => !!editId.value);
const pageTitle = computed(() => (isEdit.value ? ui.formEditTitle : ui.formAddTitle));

const form = ref({
	name: '',
	phone: '',
	region: '',
	detail: '',
	isDefault: false
});

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: '请先登录', icon: 'none' });
	setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
	return false;
}

function onDefaultChange(e) {
	form.value.isDefault = !!e.detail?.value;
}

function validateForm() {
	if (!form.value.name.trim()) {
		uni.showToast({ title: ui.toastInvalid, icon: 'none' });
		return false;
	}
	if (!/^1\d{10}$/.test(String(form.value.phone))) {
		uni.showToast({ title: ui.toastPhoneInvalid, icon: 'none' });
		return false;
	}
	if (!form.value.region || !form.value.detail.trim()) {
		uni.showToast({ title: ui.toastInvalid, icon: 'none' });
		return false;
	}
	return true;
}

async function onSave() {
	if (!requireLogin() || !validateForm()) return;
	const payload = {
		name: form.value.name.trim(),
		phone: form.value.phone.trim(),
		region: form.value.region,
		detail: form.value.detail.trim(),
		isDefault: form.value.isDefault
	};
	if (isEdit.value) payload.id = editId.value;
	try {
		const res = await saveAddress(payload);
		if (res.code === 0) {
			const savedId = isEdit.value ? editId.value : res.data;
			if (payload.isDefault && savedId) setSelectedAddressId(savedId);
			uni.showToast({ title: ui.toastSaveSuccess, icon: 'success' });
			setTimeout(() => uni.navigateBack(), 500);
		} else {
			uni.showToast({ title: res.msg || '保存失败', icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	}
}

function onDelete() {
	if (!requireLogin()) return;
	uni.showModal({
		title: ui.deleteTitle,
		content: ui.deleteContent,
		confirmText: ui.deleteConfirm,
		cancelText: ui.deleteCancel,
		success: async (res) => {
			if (!res.confirm) return;
			try {
				const apiRes = await deleteAddressApi(editId.value);
				if (apiRes.code === 0) {
					uni.showToast({ title: ui.toastDeleted, icon: 'success' });
					setTimeout(() => uni.navigateBack(), 500);
				} else {
					uni.showToast({ title: apiRes.msg || '删除失败', icon: 'none' });
				}
			} catch (e) {
				uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
			}
		}
	});
}

async function loadAddressForEdit(id) {
	try {
		const res = await fetchAddressList();
		if (res.code === 0 && Array.isArray(res.data)) {
			const found = res.data.map(mapAddressItem).find((item) => String(item.id) === String(id));
			if (found) {
				form.value = {
					name: found.name,
					phone: found.phone,
					region: found.region || '',
					detail: found.detail,
					isDefault: !!found.isDefault
				};
			}
		}
	} catch (e) {
		uni.showToast({ title: '加载地址失败', icon: 'none' });
	}
}

onLoad(async (options) => {
	if (!requireLogin()) return;
	if (options?.id) {
		editId.value = String(options.id);
		await loadAddressForEdit(editId.value);
	}
	uni.setNavigationBarTitle({ title: pageTitle.value });
});
</script>

<style lang="scss" scoped>
.haf-page {
	min-height: 100vh;
	background-color: #faf6f3;
	padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.haf-banner {
	position: relative;
	margin: 20rpx var(--ry-page-padding-x) 0;
	padding: 28rpx 28rpx 24rpx;
	border-radius: 24rpx;
	background: linear-gradient(135deg, #4a3728 0%, #5d4037 50%, #8d6e63 100%);
	overflow: hidden;
	box-shadow: 0 12rpx 40rpx rgba(74, 55, 40, 0.22);

	&__pattern {
		position: absolute;
		inset: 0;
		opacity: 0.08;
		background-image:
			repeating-linear-gradient(45deg, #ffffff 0, #ffffff 1rpx, transparent 1rpx, transparent 16rpx),
			repeating-linear-gradient(-45deg, #ffffff 0, #ffffff 1rpx, transparent 1rpx, transparent 16rpx);
	}

	&__content {
		position: relative;
		z-index: 1;
	}

	&__title {
		display: block;
		font-size: 34rpx;
		font-weight: 800;
		color: #ffffff;
		letter-spacing: 2rpx;
	}

	&__tip {
		display: block;
		margin-top: 10rpx;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.7);
		line-height: 1.5;
	}
}

.haf-body {
	padding: 20rpx var(--ry-page-padding-x) 0;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.haf-card {
	padding: 8rpx 24rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);
}

.haf-field {
	display: flex;
	align-items: center;
	min-height: 96rpx;
	padding: 16rpx 0;
	border-bottom: 1rpx solid #f5f0eb;

	&:last-child {
		border-bottom: none;
	}

	&--picker {
		width: 100%;
	}

	&--textarea {
		align-items: flex-start;
		flex-direction: column;
		gap: 16rpx;
		padding-bottom: 24rpx;
	}

	&__label {
		flex-shrink: 0;
		width: 168rpx;
		font-size: 28rpx;
		color: #4a3728;
		font-weight: 600;

		&--block {
			width: 100%;
		}
	}

	&__control {
		flex: 1;
		min-width: 0;
	}

	&__picker {
		flex: 1;
		min-width: 0;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	&__picker-text {
		flex: 1;
		font-size: 28rpx;
		color: #1a1a1a;
		line-height: 1.5;

		&--placeholder {
			color: #bbbbbb;
		}
	}

	&__textarea {
		width: 100%;
		min-height: 120rpx;
		padding: 16rpx 20rpx;
		border-radius: 12rpx;
		background-color: #faf6f3;
		font-size: 28rpx;
		color: #1a1a1a;
		line-height: 1.6;
		box-sizing: border-box;
	}
}

.haf-switch-card {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 24rpx 28rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);

	&__title {
		font-size: 28rpx;
		font-weight: 600;
		color: #4a3728;
	}
}

.haf-delete {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	padding: 24rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	border: 1rpx solid #ffebee;

	&:active {
		opacity: 0.88;
	}

	&__text {
		font-size: 28rpx;
		color: #e57373;
		font-weight: 600;
	}
}

.haf-footer {
	display: block;
	text-align: center;
	font-size: 22rpx;
	color: #cccccc;
	padding: 8rpx 0 16rpx;
}

.haf-bar {
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

:deep(.u-input__content__field-wrapper__field) {
	color: #1a1a1a !important;
}
</style>
