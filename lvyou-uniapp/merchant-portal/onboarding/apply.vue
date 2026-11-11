<template>
	<view class="apply-page">
		<view class="apply-hero">
			<view class="apply-hero__circle apply-hero__circle--1" />
			<view class="apply-hero__circle apply-hero__circle--2" />
			<view class="apply-hero__content">
				<text class="apply-hero__badge">商户入驻</text>
				<text class="apply-hero__title">加入荣e游商家</text>
				<text class="apply-hero__sub">填写真实资料，审核通过后即可经营</text>
			</view>
			<view class="apply-steps">
				<view v-for="(step, index) in steps" :key="step.key" class="apply-steps__item">
					<view class="apply-steps__node" :class="{ 'apply-steps__node--active': index <= activeStep }">
						<text>{{ index + 1 }}</text>
					</view>
					<text class="apply-steps__label"
						:class="{ 'apply-steps__label--active': index <= activeStep }">{{ step.label }}</text>
					<view v-if="index < steps.length - 1" class="apply-steps__line"
						:class="{ 'apply-steps__line--active': index < activeStep }" />
				</view>
			</view>
		</view>

		<scroll-view scroll-y class="apply-scroll" :scroll-into-view="scrollIntoView" scroll-with-animation>
			<view v-if="rejectedRemark" class="apply-reject ry-card">
				<view class="apply-reject__head">
					<u-icon name="close-circle-fill" color="#FF4D4F" :size="18" />
					<text class="apply-reject__title">{{ ui.rejectedBannerTitle }}</text>
				</view>
				<text class="apply-reject__hint">{{ ui.rejectedBannerHint }}</text>
				<view class="apply-reject__reason">
					<text class="apply-reject__reason-label">{{ ui.rejectedReasonTitle }}</text>
					<text class="apply-reject__reason-text">{{ rejectedRemark }}</text>
				</view>
			</view>

			<view class="apply-notice" @click="openQualTip">
				<view class="apply-notice__icon">
					<u-icon name="info-circle-fill" color="#FF6600" :size="18" />
				</view>
				<view class="apply-notice__main">
					<text class="apply-notice__text">请如实填写资料，仅需上传基础经营材料即可</text>
					<text class="apply-notice__link">{{ ui.viewQualification }}</text>
				</view>
				<u-icon name="arrow-right" color="#FF6600" :size="14" />
			</view>

			<!-- Step 1: 联系人 -->
			<view id="section-contact" class="apply-block ry-card">
				<view class="apply-block__head">
					<view class="apply-block__index">01</view>
					<view class="apply-block__titles">
						<text class="apply-block__title">{{ ui.contactSection }}</text>
						<text class="apply-block__sub">用于平台联系与审核通知</text>
					</view>
				</view>
				<view class="apply-form">
					<view class="apply-form__item">
						<text class="apply-form__label"><text class="apply-form__req">*</text>{{ ui.contactName }}</text>
						<view class="apply-form__input-wrap">
							<u-icon name="account-fill" color="#B0AFA9" :size="18" />
							<input v-model="form.contactName" class="apply-form__input" :placeholder="'请输入' + ui.contactName"
								placeholder-class="apply-form__placeholder" />
						</view>
					</view>
					<view class="apply-form__item apply-form__item--last">
						<text class="apply-form__label"><text class="apply-form__req">*</text>{{ ui.contactPhone }}</text>
						<view class="apply-form__input-wrap">
							<u-icon name="phone-fill" color="#B0AFA9" :size="18" />
							<input v-model="form.contactPhone" class="apply-form__input" type="number" maxlength="11"
								:placeholder="'请输入' + ui.contactPhone" placeholder-class="apply-form__placeholder" />
						</view>
					</view>
				</view>
			</view>

			<!-- Step 2: 门店信息 -->
			<view id="section-store" class="apply-block ry-card">
				<view class="apply-block__head">
					<view class="apply-block__index apply-block__index--blue">02</view>
					<view class="apply-block__titles">
						<text class="apply-block__title">{{ ui.storeSection }}</text>
						<text class="apply-block__sub">门店基本信息与经营类型</text>
					</view>
				</view>
				<view class="apply-form">
					<view class="apply-form__item">
						<text class="apply-form__label"><text class="apply-form__req">*</text>{{ ui.storeName }}</text>
						<view class="apply-form__input-wrap">
							<u-icon name="home-fill" color="#B0AFA9" :size="18" />
							<input v-model="form.storeName" class="apply-form__input" :placeholder="'请输入' + ui.storeName"
								placeholder-class="apply-form__placeholder" />
						</view>
					</view>
					<view class="apply-form__item">
						<text class="apply-form__label apply-form__label--block"><text class="apply-form__req">*</text>{{ ui.storeLocation }}</text>
						<ry-map-location-picker
							v-model:latitude="form.latitude"
							v-model:longitude="form.longitude"
							v-model:store-region="form.storeRegion"
							v-model:store-address-detail="form.storeAddressDetail"
							v-model:store-address="form.storeAddress"
							@change="onLocationChange"
						/>
					</view>
					<view class="apply-form__item">
						<text class="apply-form__label"><text class="apply-form__req">*</text>{{ ui.storeAddressDetail }}</text>
						<view class="apply-form__input-wrap">
							<u-icon name="edit-pen-fill" color="#B0AFA9" :size="18" />
							<input v-model="form.storeAddressDetail" class="apply-form__input"
								:placeholder="'地图选点后可微调' + ui.storeAddressDetail" placeholder-class="apply-form__placeholder"
								@input="onAddressDetailInput" />
						</view>
					</view>
					<view class="apply-form__item">
						<text class="apply-form__label">{{ ui.storeHours }}</text>
						<view class="apply-form__input-wrap apply-form__input-wrap--hours">
							<u-icon name="clock-fill" color="#B0AFA9" :size="18" />
							<text class="apply-form__hours-val" @click.stop="openOpenTimePicker">{{ storeOpenTime || '开始时间' }}</text>
							<text class="apply-form__hours-sep">至</text>
							<text class="apply-form__hours-val"
								@click.stop="openCloseTimePicker">{{ storeCloseTime || '结束时间' }}</text>
						</view>
					</view>
				</view>

				<view class="apply-form__item" :class="{ 'apply-form__item--last': form.storeType !== 'culture' }">
					<text class="apply-form__label apply-form__label--block"><text
							class="apply-form__req">*</text>{{ ui.storeType }}</text>
					<view class="apply-form__picker" @click="showTypePicker = true">
						<view v-if="selectedTypeMeta" class="apply-form__picker-value">
							<view class="apply-form__picker-icon" :style="{ backgroundColor: selectedTypeMeta.bgColor }">
								<u-icon :name="selectedTypeMeta.icon" :color="selectedTypeMeta.color" :size="20" />
							</view>
							<text class="apply-form__picker-label">{{ selectedTypeMeta.label }}</text>
						</view>
						<text v-else class="apply-form__picker-placeholder">请选择门店类型</text>
						<u-icon name="arrow-right" color="#B0AFA9" :size="14" />
					</view>
					<view v-if="currentQual.note" class="apply-type-alert">
						<u-icon name="info-circle" color="#FF6600" :size="14" />
						<text>{{ currentQual.note }}</text>
					</view>
					<view v-if="selectedTypeMeta?.depositAmount != null" class="apply-type-deposit">
						<text>审核通过后需缴纳保证金 ¥{{ formatDepositAmount(selectedTypeMeta.depositAmount) }}</text>
					</view>
				</view>
				<view v-if="form.storeType === 'culture'" class="apply-form__item apply-form__item--last">
					<text class="apply-form__label apply-form__label--block"><text
							class="apply-form__req">*</text>{{ ui.shopCategory }}</text>
					<view class="apply-form__picker" @click="openShopCategoryPicker">
						<view v-if="form.shopCategoryLabel" class="apply-form__picker-value">
							<text class="apply-form__picker-label">{{ form.shopCategoryLabel }}</text>
						</view>
						<text v-else class="apply-form__picker-placeholder">{{ ui.shopCategoryPlaceholder }}</text>
						<u-icon name="arrow-right" color="#B0AFA9" :size="14" />
					</view>
				</view>
			</view>

			<!-- Step 3: 营业执照 + 法人 -->
			<view id="section-license" class="apply-block ry-card">
				<view class="apply-block__head">
					<view class="apply-block__index apply-block__index--green">03</view>
					<view class="apply-block__titles">
						<text class="apply-block__title">{{ ui.licenseSection }} · {{ ui.legalSection }}</text>
						<text class="apply-block__sub">上传证照原件清晰照片</text>
					</view>
				</view>
				<view class="apply-form">
					<view class="apply-form__item apply-form__item--last">
						<text class="apply-form__label"><text class="apply-form__req">*</text>{{ ui.licenseNo }}</text>
						<view class="apply-form__input-wrap">
							<u-icon name="file-text-fill" color="#B0AFA9" :size="18" />
							<input v-model="form.licenseNo" class="apply-form__input" :placeholder="'请输入' + ui.licenseNo"
								placeholder-class="apply-form__placeholder" />
						</view>
					</view>
				</view>

				<view class="apply-upload-card apply-upload-card--license" style="margin-top: 20rpx;" @click="pickImage('licenseImage')">
					<image v-if="form.licenseImage" :src="mediaSrc(form.licenseImage)" mode="aspectFill"
						referrerpolicy="no-referrer" class="apply-upload-card__img" />
					<view v-else class="apply-upload-card__empty apply-upload-card__empty--wide">
						<view class="apply-upload-card__icon-wrap">
							<u-icon name="file-text-fill" color="#FF6600" :size="32" />
						</view>
						<text class="apply-upload-card__title">{{ ui.licenseImage }}</text>
						<text class="apply-upload-card__hint">点击上传营业执照照片</text>
					</view>
					<view v-if="form.licenseImage" class="apply-upload-card__mask apply-upload-card__mask--sm">
						<u-icon name="camera-fill" color="#FFFFFF" :size="24" />
						<text>更换照片</text>
					</view>
				</view>

				<view class="apply-form apply-form--gap">
					<view class="apply-form__item">
						<text class="apply-form__label"><text class="apply-form__req">*</text>{{ ui.legalName }}</text>
						<view class="apply-form__input-wrap">
							<input v-model="form.legalName" class="apply-form__input apply-form__input--plain"
								:placeholder="'请输入' + ui.legalName" placeholder-class="apply-form__placeholder" />
						</view>
					</view>
					<view class="apply-form__item apply-form__item--last">
						<text class="apply-form__label"><text class="apply-form__req">*</text>{{ ui.legalIdNo }}</text>
						<view class="apply-form__input-wrap">
							<input v-model="form.legalIdNo" class="apply-form__input apply-form__input--plain" maxlength="18"
								:placeholder="'请输入' + ui.legalIdNo" placeholder-class="apply-form__placeholder" />
						</view>
					</view>
				</view>

				<view class="apply-id-grid">
					<view class="apply-id-item" @click="pickImage('legalIdFront')">
						<text class="apply-id-item__label"><text class="apply-form__req">*</text>{{ ui.legalIdFront }}</text>
						<view class="apply-upload-card apply-upload-card--id">
							<image v-if="form.legalIdFront" :src="mediaSrc(form.legalIdFront)" mode="aspectFill"
								class="apply-upload-card__img" />
							<view v-else class="apply-upload-card__empty">
								<u-icon name="camera-fill" color="#2979FF" :size="28" />
								<text>{{ ui.uploadImage }}</text>
							</view>
							<view v-if="form.legalIdFront" class="apply-upload-card__mask apply-upload-card__mask--sm">
								<u-icon name="camera-fill" color="#FFFFFF" :size="18" />
							</view>
						</view>
					</view>
					<view class="apply-id-item" @click="pickImage('legalIdBack')">
						<text class="apply-id-item__label"><text class="apply-form__req">*</text>{{ ui.legalIdBack }}</text>
						<view class="apply-upload-card apply-upload-card--id">
							<image v-if="form.legalIdBack" :src="mediaSrc(form.legalIdBack)" mode="aspectFill" class="apply-upload-card__img" />
							<view v-else class="apply-upload-card__empty">
								<u-icon name="camera-fill" color="#2979FF" :size="28" />
								<text>{{ ui.uploadImage }}</text>
							</view>
							<view v-if="form.legalIdBack" class="apply-upload-card__mask apply-upload-card__mask--sm">
								<u-icon name="camera-fill" color="#FFFFFF" :size="18" />
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- Step 4: 行业资质 -->
			<view v-if="form.storeType" id="section-qual" class="apply-block ry-card">
				<view class="apply-block__head">
					<view class="apply-block__index apply-block__index--purple">04</view>
					<view class="apply-block__titles">
						<text class="apply-block__title">{{ ui.qualSection }}</text>
						<text class="apply-block__sub">{{ currentQual.title }}</text>
					</view>
				</view>

				<view class="apply-qual-list">
					<view v-for="(item, index) in qualList" :key="item.key" class="apply-qual-item"
						:class="{ 'apply-qual-item--multi': isQualMultiItem(item) }">
						<view class="apply-qual-item__main">
							<view class="apply-qual-item__info">
								<view class="apply-qual-item__head">
									<text v-if="item.required" class="apply-qual-item__tag apply-qual-item__tag--req">必传</text>
									<text v-else class="apply-qual-item__tag">选传</text>
									<text class="apply-qual-item__name">{{ item.label }}</text>
									<text v-if="isQualMultiItem(item)" class="apply-qual-item__count">
										{{ qualImageList(item.key).length }}/{{ getQualMaxCount(item) }}
									</text>
								</view>
								<text class="apply-qual-item__desc">{{ item.desc }}</text>
							</view>

							<view v-if="!isQualMultiItem(item)" class="apply-qual-item__upload" @click="pickQualImage(item.key)">
								<image v-if="form.qualImages[item.key]" :src="mediaSrc(form.qualImages[item.key])" mode="aspectFill"
									class="apply-qual-item__img" />
								<view v-else class="apply-qual-item__empty">
									<u-icon name="plus" color="#FF6600" :size="20" />
								</view>
								<view v-if="form.qualImages[item.key]" class="apply-qual-item__done">
									<u-icon name="checkmark" color="#FFFFFF" :size="12" />
								</view>
							</view>
						</view>

						<view v-if="isQualMultiItem(item)" class="apply-qual-multi">
							<view v-for="(url, imgIndex) in qualImageList(item.key)" :key="`${item.key}-${imgIndex}`"
								class="apply-qual-multi__item">
								<image :src="mediaSrc(url)" mode="aspectFill" class="apply-qual-multi__img"
									@click="previewQualImages(item.key, imgIndex)" />
								<view class="apply-qual-multi__del" @click.stop="removeQualImage(item.key, imgIndex)">
									<u-icon name="close" color="#FFFFFF" :size="12" />
								</view>
							</view>
							<view v-if="canAddQualImage(item)" class="apply-qual-multi__add" @click="pickQualImages(item)">
								<u-icon name="plus" color="#FF6600" :size="22" />
								<text>添加</text>
							</view>
						</view>

						<view v-if="index < qualList.length - 1" class="apply-qual-item__line" />
					</view>
				</view>
			</view>

			<view v-else class="apply-block apply-block--placeholder ry-card">
				<u-icon name="arrow-up-fill" color="#B0AFA9" :size="20" />
				<text>请先选择门店类型，再上传行业资质</text>
			</view>
		</scroll-view>

		<view class="apply-footer">
			<view class="apply-footer__safe">
				<u-icon name="lock-fill" color="#B0AFA9" :size="14" />
				<text>您的资料仅用于入驻审核，平台严格保密</text>
			</view>
			<u-button :text="submitButtonText" block color="#FF6600" :custom-style="btnStyle" :loading="submitting"
				@click="onSubmit" />
		</view>

		<u-datetime-picker v-if="showOpenTimePicker" v-model:show="showOpenTimePicker" v-model="openTimePickerValue"
			mode="time" :round="16" :mask-style="pickerMaskStyle" confirm-color="#FF6600" @confirm="onOpenTimeConfirm"
			@cancel="showOpenTimePicker = false" />
		<u-datetime-picker v-if="showCloseTimePicker" v-model:show="showCloseTimePicker" v-model="closeTimePickerValue"
			mode="time" :round="16" :mask-style="pickerMaskStyle" confirm-color="#FF6600" @confirm="onCloseTimeConfirm"
			@cancel="showCloseTimePicker = false" />

		<u-popup :show="showTypePicker" mode="bottom" round="20" bg-color="#FFFFFF" :overlay-opacity="0.45"
			@close="showTypePicker = false">
			<view class="apply-type-popup">
				<view class="apply-popup__handle" />
				<text class="apply-type-popup__title">选择门店类型</text>
				<text class="apply-type-popup__sub">请选择与经营业务匹配的类型</text>
				<view class="apply-type-popup__list">
					<view v-for="item in storeTypes" :key="item.key" class="apply-type-popup__item"
						:class="{ 'apply-type-popup__item--active': form.storeType === item.key }" @click="onSelectType(item)">
						<view class="apply-type-popup__icon" :style="{ backgroundColor: item.bgColor }">
							<u-icon :name="item.icon" :color="item.color" :size="24" />
						</view>
						<view class="apply-type-popup__info">
							<text class="apply-type-popup__name">{{ item.label }}</text>
							<text v-if="item.depositAmount != null" class="apply-type-popup__deposit">
								保证金 ¥{{ formatDepositAmount(item.depositAmount) }}
							</text>
							<text v-if="item.desc" class="apply-type-popup__desc">{{ item.desc }}</text>
						</view>
						<u-icon :name="form.storeType === item.key ? 'checkmark-circle-fill' : 'checkmark-circle'"
							:color="form.storeType === item.key ? '#FF6600' : '#DDDDDD'" :size="22" />
					</view>
				</view>
			</view>
		</u-popup>

		<u-popup :show="showShopCategoryPicker" mode="bottom" round="20" bg-color="#FFFFFF" :overlay-opacity="0.45"
			@close="showShopCategoryPicker = false">
			<view class="apply-type-popup">
				<view class="apply-popup__handle" />
				<text class="apply-type-popup__title">{{ ui.shopCategory }}</text>
				<text class="apply-type-popup__sub">{{ ui.shopCategoryHint }}</text>
				<scroll-view scroll-y class="apply-craft-popup__scroll">
					<view v-if="shopCategoryLoading" class="apply-craft-popup__loading">
						<u-loading-icon mode="circle" color="#FF6600" />
						<text>加载中...</text>
					</view>
					<view v-else-if="!shopCategoryOptions.length" class="apply-craft-popup__empty">
						<text>暂无可选好店分类</text>
					</view>
					<view v-else class="apply-type-popup__list">
						<view v-for="item in shopCategoryOptions" :key="item.code" class="apply-type-popup__item"
							:class="{ 'apply-type-popup__item--active': form.shopCategory === item.code }"
							@click="onSelectShopCategory(item)">
							<view class="apply-type-popup__info">
								<text class="apply-type-popup__name">{{ item.name }}</text>
							</view>
							<u-icon :name="form.shopCategory === item.code ? 'checkmark-circle-fill' : 'checkmark-circle'"
								:color="form.shopCategory === item.code ? '#FF6600' : '#DDDDDD'" :size="22" />
						</view>
					</view>
				</scroll-view>
			</view>
		</u-popup>

		<u-popup :show="showQualTip" mode="bottom" round="20" bg-color="#FFFFFF" :overlay-opacity="0.45"
			@close="showQualTip = false">
			<view class="apply-popup">
				<view class="apply-popup__handle" />
				<text class="apply-popup__title">{{ currentQual.title || '入驻资质要求' }}</text>
				<text class="apply-popup__sub">以下为{{ form.storeType ? '当前类型' : '各类型' }}所需资质材料</text>
				<scroll-view scroll-y class="apply-popup__scroll">
					<view v-for="item in popupQualList" :key="item.key" class="apply-popup__item">
						<view class="apply-popup__item-head">
							<text class="apply-popup__tag"
								:class="{ 'apply-popup__tag--req': item.required }">{{ item.required ? '必传' : '选传' }}</text>
							<text class="apply-popup__name">{{ item.label }}</text>
						</view>
						<text class="apply-popup__desc">{{ item.desc }}</text>
					</view>
				</scroll-view>
				<u-button text="知道了" block color="#FF6600" :custom-style="btnStyle" @click="showQualTip = false" />
			</view>
		</u-popup>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive,
		computed
	} from 'vue';
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app';
	import {
		parseRegionFromAddress
	} from '@/common/map-location.js';
	import {
		MERCHANT_UI,
		getEmptyApplyForm,
		getQualificationsByType,
		getStoreTypeMeta,
		formatStoreAddress,
		validateApplyForm,
		prefillContactFromUser,
		mapStatusToApplyForm,
		MERCHANT_STATUS,
		formatDepositAmount,
		mapStoreTypesFromDictItems,
		mergeStoreTypesFromDict,
		setCachedStoreTypes,
		isQualMultiItem,
		getQualMaxCount,
		getQualImageList,
		isQualItemFilled,
		serializeQualImagesForSubmit
	} from '@/common/merchant-onboarding.js';
	import { DICT_TYPES, loadDictItems } from '@/common/dict-util.js';
	import {
		fetchMerchantStatus,
		fetchMerchantStoreTypes,
		submitMerchantApply,
		ensureUploadedUrl,
		requireMerchantLogin
	} from '@/common/api/merchant.js';
	import {
		resolvePublicUrl
	} from '@/common/media-url.js';
	import {
		fetchShopCategoryOptions
	} from '@/common/api/heritage.js';

	const ui = MERCHANT_UI;
	const storeTypes = ref([]);
	const form = reactive(getEmptyApplyForm());
	const rejectedRemark = ref('');
	const isResubmit = ref(false);
	const rejectedFormLoaded = ref(false);
	const submitting = ref(false);
	const uploading = ref(false);
	const showQualTip = ref(false);
	const showTypePicker = ref(false);
	const showShopCategoryPicker = ref(false);
	const shopCategoryOptions = ref([]);
	const shopCategoryLoading = ref(false);
	const showOpenTimePicker = ref(false);
	const showCloseTimePicker = ref(false);
	const scrollIntoView = ref('');
	const btnStyle = {
		height: '48px',
		borderRadius: '24px',
		fontWeight: '600',
		background: 'linear-gradient(135deg, #FF6600 0%, #FF8533 100%)',
		border: 'none',
		boxShadow: '0 8px 24px rgba(255, 102, 0, 0.28)'
	};
	const pickerMaskStyle =
		'background-image: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.6)), linear-gradient(0deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.6));';

	function parseStoreHours(hours) {
		const [open = '09:00', close = '21:00'] = String(hours || '09:00-21:00').split('-');
		return {
			open: open.trim(),
			close: close.trim()
		};
	}

	function normalizeTimeString(timeStr, fallback = '09:00') {
		const matched = String(timeStr || '').trim().match(/^(\d{1,2}):(\d{2})$/);
		if (!matched) return fallback;
		const hour = Math.min(23, Math.max(0, Number(matched[1])));
		const minute = Math.min(59, Math.max(0, Number(matched[2])));
		return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
	}

	function formatTimeValue(value) {
		if (typeof value === 'string') return normalizeTimeString(value);
		const d = new Date(value);
		if (Number.isNaN(d.getTime())) return '09:00';
		return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
	}

	const storeOpenTime = ref('');
	const storeCloseTime = ref('');
	const openTimePickerValue = ref('09:00');
	const closeTimePickerValue = ref('21:00');

	function initStoreHoursFromForm() {
		const {
			open,
			close
		} = parseStoreHours(form.storeHours);
		storeOpenTime.value = normalizeTimeString(open, '09:00');
		storeCloseTime.value = normalizeTimeString(close, '21:00');
		openTimePickerValue.value = storeOpenTime.value;
		closeTimePickerValue.value = storeCloseTime.value;
	}

	function syncStoreHours() {
		form.storeHours = `${storeOpenTime.value}-${storeCloseTime.value}`;
	}

	initStoreHoursFromForm();

	function openOpenTimePicker() {
		showOpenTimePicker.value = true;
	}

	function openCloseTimePicker() {
		showCloseTimePicker.value = true;
	}

	function onOpenTimeConfirm(e) {
		storeOpenTime.value = formatTimeValue(e.value);
		syncStoreHours();
		showOpenTimePicker.value = false;
	}

	function onCloseTimeConfirm(e) {
		storeCloseTime.value = formatTimeValue(e.value);
		syncStoreHours();
		showCloseTimePicker.value = false;
	}

	const selectedTypeMeta = computed(() => (form.storeType ? getStoreTypeMeta(form.storeType) : null));
	const submitButtonText = computed(() => (isResubmit.value ? ui.resubmitApply : ui.submitApply));

	const steps = [{
			key: 'contact',
			label: '联系人'
		},
		{
			key: 'store',
			label: '门店'
		},
		{
			key: 'license',
			label: '证照'
		},
		{
			key: 'qual',
			label: '资质'
		}
	];

	const currentQual = computed(() => (form.storeType ? getQualificationsByType(form.storeType) : {
		title: '',
		required: [],
		note: ''
	}));
	const qualList = computed(() => currentQual.value.required.filter((item) => !['license', 'legalId'].includes(item
		.key)));
	const popupQualList = computed(() => {
		if (!form.storeType) return [];
		return currentQual.value.required.filter((item) => !['license', 'legalId'].includes(item.key));
	});

	const activeStep = computed(() => {
		if (form.storeType && qualList.value.every((item) => !item.required || isQualItemFilled(form.qualImages, item))) return 3;
		if (form.licenseImage && form.legalName && form.legalIdNo && form.legalIdFront && form.legalIdBack) return 2;
		const craftReady = form.storeType !== 'culture' || Boolean(form.shopCategory);
		if (form.storeName && form.storeAddressDetail && form.storeType && craftReady && form.latitude != null && form.longitude != null) return 1;
		if (form.contactName && form.contactPhone) return 0;
		return 0;
	});

	function onTypeChange(key) {
		if (form.storeType === key) return;
		form.storeType = key;
		form.qualImages = {};
		if (key !== 'culture') {
			form.shopCategory = '';
			form.shopCategoryLabel = '';
		}
		setTimeout(() => {
			scrollIntoView.value = 'section-qual';
		}, 100);
	}

	async function loadShopCategoryOptions() {
		if (shopCategoryLoading.value) return;
		shopCategoryLoading.value = true;
		try {
			const res = await fetchShopCategoryOptions();
			if (res.code !== 0) {
				shopCategoryOptions.value = [];
				uni.showToast({ title: res.msg || '加载好店分类失败', icon: 'none' });
				return;
			}
			shopCategoryOptions.value = Array.isArray(res.data) ? res.data : [];
		} catch (e) {
			shopCategoryOptions.value = [];
			uni.showToast({ title: '加载好店分类失败', icon: 'none' });
		} finally {
			shopCategoryLoading.value = false;
		}
	}

	async function openShopCategoryPicker() {
		if (form.storeType !== 'culture') return;
		showShopCategoryPicker.value = true;
		if (!shopCategoryOptions.value.length) {
			await loadShopCategoryOptions();
		}
	}

	function onSelectShopCategory(item) {
		form.shopCategory = item?.code || '';
		form.shopCategoryLabel = item?.name || '';
		showShopCategoryPicker.value = false;
	}

	function onSelectType(item) {
		onTypeChange(item.key);
		showTypePicker.value = false;
	}

	function onLocationChange(payload) {
		if (payload?.storeAddress) {
			form.storeAddress = payload.storeAddress;
			return;
		}
		form.storeAddress = formatStoreAddress(form);
	}

	function onAddressDetailInput() {
		form.storeAddress = formatStoreAddress(form);
	}

	function mediaSrc(url) {
		return resolvePublicUrl(url);
	}

	function openQualTip() {
		if (!form.storeType) {
			uni.showToast({
				title: '请先选择门店类型',
				icon: 'none'
			});
			return;
		}
		showQualTip.value = true;
	}

	async function uploadPickedImage(setter) {
		if (uploading.value) return;
		uni.chooseImage({
			count: 1,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: async (res) => {
				const tempPath = res.tempFilePaths[0];
				if (!tempPath) return;
				uploading.value = true;
				uni.showLoading({ title: '上传中...', mask: true });
				try {
					const url = resolvePublicUrl(await ensureUploadedUrl(tempPath));
					setter(url);
				} catch (e) {
					uni.showToast({ title: MERCHANT_UI.uploadFail, icon: 'none' });
				} finally {
					uploading.value = false;
					uni.hideLoading();
				}
			}
		});
	}

	function pickImage(field) {
		uploadPickedImage((url) => {
			form[field] = url;
		});
	}

	function qualImageList(key) {
		return getQualImageList(form.qualImages, key);
	}

	function canAddQualImage(item) {
		return qualImageList(item.key).length < getQualMaxCount(item);
	}

	function pickQualImage(key) {
		uploadPickedImage((url) => {
			form.qualImages[key] = url;
		});
	}

	function pickQualImages(item) {
		if (uploading.value) return;
		const current = qualImageList(item.key);
		const remain = getQualMaxCount(item) - current.length;
		if (remain <= 0) return;
		uni.chooseImage({
			count: Math.min(remain, 9),
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: async (res) => {
				const paths = res.tempFilePaths || [];
				if (!paths.length) return;
				uploading.value = true;
				uni.showLoading({ title: '上传中...', mask: true });
				try {
					const next = [...current];
					for (const tempPath of paths) {
						if (next.length >= getQualMaxCount(item)) break;
						const url = resolvePublicUrl(await ensureUploadedUrl(tempPath));
						next.push(url);
					}
					form.qualImages[item.key] = next;
				} catch (e) {
					uni.showToast({ title: MERCHANT_UI.uploadFail, icon: 'none' });
				} finally {
					uploading.value = false;
					uni.hideLoading();
				}
			}
		});
	}

	function removeQualImage(key, index) {
		const item = qualList.value.find((entry) => entry.key === key);
		const next = qualImageList(key).filter((_, idx) => idx !== index);
		if (!next.length) {
			delete form.qualImages[key];
			return;
		}
		form.qualImages[key] = item && isQualMultiItem(item) ? next : next[0];
	}

	function previewQualImages(key, index) {
		const urls = qualImageList(key).map((url) => mediaSrc(url));
		if (!urls.length) return;
		uni.previewImage({ urls, current: urls[index] || urls[0] });
	}

	async function onSubmit() {
		if (submitting.value || uploading.value) return;
		if (!form.storeRegion) {
			form.storeRegion = parseRegionFromAddress(form.storeAddress || form.storeAddressDetail);
		}
		form.storeAddress = formatStoreAddress(form);
		const check = validateApplyForm(form);
		if (!check.ok) {
			uni.showToast({ title: check.message, icon: 'none' });
			return;
		}
		submitting.value = true;
		uni.showLoading({ title: '提交中...', mask: true });
		try {
			const normalizedQualImages = {};
			for (const [key, value] of Object.entries(form.qualImages || {})) {
				const urls = getQualImageList({ [key]: value }, key);
				if (!urls.length) continue;
				const uploaded = [];
				for (const url of urls) {
					uploaded.push(await ensureUploadedUrl(url));
				}
				const item = qualList.value.find((entry) => entry.key === key);
				normalizedQualImages[key] = item && isQualMultiItem(item) ? uploaded : uploaded[0];
			}
			const qualImages = serializeQualImagesForSubmit(normalizedQualImages, form.storeType);
			const payload = {
				contactName: form.contactName,
				contactPhone: form.contactPhone,
				storeName: form.storeName,
				storeRegion: form.storeRegion,
				storeAddressDetail: form.storeAddressDetail,
				storeAddress: form.storeAddress,
				latitude: form.latitude,
				longitude: form.longitude,
				storeHours: form.storeHours,
				storeType: form.storeType,
				shopCategory: form.storeType === 'culture' && form.shopCategory ? form.shopCategory : undefined,
				licenseNo: form.licenseNo,
				licenseImage: await ensureUploadedUrl(form.licenseImage),
				legalName: form.legalName,
				legalIdNo: form.legalIdNo,
				legalIdFront: await ensureUploadedUrl(form.legalIdFront),
				legalIdBack: await ensureUploadedUrl(form.legalIdBack),
				qualImages
			};
			const res = await submitMerchantApply(payload);
			if (res.code !== 0) {
				uni.showToast({ title: res.msg || MERCHANT_UI.submitFail, icon: 'none' });
				return;
			}
			uni.showToast({ title: MERCHANT_UI.submitSuccess, icon: 'success' });
			setTimeout(() => {
				uni.redirectTo({ url: '/merchant-portal/onboarding/result' });
			}, 400);
		} catch (e) {
			uni.showToast({ title: e.message || MERCHANT_UI.submitFail, icon: 'none' });
		} finally {
			submitting.value = false;
			uni.hideLoading();
		}
	}

	async function loadStoreTypes() {
		try {
			const res = await fetchMerchantStoreTypes();
			if (res.code === 0 && Array.isArray(res.data) && res.data.length) {
				const mapped = res.data.map((item) => ({
					code: item.code,
					label: item.label,
					depositAmount: item.depositAmount,
					sort: item.sortOrder
				}));
				storeTypes.value = mapStoreTypesFromDictItems(mapped);
				setCachedStoreTypes(storeTypes.value);
				return;
			}
		} catch (e) {
			// 回退字典接口
		}
		const items = await loadDictItems(DICT_TYPES.MERCHANT_TYPE);
		storeTypes.value = mergeStoreTypesFromDict(items);
	}

	function setApplyNavBar() {
		try {
			uni.setNavigationBarTitle({ title: ui.applyTitle });
			uni.setNavigationBarColor({
				frontColor: '#ffffff',
				backgroundColor: '#1C1C1E',
				animation: { duration: 0, timingFunc: 'easeIn' }
			});
		} catch (e) { /* H5 分包页可能尚未就绪 */ }
	}

	onLoad(async () => {
		await loadStoreTypes();
	});

	function applyStatusToForm(statusData) {
		Object.assign(form, mapStatusToApplyForm(statusData));
		initStoreHoursFromForm();
	}

	onShow(async () => {
		setApplyNavBar();
		if (!requireMerchantLogin()) return;
		try {
			const res = await fetchMerchantStatus();
			if (res.code === 0 && res.data) {
				const status = res.data.status;
				if (status === MERCHANT_STATUS.APPROVED) {
					uni.redirectTo({ url: '/merchant-portal/center/index' });
					return;
				}
				if (status === MERCHANT_STATUS.PENDING) {
					uni.redirectTo({ url: '/merchant-portal/onboarding/result' });
					return;
				}
				if (status === MERCHANT_STATUS.REJECTED) {
					isResubmit.value = true;
					rejectedRemark.value = res.data.auditRemark || '';
					if (!rejectedFormLoaded.value) {
						applyStatusToForm(res.data);
						rejectedFormLoaded.value = true;
					}
					return;
				}
			}
		} catch (e) { /* ignore */ }
		if (!form.contactName && !form.storeName) {
			const prefilled = await prefillContactFromUser(getEmptyApplyForm());
			Object.assign(form, prefilled);
			initStoreHoursFromForm();
		}
	});
