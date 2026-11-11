<template>
	<view class="ry-region-picker">
		<!-- #ifndef H5 -->
		<picker mode="region" :value="nativeValue" @change="onNativeChange">
			<slot>
				<view class="ry-region-picker__default">
					<text
						class="ry-region-picker__text"
						:class="{ 'ry-region-picker__text--placeholder': !displayText }"
					>{{ displayText || placeholder }}</text>
					<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
				</view>
			</slot>
		</picker>
		<!-- #endif -->

		<!-- #ifdef H5 -->
		<view class="ry-region-picker__trigger" @click="openPicker">
			<slot>
				<view class="ry-region-picker__default">
					<text
						class="ry-region-picker__text"
						:class="{ 'ry-region-picker__text--placeholder': !displayText }"
					>{{ displayText || placeholder }}</text>
					<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
				</view>
			</slot>
		</view>
		<u-picker
			:show="showPicker"
			:columns="pickerColumns"
			:default-index="pickerIndexes"
			:confirm-color="confirmColor"
			:mask-style="pickerMaskStyle"
			:round="16"
			bg-color="#FFFFFF"
			:overlay-opacity="0.45"
			@change="onPickerChange"
			@confirm="onPickerConfirm"
			@cancel="closePicker"
			@close="closePicker"
		/>
		<!-- #endif -->
	</view>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import {
	formatRegionText,
	parseRegionValue,
	buildRegionColumns,
	findRegionIndexes,
	getRegionValuesByIndexes
} from '@/common/china-region-lite.js';

const props = defineProps({
	modelValue: {
		type: String,
		default: ''
	},
	placeholder: {
		type: String,
		default: '请选择所在地区'
	},
	confirmColor: {
		type: String,
		default: '#FF6600'
	}
});

const emit = defineEmits(['update:modelValue', 'change']);

const displayText = computed(() => String(props.modelValue || '').trim());
const nativeValue = computed(() => parseRegionValue(displayText.value));

const pickerMaskStyle = 'background-image: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.6)), linear-gradient(0deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.6));';
const showPicker = ref(false);
const pickerColumns = ref(buildRegionColumns());
const pickerIndexes = ref([0, 0, 0]);

watch(displayText, (value) => {
	pickerIndexes.value = findRegionIndexes(parseRegionValue(value));
	pickerColumns.value = buildRegionColumns(pickerIndexes.value[0], pickerIndexes.value[1]);
}, { immediate: true });

function emitRegion(values) {
	const text = formatRegionText(values);
	emit('update:modelValue', text);
	emit('change', text);
}

function onNativeChange(e) {
	emitRegion(e.detail?.value || []);
}

function openPicker() {
	pickerIndexes.value = findRegionIndexes(parseRegionValue(displayText.value));
	pickerColumns.value = buildRegionColumns(pickerIndexes.value[0], pickerIndexes.value[1]);
	showPicker.value = true;
}

function closePicker() {
	showPicker.value = false;
}

function onPickerChange(e) {
	const indexs = e.indexs || [];
	if (e.columnIndex === 0) {
		pickerColumns.value = buildRegionColumns(indexs[0] || 0, 0);
		return;
	}
	if (e.columnIndex === 1) {
		pickerColumns.value = buildRegionColumns(indexs[0] || 0, indexs[1] || 0);
	}
}

function onPickerConfirm(e) {
	const indexs = e.indexs || pickerIndexes.value;
	const values = getRegionValuesByIndexes(indexs[0] || 0, indexs[1] || 0, indexs[2] || 0);
	emitRegion(values);
	closePicker();
}
</script>

<style lang="scss" scoped>
.ry-region-picker {
	width: 100%;
}

.ry-region-picker__trigger,
.ry-region-picker__default {
	width: 100%;
}

.ry-region-picker__default {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12rpx;
}

.ry-region-picker__text {
	flex: 1;
	min-width: 0;
	font-size: 28rpx;
	color: #1a1a1a;
	line-height: 1.5;

	&--placeholder {
		color: #cccccc;
	}
}
</style>
