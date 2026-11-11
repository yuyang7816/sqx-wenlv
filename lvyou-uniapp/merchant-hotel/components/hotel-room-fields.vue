<template>
	<view :class="isSplit ? 'hrf-split' : 'hrf'">
		<!-- 房型相册 -->
		<view :class="moduleClass">
			<view v-if="isSplit" class="hrf-module__head">
				<view class="hrf-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="photo-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="hrf-module__meta">
					<text class="hrf-module__title">{{ ui.galleryTitle }}</text>
					<text class="hrf-module__desc">{{ ui.galleryHint }}</text>
				</view>
			</view>
			<view :class="bodyClass">
				<template v-if="!isSplit">
					<text class="hrf-section__title">{{ ui.galleryTitle }}</text>
					<text class="hrf-section__hint">{{ ui.galleryHint }}</text>
				</template>
				<scroll-view scroll-x class="hrf-gallery-scroll" :show-scrollbar="false">
					<view class="hrf-gallery-row">
						<view v-for="(img, index) in local.gallery" :key="index" class="hrf-gallery-item">
							<image :src="img" mode="aspectFill" class="hrf-gallery-item__img" />
							<view class="hrf-gallery-item__del" @click="removeGallery(index)">
								<u-icon name="close" color="#FFF" :size="10" />
							</view>
						</view>
						<view v-if="local.gallery.length < maxGallery" class="hrf-gallery-item hrf-gallery-item--add" @click="addGallery">
							<u-icon name="plus" :color="theme.primary" :size="24" />
							<text>{{ ui.galleryAdd }}</text>
						</view>
					</view>
				</scroll-view>
			</view>
		</view>

		<!-- 默认参数 / 房型参数 -->
		<view v-if="showParams || variant === 'standard'" :class="moduleClass">
			<view v-if="isSplit" class="hrf-module__head">
				<view class="hrf-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="setting-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="hrf-module__meta">
					<text class="hrf-module__title">{{ paramsTitle }}</text>
					<text class="hrf-module__desc">{{ paramsDesc }}</text>
				</view>
			</view>
			<view :class="bodyClass">
				<template v-if="!isSplit">
					<text class="hrf-section__title">{{ paramsTitle }}</text>
				</template>
				<view v-if="showParams" class="hrf-field">
					<text class="hrf-field__label">{{ ui.areaLabel }}</text>
					<input v-model="local.area" class="hrf-field__input" type="digit" placeholder="26" placeholder-class="hrf-field__placeholder" @input="emitChange" />
				</view>
				<view class="hrf-field">
					<text class="hrf-field__label">{{ floorLabel }}</text>
					<input
						v-model="local.floor"
						class="hrf-field__input"
						:placeholder="floorPlaceholder"
						placeholder-class="hrf-field__placeholder"
						@input="emitChange"
					/>
					<text v-if="variant === 'standard'" class="hrf-field__hint">支持 5-9层、5、6、3/8层 等自由填写，列表会自动补「层」</text>
				</view>
				<view v-if="showParams" class="hrf-field">
					<text class="hrf-field__label">{{ ui.occupancyLabel }}</text>
					<input v-model="local.occupancy" class="hrf-field__input" type="number" placeholder="2" placeholder-class="hrf-field__placeholder" @input="emitChange" />
				</view>
				<view class="hrf-field">
					<text class="hrf-field__label">{{ ui.windowLabel }}</text>
					<picker mode="selector" :range="windowOptions" :value="windowIndex" @change="onWindowChange">
						<view class="hrf-field__picker">
							<text :class="{ 'hrf-field__picker-placeholder': !local.windowType }">{{ local.windowType || '请选择' }}</text>
							<u-icon name="arrow-down" color="#999" :size="14" />
						</view>
					</picker>
				</view>
				<view class="hrf-field hrf-field--last">
					<text class="hrf-field__label">{{ ui.smokingLabel }}</text>
					<picker mode="selector" :range="smokingOptions" :value="smokingIndex" @change="onSmokingChange">
						<view class="hrf-field__picker">
							<text :class="{ 'hrf-field__picker-placeholder': !local.smokingPolicy }">{{ local.smokingPolicy || '请选择' }}</text>
							<u-icon name="arrow-down" color="#999" :size="14" />
						</view>
					</picker>
				</view>
			</view>
		</view>

		<!-- 床型信息 -->
		<view v-if="showBed" :class="moduleClass">
			<view v-if="isSplit" class="hrf-module__head">
				<view class="hrf-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="home-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="hrf-module__meta">
					<text class="hrf-module__title">{{ ui.bedTitle }}</text>
					<text class="hrf-module__desc">床型、数量与加床政策</text>
				</view>
			</view>
			<view :class="bodyClass">
				<template v-if="!isSplit">
					<text class="hrf-section__title">{{ ui.bedTitle }}</text>
				</template>
				<view class="hrf-field">
					<text class="hrf-field__label">{{ ui.bedTypeLabel }}</text>
					<picker mode="selector" :range="bedTypeOptions" :value="bedTypeIndex" @change="onBedTypeChange">
						<view class="hrf-field__picker">
							<text :class="{ 'hrf-field__picker-placeholder': !local.bedType }">{{ local.bedType || '请选择' }}</text>
							<u-icon name="arrow-down" color="#999" :size="14" />
						</view>
					</picker>
				</view>
				<view class="hrf-field">
					<text class="hrf-field__label">{{ ui.bedCountLabel }}</text>
					<input v-model="local.bedCount" class="hrf-field__input" type="number" placeholder="2" placeholder-class="hrf-field__placeholder" @input="emitChange" />
				</view>
				<view class="hrf-field">
					<text class="hrf-field__label">{{ ui.bedSizeLabel }}</text>
					<input v-model="local.bedSize" class="hrf-field__input" type="digit" placeholder="1.3" placeholder-class="hrf-field__placeholder" @input="emitChange" />
				</view>
				<view class="hrf-field hrf-field--last">
					<text class="hrf-field__label">{{ ui.extraBedLabel }}</text>
					<input v-model="local.extraBedPolicy" class="hrf-field__input" :placeholder="ui.extraBedPlaceholder" placeholder-class="hrf-field__placeholder" @input="emitChange" />
				</view>
			</view>
		</view>

		<!-- 宠物政策 -->
		<view v-if="showPetPolicy" :class="moduleClass">
			<view v-if="isSplit" class="hrf-module__head">
				<view class="hrf-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="heart-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="hrf-module__meta">
					<text class="hrf-module__title">{{ ui.petPolicyTitle }}</text>
					<text class="hrf-module__desc">{{ petPolicyDesc }}</text>
				</view>
			</view>
			<view :class="bodyClass">
				<template v-if="!isSplit">
					<text class="hrf-section__title">{{ ui.petPolicyTitle }}</text>
				</template>
				<view class="hrf-field hrf-field--last">
					<text class="hrf-field__label">{{ ui.petPolicyLabel }}</text>
					<picker mode="selector" :range="petPolicyOptions" :value="petPolicyIndex" @change="onPetPolicyChange">
						<view class="hrf-field__picker">
							<text :class="{ 'hrf-field__picker-placeholder': !local.petPolicy }">{{ local.petPolicy || ui.petPolicyPlaceholder }}</text>
							<u-icon name="arrow-down" color="#999" :size="14" />
						</view>
					</picker>
				</view>
			</view>
		</view>

		<!-- 房间设施 -->
		<view v-if="showFacilities" :class="moduleClass">
			<view v-if="isSplit" class="hrf-module__head">
				<view class="hrf-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="grid-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="hrf-module__meta">
					<text class="hrf-module__title">{{ ui.facilitiesTitle }}</text>
					<text class="hrf-module__desc">{{ facilitiesDesc }}</text>
				</view>
			</view>
			<view :class="bodyClass">
				<template v-if="!isSplit">
					<text class="hrf-section__title">{{ ui.facilitiesTitle }}</text>
				</template>
				<template v-if="showSplitFacilities">
					<view v-if="inheritedFacilities.length" class="hrf-facility-group">
						<text class="hrf-tag-group__label">{{ ui.facilitiesFromStandardLabel }}</text>
						<view class="hrf-list">
							<view v-for="item in inheritedFacilities" :key="'std-fac-' + item.id" class="hrf-list-item hrf-list-item--standard">
								<view class="hrf-list-item__icon" :style="{ background: '#EEF0F4' }">
									<u-icon :name="item.icon" color="#8C95A6" :size="14" />
								</view>
								<text class="hrf-tag__badge">标准</text>
								<text class="hrf-list-item__label hrf-list-item__label--standard">{{ item.label }}</text>
							</view>
						</view>
					</view>
					<view v-if="ownFacilities.length" class="hrf-facility-group">
						<text class="hrf-tag-group__label">{{ ui.facilitiesOwnLabel }}</text>
						<view class="hrf-list">
							<view v-for="item in ownFacilities" :key="'own-fac-' + item.id" class="hrf-list-item">
								<view class="hrf-list-item__icon" :style="{ background: theme.primaryLight }">
									<u-icon :name="item.icon" :color="theme.primary" :size="14" />
								</view>
								<text class="hrf-list-item__label">{{ item.label }}</text>
								<view class="hrf-list-item__del" @click="removeFacilityLabel(item.label)">
									<u-icon name="close" color="#999" :size="12" />
								</view>
							</view>
						</view>
					</view>
				</template>
				<view v-else-if="local.facilities.length" class="hrf-list">
					<view v-for="(item, index) in local.facilities" :key="item.id" class="hrf-list-item">
						<view class="hrf-list-item__icon" :style="{ background: theme.primaryLight }">
							<u-icon :name="item.icon" :color="theme.primary" :size="14" />
						</view>
						<text class="hrf-list-item__label">{{ item.label }}</text>
						<view class="hrf-list-item__del" @click="removeFacility(index)">
							<u-icon name="close" color="#999" :size="12" />
						</view>
					</view>
				</view>
				<view class="hrf-add-row">
					<input v-model="facilityInput" class="hrf-field__input hrf-add-row__input" placeholder="如：免费WiFi" placeholder-class="hrf-field__placeholder" maxlength="20" @confirm="addFacility" />
					<view class="hrf-add-row__btn" :style="{ background: theme.primary }" @click="addFacility">
						<text>添加</text>
					</view>
				</view>
				<view v-if="availableFacilityPresets.length" class="hrf-presets">
					<view v-for="preset in availableFacilityPresets" :key="preset" class="hrf-preset" @click="addPresetFacility(preset)">
						<u-icon name="plus" :color="theme.primary" :size="10" />
						<text>{{ preset }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 卖点标签 -->
		<view :class="[moduleClass, !isSplit && 'hrf-section--last']">
			<view v-if="isSplit" class="hrf-module__head">
				<view class="hrf-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="star-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="hrf-module__meta">
					<text class="hrf-module__title">{{ ui.tagsTitle }}</text>
					<text class="hrf-module__desc">{{ ui.tagsHint }}</text>
				</view>
			</view>
			<view :class="bodyClass">
				<template v-if="!isSplit">
					<text class="hrf-section__title">{{ ui.tagsTitle }}</text>
					<text class="hrf-section__hint">{{ ui.tagsHint }}</text>
				</template>
				<template v-if="showSplitTags">
					<view v-if="inheritedTags.length" class="hrf-tag-group">
						<text class="hrf-tag-group__label">{{ ui.tagsFromStandardLabel }}</text>
						<view class="hrf-tags">
							<view v-for="tag in inheritedTags" :key="'std-' + tag" class="hrf-tag hrf-tag--standard">
								<text class="hrf-tag__badge">标准</text>
								<text class="hrf-tag__text">{{ tag }}</text>
							</view>
						</view>
					</view>
					<view v-if="ownTags.length" class="hrf-tag-group">
						<text class="hrf-tag-group__label">{{ ui.tagsOwnLabel }}</text>
						<view class="hrf-tags">
							<view
								v-for="tag in ownTags"
								:key="'own-' + tag"
								class="hrf-tag"
								:style="{ color: theme.primary, background: theme.primaryLight }"
							>
								<text>{{ tag }}</text>
								<u-icon name="close" :color="theme.primary" :size="10" @click="removeTag(tag)" />
							</view>
						</view>
					</view>
				</template>
				<view v-else-if="local.tags.length" class="hrf-tags">
					<view
						v-for="tag in local.tags"
						:key="tag"
						class="hrf-tag"
						:style="{ color: theme.primary, background: theme.primaryLight }"
					>
						<text>{{ tag }}</text>
						<u-icon name="close" :color="theme.primary" :size="10" @click="removeTag(tag)" />
					</view>
				</view>
				<view class="hrf-add-row">
					<input v-model="tagInput" class="hrf-field__input hrf-add-row__input" placeholder="输入卖点标签" placeholder-class="hrf-field__placeholder" maxlength="12" @confirm="addTag" />
					<view class="hrf-add-row__btn" :style="{ background: theme.primary }" @click="addTag">
						<text>{{ ui.addTag }}</text>
					</view>
				</view>
				<view v-if="availableTagPresets.length" class="hrf-presets">
					<view v-for="preset in availableTagPresets" :key="preset" class="hrf-preset" @click="addPresetTag(preset)">
						<u-icon name="plus" :color="theme.primary" :size="10" />
						<text>{{ preset }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import {
	HOTEL_ROOM_UI,
	HOTEL_BED_TYPE_OPTIONS,
	HOTEL_WINDOW_OPTIONS,
	HOTEL_SMOKING_OPTIONS,
	HOTEL_ROOM_TAG_PRESETS,
	HOTEL_FACILITY_PRESETS,
	HOTEL_PET_POLICY_OPTIONS,
	normalizeRoomDetail,
	splitTagsByStandard,
	splitFacilitiesByStandard,
	resolveFacilityIcon
} from '@/common/merchant-hotel-room.js';

const props = defineProps({
	modelValue: { type: Object, default: () => ({}) },
	theme: { type: Object, default: () => ({ primary: '#2979FF', primaryLight: '#EAF2FF' }) },
	/** full=完整房型 | standard=客房标准模板 | override=继承标准时的差异项 */
	variant: { type: String, default: 'full' },
	/** default=单卡片内分组 | split=每模块独立卡片 */
	layout: { type: String, default: 'default' },
	/** 继承模式下传入客房标准标签，用于区分展示 */
	standardTags: { type: Array, default: () => [] },
	/** 继承模式下传入客房标准设施，用于区分展示 */
	standardFacilities: { type: Array, default: () => [] }
});

const showParams = computed(() => props.variant === 'full' || props.variant === 'override');
const showBed = computed(() => props.variant === 'full' || props.variant === 'override');
const showPetPolicy = computed(() => props.variant !== 'override' || true);
const showFacilities = computed(() => props.variant === 'full' || props.variant === 'standard');
const floorLabel = computed(() => (props.variant === 'standard' ? '适用楼层' : ui.floorLabel));
const floorPlaceholder = computed(() =>
	props.variant === 'standard' ? '如：5-9层、5、6' : '5-9层'
);
const isSplit = computed(() => props.layout === 'split');
const moduleClass = computed(() => (isSplit.value ? 'hrf-module ry-card' : 'hrf-section'));
const bodyClass = computed(() => (isSplit.value ? 'hrf-module__body' : ''));
const paramsTitle = computed(() => (props.variant === 'standard' ? '默认参数' : ui.paramsTitle));
const paramsDesc = computed(() =>
	props.variant === 'standard'
		? '窗户、吸烟、适用楼层等默认值，新建房型时自动带入'
		: '面积、楼层、可住人数等基础信息'
);
const petPolicyDesc = computed(() =>
	props.variant === 'standard'
		? '新建房型时将继承此宠物政策'
		: ui.petPolicyDesc
);
const petPolicyOptions = HOTEL_PET_POLICY_OPTIONS;
const petPolicyIndex = computed(() => Math.max(0, petPolicyOptions.indexOf(local.petPolicy)));
const facilitiesDesc = computed(() =>
	props.variant === 'standard'
		? '勾选常用设施，新建房型时自动继承'
		: '勾选或添加本房型提供的设施，可自由修改'
);

const emit = defineEmits(['update:modelValue']);

const ui = HOTEL_ROOM_UI;
const maxGallery = 9;
const local = reactive(normalizeRoomDetail(props.modelValue));
const facilityInput = ref('');
const tagInput = ref('');

const bedTypeOptions = HOTEL_BED_TYPE_OPTIONS;
const windowOptions = HOTEL_WINDOW_OPTIONS;
const smokingOptions = HOTEL_SMOKING_OPTIONS;

const bedTypeIndex = computed(() => Math.max(0, bedTypeOptions.indexOf(local.bedType)));
const windowIndex = computed(() => Math.max(0, windowOptions.indexOf(local.windowType)));
const smokingIndex = computed(() => Math.max(0, smokingOptions.indexOf(local.smokingPolicy)));

const availableFacilityPresets = computed(() => {
	const existing = new Set(local.facilities.map((item) => item.label));
	if (showSplitFacilities.value) {
		for (const item of props.standardFacilities) {
			existing.add(String(item?.label || '').trim());
		}
	}
	return HOTEL_FACILITY_PRESETS.filter((label) => !existing.has(label));
});

const availableTagPresets = computed(() => {
	const existing = new Set(local.tags);
	return HOTEL_ROOM_TAG_PRESETS.filter((label) => !existing.has(label));
});

const showSplitTags = computed(() => props.standardTags.length > 0);
const tagGroups = computed(() => splitTagsByStandard(local.tags, props.standardTags));
const inheritedTags = computed(() => tagGroups.value.fromStandard);
const ownTags = computed(() => tagGroups.value.own);

const showSplitFacilities = computed(() => props.standardFacilities.length > 0);
const facilityGroups = computed(() => splitFacilitiesByStandard(local.facilities, props.standardFacilities));
const inheritedFacilities = computed(() => facilityGroups.value.fromStandard);
const ownFacilities = computed(() => facilityGroups.value.own);

watch(
	() => props.modelValue,
	(value) => {
		Object.assign(local, normalizeRoomDetail(value));
	},
	{ deep: true, immediate: true }
);

function emitChange() {
	emit('update:modelValue', normalizeRoomDetail({ ...local }));
}

function onBedTypeChange(event) {
	const index = Number(event?.detail?.value ?? 0);
	local.bedType = bedTypeOptions[index] || '';
	emitChange();
}

function onWindowChange(event) {
	const index = Number(event?.detail?.value ?? 0);
	local.windowType = windowOptions[index] || '';
	emitChange();
}

function onSmokingChange(event) {
	const index = Number(event?.detail?.value ?? 0);
	local.smokingPolicy = smokingOptions[index] || '';
	emitChange();
}

function onPetPolicyChange(event) {
	const index = Number(event?.detail?.value ?? 0);
	local.petPolicy = petPolicyOptions[index] || '';
	emitChange();
}

function addGallery() {
	const remain = maxGallery - local.gallery.length;
	if (remain <= 0) return;
	uni.chooseImage({
		count: remain,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			local.gallery.push(...(res.tempFilePaths || []));
			emitChange();
		}
	});
}

function removeGallery(index) {
	local.gallery.splice(index, 1);
	emitChange();
}

function addFacilityLabel(label) {
	const normalized = String(label || '').trim();
	if (!normalized) {
		uni.showToast({ title: '请输入设施名称', icon: 'none' });
		return false;
	}
	if (local.facilities.some((item) => item.label === normalized)) {
		uni.showToast({ title: '该设施已添加', icon: 'none' });
		return false;
	}
	local.facilities.push({
		id: `${Date.now()}-${local.facilities.length}`,
		label: normalized,
		icon: resolveFacilityIcon(normalized)
	});
	return true;
}

function addFacility() {
	if (addFacilityLabel(facilityInput.value)) {
		facilityInput.value = '';
		emitChange();
	}
}

function addPresetFacility(label) {
	if (addFacilityLabel(label)) {
		emitChange();
	}
}

function removeFacility(index) {
	local.facilities.splice(index, 1);
	emitChange();
}

function removeFacilityLabel(label) {
	const normalized = String(label || '').trim();
	const index = local.facilities.findIndex((item) => item.label === normalized);
	if (index < 0) return;
	local.facilities.splice(index, 1);
	emitChange();
}

function addTagLabel(label) {
	const normalized = String(label || '').trim();
	if (!normalized) {
		uni.showToast({ title: ui.addTagEmpty, icon: 'none' });
		return false;
	}
	if (local.tags.includes(normalized)) {
		uni.showToast({ title: ui.addTagDuplicate, icon: 'none' });
		return false;
	}
	local.tags.push(normalized);
	return true;
}

function addTag() {
	if (addTagLabel(tagInput.value)) {
		tagInput.value = '';
		emitChange();
	}
}

function addPresetTag(label) {
	if (addTagLabel(label)) {
		emitChange();
	}
}

function removeTag(tag) {
	const index = local.tags.indexOf(tag);
	if (index < 0) return;
	local.tags.splice(index, 1);
	emitChange();
}
</script>

<style lang="scss" scoped>
.hrf-split {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.hrf-module {
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
	}
}

.hrf-section {
	margin-bottom: 28rpx;

	&--last {
		margin-bottom: 0;
	}

	&__title {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		margin-bottom: 8rpx;
	}

	&__hint {
		display: block;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		margin-bottom: 16rpx;
		line-height: 1.5;
	}
}

.hrf-field {
	margin-bottom: 20rpx;

	&--last {
		margin-bottom: 0;
	}

	&__label {
		display: block;
		font-size: 26rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		margin-bottom: 10rpx;
	}

	&__input {
		width: 100%;
		height: 84rpx;
		padding: 0 24rpx;
		box-sizing: border-box;
		border-radius: 16rpx;
		background: #F7F8FC;
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	&__picker {
		height: 84rpx;
		padding: 0 24rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	&__picker-placeholder {
		color: #B0B8C8;
	}

	&__placeholder {
		color: #B0B8C8;
	}

	&__hint {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}
}

.hrf-switch-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
	padding: 4rpx 0;

	.hrf-field--last & {
		margin-bottom: 0;
	}
}

.hrf-gallery-scroll {
	width: 100%;
	white-space: nowrap;
}

.hrf-gallery-row {
	display: inline-flex;
	gap: 16rpx;
	padding: 4rpx 0;
}

.hrf-gallery-item {
	position: relative;
	flex-shrink: 0;
	width: 200rpx;
	height: 150rpx;
	border-radius: 16rpx;
	overflow: hidden;

	&__img {
		width: 100%;
		height: 100%;
		background: #EEF0F4;
	}

	&__del {
		position: absolute;
		top: 8rpx;
		right: 8rpx;
		width: 32rpx;
		height: 32rpx;
		border-radius: 50%;
		background: rgba(0, 0, 0, 0.45);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&--add {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		background: #F7F8FC;
		border: 3rpx dashed #E8ECF4;
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}
}

.hrf-list {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	margin-bottom: 16rpx;
}

.hrf-facility-group {
	margin-bottom: 16rpx;

	&:last-child {
		margin-bottom: 0;
	}
}

.hrf-list-item {
	display: flex;
	align-items: center;
	gap: 12rpx;
	padding: 16rpx 20rpx;
	border-radius: 16rpx;
	background: #F7F8FC;

	&--standard {
		background: #F3F5F8;
	}

	&__icon {
		width: 44rpx;
		height: 44rpx;
		border-radius: 12rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__label {
		flex: 1;
		font-size: 26rpx;
		color: var(--ry-text-primary);

		&--standard {
			color: #667085;
		}
	}

	&__del {
		padding: 8rpx;
	}
}

.hrf-add-row {
	display: flex;
	align-items: center;
	gap: 12rpx;

	&__input {
		flex: 1;
		min-width: 0;
	}

	&__btn {
		flex-shrink: 0;
		height: 76rpx;
		padding: 0 28rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;

		text {
			font-size: 26rpx;
			font-weight: 600;
			color: #FFF;
		}
	}
}

.hrf-presets {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-top: 16rpx;
}

.hrf-preset {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	padding: 8rpx 16rpx;
	border-radius: 999rpx;
	background: #FFF;
	border: 1rpx solid #E8ECF4;
	font-size: 22rpx;
	color: var(--ry-text-secondary);
}

.hrf-tag-group {
	margin-bottom: 16rpx;

	&:last-child {
		margin-bottom: 0;
	}

	&__label {
		display: block;
		margin-bottom: 10rpx;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
	}
}

.hrf-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.hrf-tag-group .hrf-tags {
	margin-bottom: 0;
}

.hrf-tag {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	padding: 8rpx 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;

	&--standard {
		padding: 6rpx 14rpx 6rpx 8rpx;
		background: #F3F5F8;
		color: #8C95A6;
	}

	&__badge {
		flex-shrink: 0;
		padding: 2rpx 10rpx;
		border-radius: 999rpx;
		font-size: 18rpx;
		font-weight: 600;
		line-height: 1.4;
		color: #7A8699;
		background: #E8ECF2;
	}

	&__text {
		font-size: 22rpx;
		color: #667085;
	}
}
</style>