</script>

<style lang="scss" scoped>
	.apply-page {
		min-height: 100vh;
		background: linear-gradient(180deg, #FAFAF8 0%, #F3F2EF 100%);
		display: flex;
		flex-direction: column;

		.ry-card {
			background: #FFFFFF;
			border-radius: 24rpx;
			box-shadow: none;
		}

		:deep(.u-popup__content) {
			background-color: #ffffff !important;
		}

		:deep(.u-overlay) {
			background-color: rgba(0, 0, 0, 0.35) !important;
		}

		:deep(.u-picker) {
			background-color: #ffffff;
		}

		:deep(.u-toolbar) {
			background-color: #ffffff;
		}

		:deep(.u-picker__view__column__item) {
			color: #999 !important;
		}

		:deep(.u-picker__view__column__item--selected) {
			color: #1a1a1a !important;
		}
	}

	/* ── Hero ── */
	.apply-hero {
		position: relative;
		overflow: hidden;
		padding: 36rpx var(--ry-page-padding-x) 32rpx;
		background: linear-gradient(155deg, #1C1C1E 0%, #2C2C2E 45%, #3A3A3C 100%);

		&__circle {
			position: absolute;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.06);
		}

		&__circle--1 {
			width: 280rpx;
			height: 280rpx;
			top: -90rpx;
			right: -60rpx;
		}

		&__circle--2 {
			width: 160rpx;
			height: 160rpx;
			bottom: -30rpx;
			left: -40rpx;
		}

		&::after {
			content: '';
			position: absolute;
			top: -40rpx;
			right: 80rpx;
			width: 200rpx;
			height: 200rpx;
			border-radius: 50%;
			background: radial-gradient(circle, rgba(255, 102, 0, 0.35) 0%, transparent 70%);
			pointer-events: none;
		}

		&__content {
			position: relative;
			z-index: 1;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
			margin-bottom: 28rpx;
		}

		&__badge {
			align-self: flex-start;
			padding: 6rpx 20rpx;
			border-radius: var(--ry-radius-pill);
			background: rgba(255, 102, 0, 0.18);
			font-size: 22rpx;
			color: #FFB380;
			font-weight: 500;
			letter-spacing: 1rpx;
		}

		&__title {
			font-size: 40rpx;
			font-weight: 700;
			color: #FFFFFF;
			line-height: 1.3;
		}

		&__sub {
			font-size: 26rpx;
			color: rgba(255, 255, 255, 0.55);
		}
	}

	/* ── Steps ── */
	.apply-steps {
		position: relative;
		z-index: 1;
		display: flex;
		justify-content: space-between;
		padding: 20rpx 8rpx 0;

		&__item {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: center;
			position: relative;
			gap: 10rpx;
		}

		&__node {
			width: 44rpx;
			height: 44rpx;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.08);
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 22rpx;
			color: rgba(255, 255, 255, 0.45);
			font-weight: 600;
			z-index: 1;
			transition: all 0.25s ease;

			&--active {
				background: linear-gradient(135deg, #FF6600, #FF8533);
				color: #FFFFFF;
				box-shadow: 0 4rpx 16rpx rgba(255, 102, 0, 0.4);
			}
		}

		&__label {
			font-size: 20rpx;
			color: rgba(255, 255, 255, 0.38);

			&--active {
				color: rgba(255, 255, 255, 0.92);
				font-weight: 600;
			}
		}

		&__line {
			position: absolute;
			top: 22rpx;
			left: calc(50% + 22rpx);
			width: calc(100% - 44rpx);
			height: 2rpx;
			background: rgba(255, 255, 255, 0.1);

			&--active {
				background: rgba(255, 102, 0, 0.5);
			}
		}
	}

	/* ── Scroll body ── */
	.apply-scroll {
		flex: 1;
		margin-top: -20rpx;
		padding: 8rpx var(--ry-page-padding-x) calc(180rpx + env(safe-area-inset-bottom));
		box-sizing: border-box;
	}

	.apply-reject {
		padding: 24rpx 28rpx;
		margin-bottom: 20rpx;
		border: 1rpx solid rgba(255, 77, 79, 0.18);
		background: linear-gradient(180deg, #FFF7F7 0%, #FFFFFF 100%);

		&__head {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 10rpx;
		}

		&__title {
			font-size: 28rpx;
			font-weight: 600;
			color: #FF4D4F;
		}

		&__hint {
			display: block;
			font-size: 24rpx;
			color: var(--ry-text-secondary);
			line-height: 1.5;
		}

		&__reason {
			margin-top: 16rpx;
			padding: 16rpx 20rpx;
			border-radius: 16rpx;
			background: rgba(255, 77, 79, 0.06);
		}

		&__reason-label {
			display: block;
			font-size: 22rpx;
			color: #FF7875;
			margin-bottom: 6rpx;
		}

		&__reason-text {
			display: block;
			font-size: 26rpx;
			color: #CF1322;
			line-height: 1.6;
			word-break: break-all;
		}
	}

	.apply-notice {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 24rpx 28rpx;
		margin-bottom: 20rpx;
		border-radius: var(--ry-radius-lg);
		background: rgba(255, 255, 255, 0.92);
		box-shadow: 0 2rpx 20rpx rgba(0, 0, 0, 0.04);
		backdrop-filter: blur(12px);

		&:active {
			opacity: 0.92;
		}

		&__icon {
			width: 56rpx;
			height: 56rpx;
			border-radius: 16rpx;
			background: linear-gradient(135deg, #FFF3EB, #FFE8D6);
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__main {
			flex: 1;
			min-width: 0;
		}

		&__text {
			display: block;
			font-size: 26rpx;
			color: var(--ry-text-primary);
			line-height: 1.5;
		}

		&__link {
			display: block;
			margin-top: 4rpx;
			font-size: 24rpx;
			color: #FF6600;
			font-weight: 500;
		}
	}

	/* ── Section blocks ── */
	.apply-block {
		padding: 32rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 4rpx 32rpx rgba(0, 0, 0, 0.04);

		&--placeholder {
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 16rpx;
			padding: 56rpx 28rpx;
			font-size: 26rpx;
			color: #A8A7A4;
			background: rgba(255, 255, 255, 0.6);
		}

		&__head {
			display: flex;
			align-items: flex-start;
			gap: 16rpx;
			margin-bottom: 28rpx;
			padding-bottom: 0;
		}

		&__index {
			flex-shrink: 0;
			width: 56rpx;
			height: 56rpx;
			border-radius: 16rpx;
			background: linear-gradient(135deg, #FF6600, #FF8A3D);
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 22rpx;
			font-weight: 700;
			color: #FFFFFF;

			&--blue {
				background: linear-gradient(135deg, #2979FF, #5B9FFF);
			}

			&--green {
				background: linear-gradient(135deg, #2EAF6A, #5BC88F);
			}

			&--purple {
				background: linear-gradient(135deg, #9C27B0, #BA68C8);
			}
		}

		&__titles {
			flex: 1;
			display: flex;
			flex-direction: column;
			gap: 6rpx;
			padding-top: 4rpx;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
			line-height: 1.3;
		}

		&__sub {
			font-size: 24rpx;
			color: var(--ry-text-hint);
		}
	}

	/* ── Form inputs ── */
	.apply-form {
		display: flex;
		flex-direction: column;
		gap: 24rpx;

		&--gap {
			margin-top: 24rpx;
		}

		&__item {
			display: flex;
			flex-direction: column;
			gap: 12rpx;

			&--last {
				margin-bottom: 0;
			}
		}

		&__label {
			font-size: 26rpx;
			color: var(--ry-text-secondary);
			font-weight: 500;
			letter-spacing: 0.5rpx;

			&--block {
				margin-bottom: 4rpx;
			}
		}

		&__req {
			color: #FF4D4F;
			margin-right: 4rpx;
		}

		&__input-wrap {
			display: flex;
			align-items: center;
			gap: 14rpx;
			padding: 0 28rpx;
			height: 92rpx;
			border-radius: 20rpx;
			background: #F3F2F0;
			border: none;
			transition: background 0.2s ease, box-shadow 0.2s ease;

			&:focus-within {
				background: #E8E7E4;
			}
		}

		&__input-wrap--tap:active {
			background: #EDECEA;
		}

		&__input-wrap--readonly {
			background: #F5F4F2;
		}

		&__input-wrap--hours {
			justify-content: flex-start;
		}

		&__input {
			flex: 1;
			font-size: 28rpx;
			color: var(--ry-text-primary);
			height: 100%;

			&--plain {
				padding-left: 0;
			}

			&--text {
				flex: 1;
				min-width: 0;
				line-height: 88rpx;
			}
		}

		&__hours-val {
			font-size: 28rpx;
			color: var(--ry-text-primary);
			line-height: 1;
		}

		&__hours-sep {
			margin: 0 16rpx;
			font-size: 24rpx;
			color: var(--ry-text-hint);
		}

		&__placeholder {
			color: #B8B8B8;
			font-size: 28rpx;
		}
	}

	/* ── Store type picker ── */
	.apply-form__picker {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
		padding: 0 28rpx;
		height: 92rpx;
		border-radius: 20rpx;
		background: #F3F2F0;
		border: none;
		box-sizing: border-box;
		transition: background 0.2s ease;

		&:active {
			background: #EDECEA;
		}
	}

	.apply-form__picker-value {
		flex: 1;
		display: flex;
		align-items: center;
		gap: 16rpx;
		min-width: 0;
	}

	.apply-form__picker-icon {
		width: 44rpx;
		height: 44rpx;
		border-radius: 12rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.apply-form__picker-label {
		flex: 1;
		min-width: 0;
		font-size: 28rpx;
		font-weight: 500;
		color: var(--ry-text-primary);
	}

	.apply-form__picker-placeholder {
		flex: 1;
		font-size: 28rpx;
		color: #B8B8B8;
	}

	.apply-craft-popup__scroll {
		max-height: 56vh;
	}

	.apply-craft-popup__loading,
	.apply-craft-popup__empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		padding: 48rpx 24rpx;
		font-size: 26rpx;
		color: var(--ry-text-hint);
	}

	/* ── Store type popup ── */
	.apply-type-popup {
		padding: 16rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
		background: #ffffff;
		border-radius: 20rpx 20rpx 0 0;

		&__title {
			display: block;
			font-size: 34rpx;
			font-weight: 700;
			color: var(--ry-text-primary);
			text-align: center;
		}

		&__sub {
			display: block;
			font-size: 24rpx;
			color: var(--ry-text-hint);
			text-align: center;
			margin: 8rpx 0 24rpx;
		}

		&__list {
			display: flex;
			flex-direction: column;
			gap: 16rpx;
		}

		&__item {
			display: flex;
			align-items: center;
			gap: 20rpx;
			padding: 24rpx;
			border-radius: 20rpx;
			border: none;
			background: #F3F2F0;

			&:active {
				opacity: 0.9;
			}

			&--active {
				background: linear-gradient(135deg, #FFF8F3, #FFF0E6);
			}
		}

		&__icon {
			width: 80rpx;
			height: 80rpx;
			border-radius: 20rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 6rpx;
		}

		&__name {
			font-size: 30rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
		}

		&__deposit {
			font-size: 24rpx;
			color: #FF6600;
		}

		&__desc {
			font-size: 24rpx;
			color: var(--ry-text-hint);
			line-height: 1.4;
		}
	}

	.apply-type-deposit {
		margin-top: 12rpx;
		padding: 12rpx 20rpx;
		border-radius: 12rpx;
		background: #F7F8FA;
		font-size: 24rpx;
		color: var(--ry-text-secondary);
	}

	.apply-type-alert {
		display: flex;
		align-items: flex-start;
		gap: 8rpx;
		margin-top: 16rpx;
		padding: 16rpx 20rpx;
		border-radius: 16rpx;
		background: rgba(255, 102, 0, 0.06);
		font-size: 24rpx;
		color: #E65C00;
		line-height: 1.5;
	}

	/* ── Upload cards ── */
	.apply-upload-card {
		position: relative;
		border-radius: 20rpx;
		overflow: hidden;
		border: none;
		background: #F3F2F0;

		&--license {
			height: 260rpx;

			.apply-upload-card__img {
				width: 100%;
				height: 260rpx;
				display: block;
			}
		}

		&--id {
			height: 200rpx;
		}

		&__img {
			width: 100%;
			height: 100%;
			min-height: 200rpx;
			display: block;
		}

		&__empty {
			min-height: 200rpx;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 10rpx;
			padding: 24rpx;

			&--wide {
				min-height: 260rpx;
			}
		}

		&__icon-wrap {
			width: 80rpx;
			height: 80rpx;
			border-radius: 20rpx;
			background: linear-gradient(135deg, #FFF3EB, #FFE8D6);
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 4rpx;
		}

		&__title {
			font-size: 28rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
		}

		&__hint {
			font-size: 24rpx;
			color: var(--ry-text-hint);
		}

		&__empty text {
			font-size: 24rpx;
			color: var(--ry-text-hint);
		}

		&__mask {
			position: absolute;
			inset: 0;
			background: rgba(0, 0, 0, 0.45);
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			font-size: 24rpx;
			color: #FFFFFF;
			opacity: 0;
			transition: opacity 0.2s ease;
			pointer-events: none;

			&--sm {
				opacity: 0;
			}
		}

		&:active .apply-upload-card__mask,
		&:active .apply-upload-card__mask--sm {
			opacity: 1;
		}
	}

	.apply-id-grid {
		display: flex;
		gap: 20rpx;
		margin-top: 8rpx;
	}

	.apply-id-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 12rpx;

		&__label {
			font-size: 24rpx;
			color: var(--ry-text-secondary);
			font-weight: 500;
		}
	}

	/* ── Qual list ── */
	.apply-qual-list {
		display: flex;
		flex-direction: column;
	}

	.apply-qual-item {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
		padding: 24rpx 0;
		position: relative;

		&--multi {
			gap: 12rpx;
		}

		&__main {
			display: flex;
			align-items: center;
			gap: 20rpx;
		}

		&__info {
			flex: 1;
			min-width: 0;
		}

		&__head {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 10rpx;
			margin-bottom: 6rpx;
		}

		&__count {
			margin-left: auto;
			font-size: 22rpx;
			color: #FF6600;
			font-weight: 600;
		}

		&__tag {
			flex-shrink: 0;
			padding: 2rpx 10rpx;
			border-radius: 6rpx;
			font-size: 20rpx;
			color: #A8A7A4;
			background: #EDECEA;

			&--req {
				color: #E65C00;
				background: rgba(255, 102, 0, 0.1);
			}
		}

		&__name {
			font-size: 28rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
		}

		&__desc {
			font-size: 24rpx;
			color: var(--ry-text-hint);
			line-height: 1.4;
		}

		&__upload {
			position: relative;
			flex-shrink: 0;
			width: 120rpx;
			height: 120rpx;
			border-radius: 16rpx;
			border: none;
			background: #F3F2F0;
			overflow: hidden;

			&:active {
				opacity: 0.85;
				background: #EDECEA;
			}
		}

		&__img {
			width: 100%;
			height: 100%;
		}

		&__empty {
			width: 100%;
			height: 100%;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__done {
			position: absolute;
			top: 6rpx;
			right: 6rpx;
			width: 28rpx;
			height: 28rpx;
			border-radius: 50%;
			background: #2EAF6A;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__line {
			position: absolute;
			bottom: 0;
			left: 0;
			right: 140rpx;
			height: 1rpx;
			background: rgba(0, 0, 0, 0.04);
		}

		&--multi &__line {
			right: 0;
		}
	}

	.apply-qual-multi {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 16rpx;

		&__item,
		&__add {
			position: relative;
			width: 100%;
			aspect-ratio: 1;
			border-radius: 16rpx;
			overflow: hidden;
			box-sizing: border-box;
		}

		&__img {
			width: 100%;
			height: 100%;
			display: block;
			background: #EEE;
		}

		&__del {
			position: absolute;
			top: 6rpx;
			right: 6rpx;
			width: 32rpx;
			height: 32rpx;
			border-radius: 999rpx;
			background: rgba(0, 0, 0, 0.45);
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__add {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 4rpx;
			background: #F3F2F0;
			border: 2rpx dashed rgba(255, 102, 0, 0.35);

			text {
				font-size: 20rpx;
				color: #FF6600;
			}

			&:active {
				opacity: 0.85;
			}
		}
	}

	/* ── Footer ── */
	.apply-footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
		background: rgba(255, 255, 255, 0.96);
		backdrop-filter: blur(20px);
		box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.04);

		&__safe {
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			margin-bottom: 16rpx;
			font-size: 22rpx;
			color: var(--ry-text-hint);
		}
	}

	/* ── Popup ── */
	.apply-popup {
		padding: 16rpx var(--ry-page-padding-x) calc(24rpx + env(safe-area-inset-bottom));
		background: #ffffff;
		border-radius: 20rpx 20rpx 0 0;

		&__handle {
			width: 64rpx;
			height: 8rpx;
			border-radius: 4rpx;
			background: #E0E0E0;
			margin: 0 auto 24rpx;
		}

		&__title {
			display: block;
			font-size: 34rpx;
			font-weight: 700;
			color: var(--ry-text-primary);
			text-align: center;
		}

		&__sub {
			display: block;
			font-size: 24rpx;
			color: var(--ry-text-hint);
			text-align: center;
			margin: 8rpx 0 24rpx;
		}

		&__scroll {
			max-height: 52vh;
			margin-bottom: 24rpx;
		}

		&__item {
			padding: 20rpx 0;

			& + & {
				margin-top: 4rpx;
				padding-top: 24rpx;
				border-top: 1rpx solid rgba(0, 0, 0, 0.04);
			}
		}

		&__item-head {
			display: flex;
			align-items: center;
			gap: 12rpx;
			margin-bottom: 8rpx;
		}

		&__tag {
			padding: 2rpx 12rpx;
			border-radius: 6rpx;
			font-size: 20rpx;
			color: #A8A7A4;
			background: #EDECEA;

			&--req {
				color: #E65C00;
				background: rgba(255, 102, 0, 0.1);
			}
		}

		&__name {
			font-size: 28rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
		}

		&__desc {
			font-size: 24rpx;
			color: var(--ry-text-hint);
			line-height: 1.5;
			padding-left: 0;
		}
	}
</style>
