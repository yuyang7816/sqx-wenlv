<template>
	<view class="rse-page" :style="pageStyle">
		<!-- 基本信息 -->
		<view class="rse-module ry-card">
			<view class="rse-module__head">
				<view class="rse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="edit-pen-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="rse-module__meta">
					<text class="rse-module__title">基本信息</text>
					<text class="rse-module__desc">标准名称与启用状态，便于在房型列表中识别</text>
				</view>
			</view>
			<view class="rse-module__body">
				<view class="rse-field">
					<text class="rse-field__label"><text class="rse-field__req">*</text>标准名称</text>
					<input
						v-model="form.name"
						class="rse-field__input"
						placeholder="如：标准客房、豪华大床房"
						placeholder-class="rse-field__placeholder"
					/>
				</view>
				<view class="rse-field rse-field--last">
					<view class="rse-switch-row">
						<view class="rse-switch-row__text">
							<text class="rse-field__label">启用状态</text>
							<text class="rse-field__hint">停用后新建房型时将不可选择此标准</text>
						</view>
						<u-switch
							v-model="statusEnabled"
							size="20"
							:active-color="theme.primary"
						/>
					</view>
				</view>
			</view>
		</view>

		<HotelRoomFields v-model="roomDetail" variant="standard" layout="split" :theme="theme" />

		<view class="rse-footer">
			<u-button text="保存" :color="theme.primary" :custom-style="btnPrimary" :loading="submitting" @click="onSave" />
		</view>
	</view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { requireMerchantDepositPaid } from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import {
	emptyRoomStandard,
	standardToRoomDetail,
	buildRoomStandardPayload
} from '@/common/merchant-hotel-room.js';
import HotelRoomFields from '@/merchant-hotel/components/hotel-room-fields.vue';
import {
	fetchMerchantRoomStandardDetail,
	saveMerchantRoomStandard,
	ensureUploadedUrl
} from '@/common/api/merchant.js';

const { theme, pageStyle, initTheme, applyNavBarColor } = useMerchantTheme();
const standardId = ref('');
const submitting = ref(false);
const form = reactive(emptyRoomStandard());
const roomDetail = ref(standardToRoomDetail({}));

const statusEnabled = computed({
	get: () => form.status !== 0,
	set: (value) => {
		form.status = value ? 1 : 0;
	}
});

async function loadForm() {
	if (!standardId.value) {
		Object.assign(form, emptyRoomStandard());
		roomDetail.value = standardToRoomDetail({});
		return;
	}
	try {
		const res = await fetchMerchantRoomStandardDetail(standardId.value);
		if (res.code === 0 && res.data) {
			const data = res.data;
			Object.assign(form, {
				id: data.id,
				name: data.name || '',
				floorHint: data.floorHint || '',
				sort: data.sort ?? 0,
				status: data.status ?? 1
			});
			roomDetail.value = standardToRoomDetail(data);
		}
	} catch (e) {
		uni.showToast({ title: '网络异常', icon: 'none' });
	}
}

async function onSave() {
	if (!String(form.name || '').trim()) {
		uni.showToast({ title: '请填写标准名称', icon: 'none' });
		return;
	}
	submitting.value = true;
	try {
		const detail = { ...roomDetail.value };
		const uploadedGallery = [];
		for (const path of detail.gallery || []) {
			uploadedGallery.push(await ensureUploadedUrl(path));
		}
		detail.gallery = uploadedGallery;
		const payload = buildRoomStandardPayload({
			...form,
			...detail
		});
		const res = await saveMerchantRoomStandard(payload);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '保存失败', icon: 'none' });
			return;
		}
		uni.showToast({ title: '保存成功', icon: 'success' });
		setTimeout(() => uni.navigateBack(), 500);
	} catch (e) {
		uni.showToast({ title: e.message || '网络异常', icon: 'none' });
	} finally {
		submitting.value = false;
	}
}

onLoad(async (options) => {
	standardId.value = options?.id || '';
	await initTheme();
	uni.setNavigationBarTitle({ title: standardId.value ? '编辑客房标准' : '新增客房标准' });
	applyNavBarColor();
});

onShow(async () => {
	if (!(await requireMerchantDepositPaid())) return;
	await initTheme();
	applyNavBarColor();
	await loadForm();
});

const btnPrimary = { flex: 1, height: '48px', borderRadius: '24px', fontWeight: '600' };
</script>

<style lang="scss" scoped>
.rse-page {
	min-height: 100vh;
	background: var(--ry-page-bg);
	padding: 20rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.rse-module {
	padding: 28rpx;
	overflow: hidden;

	&__head {
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
		margin-bottom: 24rpx;
	}

	&__icon {
		flex-shrink: 0;
		width: 56rpx;
		height: 56rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__meta {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__title {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		line-height: 1.35;
	}

	&__desc {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.55;
	}

	&__body {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
}

.rse-field {
	display: flex;
	flex-direction: column;
	gap: 12rpx;

	&--last {
		margin-bottom: 0;
	}

	&__label {
		font-size: 26rpx;
		color: var(--ry-text-primary);
		font-weight: 600;
	}

	&__req {
		color: #FF4D4F;
		margin-right: 4rpx;
	}

	&__hint {
		display: block;
		margin-top: 4rpx;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}

	&__input {
		padding: 0 24rpx;
		height: 88rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	&__placeholder {
		color: #B8B8B8;
	}
}

.rse-switch-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 24rpx;
	padding: 20rpx 24rpx;
	border-radius: 16rpx;
	background: #F7F8FC;

	&__text {
		flex: 1;
		min-width: 0;
	}
}

.rse-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255, 255, 255, 0.96);
	box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.04);
}
</style>
