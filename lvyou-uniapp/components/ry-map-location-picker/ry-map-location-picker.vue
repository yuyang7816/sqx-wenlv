<template>
	<view class="ry-map-location-picker">
		<view class="ry-map-location-picker__summary" @click="openPicker">
			<view class="ry-map-location-picker__icon">
				<u-icon name="map-fill" color="#FF6600" :size="20" />
			</view>
			<view class="ry-map-location-picker__main">
				<text class="ry-map-location-picker__title">{{ hasLocation ? ui.repickOnMap : ui.pickOnMap }}</text>
				<text class="ry-map-location-picker__text" :class="{ 'is-empty': !displayAddress }">
					{{ displayAddress || ui.locationPlaceholder }}
				</text>
				<text v-if="hasLocation" class="ry-map-location-picker__coord">
					{{ latitude }}, {{ longitude }}
				</text>
			</view>
			<u-icon name="arrow-right" color="#B0AFA9" :size="14" />
		</view>
	</view>
</template>

<script setup>
import { computed } from 'vue';
import { MERCHANT_UI } from '@/common/merchant-onboarding.js';
import { applyChooseLocationResult } from '@/common/map-location.js';

const props = defineProps({
	latitude: { type: [Number, String], default: null },
	longitude: { type: [Number, String], default: null },
	storeRegion: { type: String, default: '' },
	storeAddressDetail: { type: String, default: '' },
	storeAddress: { type: String, default: '' },
});

const emit = defineEmits([
	'update:latitude',
	'update:longitude',
	'update:storeRegion',
	'update:storeAddressDetail',
	'update:storeAddress',
	'change',
]);

const ui = MERCHANT_UI;

const hasLocation = computed(() => props.latitude != null && props.longitude != null && props.latitude !== '' && props.longitude !== '');

const displayAddress = computed(() => {
	const detail = String(props.storeAddressDetail || '').trim();
	const full = String(props.storeAddress || '').trim();
	return detail || full;
});

async function openPicker() {
	const options = {};
	if (hasLocation.value) {
		options.latitude = Number(props.latitude);
		options.longitude = Number(props.longitude);
	}
	uni.chooseLocation({
		...options,
		success: async (res) => {
			try {
				const resolved = await applyChooseLocationResult(res);
				emit('update:latitude', resolved.latitude);
				emit('update:longitude', resolved.longitude);
				emit('update:storeRegion', resolved.storeRegion);
				emit('update:storeAddressDetail', resolved.storeAddressDetail);
				emit('update:storeAddress', resolved.storeAddress);
				emit('change', resolved);
			} catch (e) {
				uni.showToast({ title: '位置解析失败', icon: 'none' });
			}
		},
		fail: (err) => {
			const msg = String(err?.errMsg || '');
			if (msg.includes('cancel') || msg.includes('取消')) return;
			uni.showToast({ title: '请在系统设置中开启定位权限', icon: 'none' });
		},
	});
}

defineExpose({
	openPicker,
});
</script>

<style lang="scss" scoped>
.ry-map-location-picker {
	width: 100%;
}

.ry-map-location-picker__summary {
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 24rpx;
	border-radius: 20rpx;
	background: #FAFAF8;
	border: 1rpx solid #EEECE8;
}

.ry-map-location-picker__icon {
	width: 72rpx;
	height: 72rpx;
	border-radius: 18rpx;
	background: #FFF3EB;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.ry-map-location-picker__main {
	flex: 1;
	min-width: 0;
}

.ry-map-location-picker__title {
	display: block;
	font-size: 28rpx;
	font-weight: 600;
	color: #FF6600;
	margin-bottom: 8rpx;
}

.ry-map-location-picker__text {
	display: block;
	font-size: 26rpx;
	line-height: 1.5;
	color: #333333;
	word-break: break-all;

	&.is-empty {
		color: #B0AFA9;
	}
}

.ry-map-location-picker__coord {
	display: block;
	margin-top: 8rpx;
	font-size: 22rpx;
	color: #999999;
}
</style>
