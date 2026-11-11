<template>
	<view class="sd-page" :style="pageStyle">
		<view v-if="!ready" class="sd-loading">
			<view class="sd-loading__ring">
				<u-loading-icon mode="circle" :color="theme.primary" size="36" />
			</view>
			<text class="sd-loading__text">加载装修数据...</text>
		</view>

		<template v-else>
			<!-- 门店摘要 -->
			<view class="sd-hero ry-card">
				<view class="sd-hero__bg" :style="{ background: `linear-gradient(135deg, ${theme.primary} 0%, ${theme.primary}CC 100%)` }" />
				<view class="sd-hero__content">
					<view class="sd-hero__top">
						<view class="sd-hero__type" :style="{ color: displayTypeMeta.color, backgroundColor: displayTypeMeta.bgColor }">
							<u-icon :name="displayTypeMeta.icon" :color="displayTypeMeta.color" :size="14" />
							<text>{{ displayTypeMeta.label }}</text>
						</view>
						<view class="sd-hero__preview" @click="onPreview">
							<u-icon name="eye-fill" color="#FFF" :size="14" />
							<text>{{ ui.previewUserPage }}</text>
						</view>
					</view>
					<text class="sd-hero__name">{{ profile.storeName }}</text>
					<view class="sd-hero__addr">
						<u-icon name="map-fill" color="rgba(255,255,255,0.75)" :size="14" />
						<text>{{ profile.storeAddress || '暂无地址' }}</text>
					</view>
				</view>
			</view>

			<!-- 视觉展示 -->
			<view class="sd-section ry-card">
				<view class="sd-section__head">
					<view class="sd-section__icon" :style="{ background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
						<u-icon name="photo-fill" :color="theme.primary" :size="18" />
					</view>
					<view class="sd-section__meta">
						<text class="sd-section__title">{{ ui.sectionVisual }}</text>
						<text class="sd-section__desc">{{ ui.sectionVisualDesc }}</text>
					</view>
				</view>
				<view class="sd-section__body">
					<view class="sd-field sd-field--cover">
						<view class="sd-field__head">
							<text class="sd-field__label">{{ ui.uploadCover }}</text>
							<text class="sd-field__badge">16:9</text>
						</view>
						<text class="sd-field__hint">{{ coverUploadHint }}</text>
						<view class="sd-upload sd-upload--cover">
							<view v-if="coverImage" class="sd-upload__cover-item" @click="pickCoverImage">
								<image :src="coverImage" mode="aspectFill" class="sd-upload__img" />
								<view class="sd-upload__overlay">
									<u-icon name="camera-fill" color="#FFF" :size="22" />
									<text>更换封面</text>
								</view>
								<view class="sd-upload__del" @click.stop="removeCoverImage">
									<u-icon name="close" color="#FFF" :size="11" />
								</view>
							</view>
							<view v-else class="sd-upload__cover-add" @click="pickCoverImage">
								<view class="sd-upload__add-icon" :style="{ background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
									<u-icon name="camera-fill" :color="theme.primary" :size="28" />
								</view>
								<text class="sd-upload__add-title">上传封面图</text>
								<text class="sd-upload__add-desc">点击选择图片</text>
							</view>
						</view>
					</view>

					<view v-if="hasBannerCarousel" class="sd-field">
						<view class="sd-field__head">
							<text class="sd-field__label">{{ ui.uploadBanner }}</text>
							<text class="sd-field__badge">{{ bannerList.length }}/{{ maxBannerImages }}</text>
						</view>
						<text class="sd-field__hint">{{ ui.uploadBannerTip }}</text>
						<scroll-view scroll-x class="sd-banner-scroll" :show-scrollbar="false">
							<view class="sd-banner-row">
								<view v-if="bannerList.length < maxBannerImages" class="sd-banner-item sd-banner-item--add" @click="addBanner">
									<u-icon name="plus-circle-fill" :color="theme.primary" :size="24" />
									<text>添加</text>
								</view>
								<view v-for="(img, index) in bannerList" :key="index" class="sd-banner-item">
									<image :src="img" mode="aspectFill" class="sd-banner-item__img" />
									<view class="sd-banner-item__idx">{{ index + 1 }}</view>
									<view class="sd-upload__del" @click="removeBanner(index)">
										<u-icon name="close" color="#FFF" :size="10" />
									</view>
								</view>
							</view>
						</scroll-view>
					</view>

					<view v-if="typeKey === 'culture'" class="sd-field">
						<text class="sd-field__label">{{ ui.coverTheme }}（起）</text>
						<view class="sd-color-input">
							<view class="sd-color-input__dot" :style="{ background: form.coverFrom || '#B45D2D' }" />
							<input v-model="form.coverFrom" class="sd-field__input" placeholder="#B45D2D" placeholder-class="sd-field__placeholder" />
						</view>
					</view>
					<view v-if="typeKey === 'culture'" class="sd-field">
						<text class="sd-field__label">{{ ui.coverTheme }}（止）</text>
						<view class="sd-color-input">
							<view class="sd-color-input__dot" :style="{ background: form.coverTo || '#8D4E2A' }" />
							<input v-model="form.coverTo" class="sd-field__input" placeholder="#8D4E2A" placeholder-class="sd-field__placeholder" />
						</view>
					</view>
				</view>
			</view>

			<!-- 基础信息 -->
			<view class="sd-section ry-card">
				<view class="sd-section__head">
					<view class="sd-section__icon" :style="{ background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
						<u-icon name="info-circle-fill" :color="theme.primary" :size="18" />
					</view>
					<view class="sd-section__meta">
						<text class="sd-section__title">{{ ui.sectionBasic }}</text>
						<text class="sd-section__desc">{{ ui.sectionBasicDesc }}</text>
					</view>
				</view>
				<view class="sd-notice">
					<u-icon name="info-circle-fill" :color="theme.primary" :size="16" />
					<text>{{ profileNote }}</text>
				</view>
				<view class="sd-section__body">
					<template v-if="typeKey === 'hotel'">
						<view class="sd-field">
							<text class="sd-field__label">星级标签</text>
							<input v-model="form.starLabel" class="sd-field__input" placeholder="五星级" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field">
							<text class="sd-field__label">星级 (1-5)</text>
							<input v-model="form.starLevel" class="sd-field__input" type="number" placeholder="5" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field">
							<view class="sd-field__label-row">
								<u-icon name="clock-fill" :color="theme.primary" :size="14" />
								<text class="sd-field__label">入住时间</text>
							</view>
							<input v-model="form.checkInTime" class="sd-field__input" placeholder="14:00 后入住" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field">
							<view class="sd-field__label-row">
								<u-icon name="clock-fill" :color="theme.primary" :size="14" />
								<text class="sd-field__label">退房时间</text>
							</view>
							<input v-model="form.checkOutTime" class="sd-field__input" placeholder="12:00 前退房" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field">
							<view class="sd-field__label-row">
								<u-icon name="phone-fill" :color="theme.primary" :size="14" />
								<text class="sd-field__label">联系电话</text>
							</view>
							<input v-model="form.phone" class="sd-field__input" type="number" placeholder="前台电话" placeholder-class="sd-field__placeholder" />
						</view>
					</template>
					<template v-else-if="typeKey === 'restaurant'">
						<view class="sd-field">
							<text class="sd-field__label">角标文案</text>
							<input v-model="form.badgeText" class="sd-field__input" placeholder="非遗美食" placeholder-class="sd-field__placeholder" />
							<text class="sd-field__hint sd-field__hint--below">用户端详情页顶部角标展示</text>
						</view>
						<view class="sd-field sd-field--tags">
							<text class="sd-field__label">门店标签</text>
							<text class="sd-field__hint sd-field__hint--below-top">{{ ui.tagHint }}</text>
							<view v-if="tagList.length" class="sd-tags__list">
								<view v-for="(item, index) in tagList" :key="item.id" class="sd-tag-item"
									:style="{ color: theme.primary, background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
									<text class="sd-tag-item__label">{{ item.label }}</text>
									<view class="sd-tag-item__del" @click="removeTag(index)">
										<u-icon name="close" :color="theme.primary" :size="12" />
									</view>
								</view>
							</view>
							<view class="sd-tags__add">
								<input
									v-model="tagInput"
									class="sd-field__input sd-field__input--sm sd-tags__input"
									:placeholder="ui.tagInputPlaceholder"
									placeholder-class="sd-field__placeholder"
									maxlength="12"
									@confirm="addTag"
								/>
								<view class="sd-tags__btn" :style="{ background: theme.primary }" @click="addTag">
									<text>{{ ui.addTag }}</text>
								</view>
							</view>
						</view>
					</template>
					<template v-else-if="typeKey === 'travel'">
						<view class="sd-field">
							<text class="sd-field__label">行程时长</text>
							<input v-model="form.duration" class="sd-field__input" placeholder="1天" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field">
							<text class="sd-field__label">线路标签</text>
							<input v-model="tagsText" class="sd-field__input" :placeholder="ui.tagPlaceholder" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field">
							<text class="sd-field__label">集合地点</text>
							<input v-model="form.meetPoint" class="sd-field__input" placeholder="集合地点" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field">
							<text class="sd-field__label">途经景点</text>
							<input v-model="spotsText" class="sd-field__input" placeholder="景点A、景点B" placeholder-class="sd-field__placeholder" />
						</view>
					</template>
					<template v-else-if="typeKey === 'culture'">
						<view class="sd-field">
							<text class="sd-field__label">展示评分</text>
							<input v-model="form.rating" class="sd-field__input" placeholder="4.8" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field">
							<text class="sd-field__label">评价数量</text>
							<input v-model="form.reviewCount" class="sd-field__input" type="number" placeholder="0" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field">
							<text class="sd-field__label">门店标签</text>
							<input v-model="tagsText" class="sd-field__input" :placeholder="ui.tagPlaceholder" placeholder-class="sd-field__placeholder" />
						</view>
					</template>
				</view>
			</view>

			<!-- 内容详情 -->
			<view class="sd-section ry-card">
				<view class="sd-section__head">
					<view class="sd-section__icon" :style="{ background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
						<u-icon name="file-text-fill" :color="theme.primary" :size="18" />
					</view>
					<view class="sd-section__meta">
						<text class="sd-section__title">{{ ui.sectionContent }}</text>
						<text class="sd-section__desc">{{ ui.sectionContentDesc }}</text>
					</view>
				</view>
				<view class="sd-section__body">
					<view class="sd-field">
						<text class="sd-field__label">{{ introLabel }}</text>
						<view class="sd-textarea-wrap">
							<textarea
								v-model="introText"
								class="sd-textarea"
								:placeholder="introPlaceholder"
								placeholder-class="sd-field__placeholder"
								maxlength="500"
							/>
							<text class="sd-textarea__count">{{ introText.length }}/500</text>
						</view>
					</view>

					<view v-if="typeKey === 'hotel'" class="sd-field sd-field--facilities">
						<text class="sd-field__label">酒店设施</text>
						<text class="sd-field__hint">逐项添加，用户端酒店详情「酒店设施」区块展示</text>
						<view v-if="facilityList.length" class="sd-facilities__list">
							<view v-for="(item, index) in facilityList" :key="item.id" class="sd-facility-item">
								<view class="sd-facility-item__icon" :style="{ background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
									<u-icon :name="item.icon" :color="theme.primary" :size="14" />
								</view>
								<text class="sd-facility-item__label">{{ item.label }}</text>
								<view class="sd-facility-item__del" @click="removeFacility(index)">
									<u-icon name="close" color="#999" :size="12" />
								</view>
							</view>
						</view>
						<view class="sd-facilities__add">
							<input
								v-model="facilityInput"
								class="sd-field__input sd-field__input--sm sd-facilities__input"
								:placeholder="ui.facilitiesPlaceholder"
								placeholder-class="sd-field__placeholder"
								maxlength="20"
								@confirm="addFacility"
							/>
							<view class="sd-facilities__btn" :style="{ background: theme.primary }" @click="addFacility">
								<text>{{ ui.addFacility }}</text>
							</view>
						</view>
						<view v-if="availableFacilityPresets.length" class="sd-facilities__presets">
							<text class="sd-facilities__presets-label">{{ ui.facilitiesPresetLabel }}</text>
							<view class="sd-chips">
								<view
									v-for="preset in availableFacilityPresets"
									:key="preset"
									class="sd-chip sd-chip--preset"
									:style="{ color: theme.primary, borderColor: theme.primaryLight || 'rgba(47,123,255,0.2)' }"
									@click="addPresetFacility(preset)"
								>
									<u-icon name="plus" :color="theme.primary" :size="10" />
									<text>{{ preset }}</text>
								</view>
							</view>
						</view>
					</view>

					<view v-if="typeKey === 'culture'" class="sd-field">
						<text class="sd-field__label">特色服务</text>
						<input v-model="servicesText" class="sd-field__input" :placeholder="ui.servicesPlaceholder" placeholder-class="sd-field__placeholder" />
					</view>

					<view v-if="typeKey === 'travel'" class="sd-itinerary">
						<text class="sd-field__label">行程节点</text>
						<view v-for="(node, index) in form.itinerary" :key="index" class="sd-itinerary__card">
							<view class="sd-itinerary__head">
								<view class="sd-itinerary__dot" :style="{ background: theme.primary }">{{ index + 1 }}</view>
								<text class="sd-itinerary__idx">节点 {{ index + 1 }}</text>
								<view v-if="form.itinerary.length > 1" class="sd-itinerary__del" @click="removeItinerary(index)">
									<u-icon name="trash" color="#999" :size="15" />
								</view>
							</view>
							<view class="sd-field sd-field--compact">
								<text class="sd-field__label">时间</text>
								<input v-model="node.time" class="sd-field__input sd-field__input--sm" placeholder="09:00" placeholder-class="sd-field__placeholder" />
							</view>
							<view class="sd-field sd-field--compact">
								<text class="sd-field__label">节点名称</text>
								<input v-model="node.name" class="sd-field__input sd-field__input--sm" placeholder="节点名称" placeholder-class="sd-field__placeholder" />
							</view>
							<view class="sd-field sd-field--compact">
								<text class="sd-field__label">地点</text>
								<input v-model="node.location" class="sd-field__input sd-field__input--sm" placeholder="地点" placeholder-class="sd-field__placeholder" />
							</view>
							<textarea v-model="node.desc" class="sd-textarea sd-textarea--sm" placeholder="节点说明" placeholder-class="sd-field__placeholder" maxlength="120" />
						</view>
						<view class="sd-add-btn" @click="addItinerary">
							<u-icon name="plus-circle-fill" :color="theme.primary" :size="16" />
							<text>{{ ui.addItinerary }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 酒店政策 -->
			<view v-if="typeKey === 'hotel'" class="sd-section ry-card">
				<view class="sd-section__head">
					<view class="sd-section__icon" :style="{ background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
						<u-icon name="list-dot" :color="theme.primary" :size="18" />
					</view>
					<view class="sd-section__meta">
						<text class="sd-section__title">{{ ui.sectionPolicy }}</text>
						<text class="sd-section__desc">{{ ui.sectionPolicyDesc }}</text>
					</view>
				</view>
				<view class="sd-section__body">
					<view class="sd-policy-card">
						<view class="sd-policy-card__head">
							<view class="sd-policy-card__icon" :style="{ background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
								<u-icon name="calendar-fill" :color="theme.primary" :size="16" />
							</view>
							<text class="sd-policy-card__label">预订提示</text>
						</view>
						<textarea v-model="form.hotelPolicy.bookingNotice" class="sd-textarea sd-textarea--policy" placeholder="订单确认规则说明" placeholder-class="sd-field__placeholder" maxlength="200" />
					</view>
					<view class="sd-policy-card">
						<view class="sd-policy-card__head">
							<view class="sd-policy-card__icon" :style="{ background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
								<u-icon name="home-fill" :color="theme.primary" :size="16" />
							</view>
							<text class="sd-policy-card__label">入住提示</text>
						</view>
						<textarea v-model="form.hotelPolicy.checkInNotice" class="sd-textarea sd-textarea--policy" placeholder="前台服务、办理方式等" placeholder-class="sd-field__placeholder" maxlength="200" />
					</view>
					<view class="sd-policy-mini">
						<view class="sd-policy-card__head">
							<u-icon name="rmb-circle-fill" :color="theme.primary" :size="14" />
							<text class="sd-policy-mini__label">押金说明</text>
						</view>
						<input v-model="form.hotelPolicy.depositPolicy" class="sd-field__input sd-field__input--sm" placeholder="不收取押金" placeholder-class="sd-field__placeholder" />
					</view>
					<view class="sd-policy-mini">
						<view class="sd-policy-card__head">
							<u-icon name="gift-fill" :color="theme.primary" :size="14" />
							<text class="sd-policy-mini__label">早餐说明</text>
						</view>
						<input v-model="form.hotelPolicy.breakfastInfo" class="sd-field__input sd-field__input--sm" placeholder="如：含早餐，07:00-09:00" placeholder-class="sd-field__placeholder" />
						<text class="sd-field__hint sd-field__hint--below">无早餐可留空；已在「酒店设施」填写含早餐时可补充时段</text>
					</view>
				</view>
			</view>

			<!-- 营业信息（餐饮） -->
			<view v-if="typeKey === 'restaurant'" class="sd-section ry-card">
				<view class="sd-section__head">
					<view class="sd-section__icon" :style="{ background: theme.primaryLight || 'rgba(47,123,255,0.1)' }">
						<u-icon name="clock-fill" :color="theme.primary" :size="18" />
					</view>
					<view class="sd-section__meta">
						<text class="sd-section__title">{{ ui.sectionBusiness }}</text>
						<text class="sd-section__desc">{{ ui.sectionBusinessDesc }}</text>
					</view>
				</view>
				<view class="sd-section__body">
					<view v-for="(row, index) in form.businessHours" :key="row.id" class="sd-hours">
						<view class="sd-hours__head">
							<view class="sd-hours__badge" :style="{ background: theme.primary }">{{ index + 1 }}</view>
							<text class="sd-hours__title">时段 {{ index + 1 }}</text>
							<view v-if="form.businessHours.length > 1" class="sd-hours__del" @click="removeHourRow(index)">
								<u-icon name="close-circle-fill" color="#CCC" :size="18" />
							</view>
						</view>
						<view class="sd-field sd-field--compact">
							<text class="sd-field__label">时段名称</text>
							<input v-model="row.label" class="sd-field__input sd-field__input--sm" placeholder="午餐 / 晚餐" placeholder-class="sd-field__placeholder" />
						</view>
						<view class="sd-field sd-field--compact">
							<text class="sd-field__label">营业时间</text>
							<input v-model="row.time" class="sd-field__input sd-field__input--sm" placeholder="09:00 - 21:00" placeholder-class="sd-field__placeholder" />
						</view>
					</view>
					<view class="sd-add-btn" @click="addHourRow">
						<u-icon name="plus-circle-fill" :color="theme.primary" :size="16" />
						<text>{{ ui.addHour }}</text>
					</view>
				</view>
			</view>

			<view class="sd-footnote ry-card">
				<u-icon name="info-circle" color="#999" :size="15" />
				<text>{{ ui.hint }}</text>
			</view>

			<view class="sd-footer">
				<view class="sd-footer__inner">
					<view class="sd-footer__preview" @click="onPreview">
						<u-icon name="eye-fill" :color="theme.primary" :size="22" />
						<text>预览</text>
					</view>
					<view class="sd-footer__save" :style="{ background: `linear-gradient(135deg, ${theme.primary} 0%, ${theme.primary}DD 100%)` }" @click="onSave">
						<u-icon v-if="!submitting" name="checkmark-circle-fill" color="#FFF" :size="18" />
						<text>{{ submitting ? '保存中...' : ui.saveBtn }}</text>
					</view>
				</view>
			</view>
		</template>
	</view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { requireMerchantApproved, MERCHANT_BUSINESS_UI } from '@/common/merchant-center.js';
import { getMerchantStoreTheme } from '@/common/merchant-theme.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import {
	STORE_DECOR_UI,
	HOTEL_FACILITY_PRESETS,
	getUserPreviewPath,
	parseCommaList,
	normalizeFacilityItems,
	buildFacilitiesPayload,
	resolveFacilityIcon,
	getStoreDecorProfileNote,
	ensureHotelDecorDefaults,
	ensureHotelPolicyDefaults,
	stripLegacyStoreDecorServiceKeys
} from '@/common/merchant-store-decor.js';
import {
	fetchMerchantStoreDecor,
	saveMerchantStoreDecor,
	ensureUploadedUrl
} from '@/common/api/merchant.js';

const PACKAGE_STORE_TYPE = 'restaurant';
const { initTheme, applyNavBarColor, applyStoreType, pageStyle, theme } = useMerchantTheme();
const ui = { ...STORE_DECOR_UI };
const ready = ref(false);
const formLoaded = ref(false);
const submitting = ref(false);
const typeKey = ref(PACKAGE_STORE_TYPE);
const profile = ref({});
const form = reactive({});
const coverImage = ref('');
const bannerList = ref([]);
const tagsText = ref('');
const tagList = ref([]);
const tagInput = ref('');
const spotsText = ref('');
const facilityList = ref([]);
const facilityInput = ref('');
const servicesText = ref('');
const introText = ref('');
const userPreviewPath = ref('');
const maxBannerImages = 5;
const maxFacilityCount = 20;
const maxTagCount = 10;

const storeTheme = computed(() => getMerchantStoreTheme(typeKey.value));
const displayTypeMeta = computed(() => ({
	icon: storeTheme.value.icon,
	label: storeTheme.value.label,
	color: storeTheme.value.primary,
	bgColor: storeTheme.value.primaryLight
}));
const hasBannerCarousel = computed(() => ['hotel', 'restaurant'].includes(typeKey.value));
const availableFacilityPresets = computed(() => {
	const existing = new Set(facilityList.value.map((item) => item.label));
	return HOTEL_FACILITY_PRESETS.filter((label) => !existing.has(label));
});

const coverUploadHint = computed(() => {
	if (hasBannerCarousel.value) {
		return ui.uploadCoverTip;
	}
	return ui.uploadCoverOnlyTip;
});

const introLabel = computed(() => {
	if (typeKey.value === 'travel') return '线路介绍';
	if (typeKey.value === 'restaurant') return '店铺介绍';
	return '门店介绍';
});

const introPlaceholder = computed(() => {
	const map = {
		hotel: ui.introPlaceholderHotel,
		restaurant: ui.introPlaceholderRestaurant,
		travel: ui.introPlaceholderTravel,
		culture: ui.introPlaceholderCulture
	};
	return map[typeKey.value] || ui.introPlaceholderCulture;
});

const profileNote = computed(() => getStoreDecorProfileNote(typeKey.value));

function applyDecorData(data) {
	const decor = data.decor || {};
	typeKey.value = PACKAGE_STORE_TYPE;
	applyStoreType(PACKAGE_STORE_TYPE);
	profile.value = data.profile || {};
	Object.keys(form).forEach((key) => delete form[key]);
	Object.assign(form, JSON.parse(JSON.stringify(decor)));
	facilityList.value = [];
	facilityInput.value = '';
	tagList.value = [];
	tagInput.value = '';

	if (data.typeKey === 'hotel') {
		ensureHotelDecorDefaults(form);
		form.hotelPolicy = ensureHotelPolicyDefaults(JSON.parse(JSON.stringify(decor.hotelPolicy || {})));
		coverImage.value = decor.cover || decor.coverImage || '';
		bannerList.value = [...(decor.bannerImages || [])];
		if (!coverImage.value && bannerList.value.length) {
			coverImage.value = bannerList.value[0];
		}
		facilityList.value = normalizeFacilityItems(decor.facilities || []);
		facilityInput.value = '';
	} else if (data.typeKey === 'restaurant') {
		coverImage.value = decor.cover || '';
		bannerList.value = [...(decor.bannerList || [])];
		if (!coverImage.value && bannerList.value.length) {
			coverImage.value = bannerList.value[0];
		}
		tagList.value = normalizeTagItems(decor.tags || []);
		tagInput.value = '';
	} else if (data.typeKey === 'travel') {
		coverImage.value = decor.cover || '';
		bannerList.value = [];
		tagsText.value = (decor.tags || []).join('、');
		spotsText.value = (decor.spots || []).join('、');
		if (!form.itinerary?.length) {
			form.itinerary = [{ time: '09:00', name: '', location: '', desc: '' }];
		}
	} else if (data.typeKey === 'culture') {
		coverImage.value = decor.cover || '';
		bannerList.value = [];
		tagsText.value = (decor.tags || []).join('、');
		servicesText.value = (decor.services || []).join('、');
	}

	introText.value = decor.introDetail || decor.intro || '';
	userPreviewPath.value = data.userPreviewPath || getUserPreviewPath(data.typeKey);
	ready.value = true;
}

async function loadForm(force = false) {
	if (formLoaded.value && !force) {
		return;
	}
	ready.value = false;
	try {
		const res = await fetchMerchantStoreDecor();
		if (res.code === 0 && res.data) {
			applyDecorData(res.data);
			formLoaded.value = true;
			return;
		}
		ready.value = true;
		uni.showToast({ title: res.msg || MERCHANT_BUSINESS_UI.loadFail, icon: 'none' });
	} catch (e) {
		ready.value = true;
		uni.showToast({ title: MERCHANT_BUSINESS_UI.networkError, icon: 'none' });
	}
}

function pickCoverImage() {
	uni.chooseImage({
		count: 1,
		success: (res) => {
			const path = res.tempFilePaths?.[0];
			if (path) {
				coverImage.value = path;
			}
		}
	});
}

function removeCoverImage() {
	coverImage.value = '';
}

function addBanner() {
	const remain = maxBannerImages - bannerList.value.length;
	if (remain <= 0) return;
	uni.chooseImage({
		count: remain,
		success: (res) => {
			bannerList.value.push(...(res.tempFilePaths || []));
		}
	});
}

function removeBanner(index) {
	bannerList.value.splice(index, 1);
}

function addHourRow() {
	if (!form.businessHours) form.businessHours = [];
	form.businessHours.push({ id: Date.now(), label: '', time: '' });
}

function removeHourRow(index) {
	form.businessHours.splice(index, 1);
}

function addItinerary() {
	if (!form.itinerary) form.itinerary = [];
	form.itinerary.push({ time: '', name: '', location: '', desc: '' });
}

function removeItinerary(index) {
	form.itinerary.splice(index, 1);
}

function hasFacilityLabel(label) {
	const normalized = String(label || '').trim();
	if (!normalized) return false;
	return facilityList.value.some((item) => item.label === normalized);
}

function addFacilityLabel(label) {
	const normalized = String(label || '').trim();
	if (!normalized) {
		uni.showToast({ title: ui.addFacilityEmpty, icon: 'none' });
		return false;
	}
	if (hasFacilityLabel(normalized)) {
		uni.showToast({ title: ui.addFacilityDuplicate, icon: 'none' });
		return false;
	}
	if (facilityList.value.length >= maxFacilityCount) {
		uni.showToast({ title: `最多添加 ${maxFacilityCount} 项设施`, icon: 'none' });
		return false;
	}
	facilityList.value.push({
		id: `${Date.now()}-${facilityList.value.length}`,
		label: normalized,
		icon: resolveFacilityIcon(normalized)
	});
	return true;
}

function addFacility() {
	if (addFacilityLabel(facilityInput.value)) {
		facilityInput.value = '';
	}
}

function addPresetFacility(label) {
	addFacilityLabel(label);
}

function removeFacility(index) {
	facilityList.value.splice(index, 1);
}

function normalizeTagItems(tags) {
	return (tags || [])
		.map((item, index) => {
			const label = String(typeof item === 'string' ? item : item?.label || '').trim();
			if (!label) return null;
			return {
				id: item?.id || `${Date.now()}-${index}`,
				label
			};
		})
		.filter(Boolean);
}

function hasTagLabel(label) {
	const normalized = String(label || '').trim();
	if (!normalized) return false;
	return tagList.value.some((item) => item.label === normalized);
}

function addTagLabel(label) {
	const normalized = String(label || '').trim();
	if (!normalized) {
		uni.showToast({ title: ui.addTagEmpty, icon: 'none' });
		return false;
	}
	if (hasTagLabel(normalized)) {
		uni.showToast({ title: ui.addTagDuplicate, icon: 'none' });
		return false;
	}
	if (tagList.value.length >= maxTagCount) {
		uni.showToast({ title: `最多添加 ${maxTagCount} 个标签`, icon: 'none' });
		return false;
	}
	tagList.value.push({
		id: `${Date.now()}-${tagList.value.length}`,
		label: normalized
	});
	return true;
}

function addTag() {
	if (addTagLabel(tagInput.value)) {
		tagInput.value = '';
	}
}

function removeTag(index) {
	tagList.value.splice(index, 1);
}

function buildPayload() {
	const payload = stripLegacyStoreDecorServiceKeys({ ...form, intro: introText.value });
	if (typeKey.value === 'hotel') {
		ensureHotelDecorDefaults(payload);
		payload.cover = coverImage.value || '';
		payload.bannerImages = [...bannerList.value];
		payload.facilities = buildFacilitiesPayload(facilityList.value);
		payload.starLabel = String(form.starLabel || '').trim() || '四星级';
		payload.starLevel = Number(form.starLevel) || 4;
		payload.checkInTime = String(form.checkInTime || '').trim() || '14:00 后入住';
		payload.checkOutTime = String(form.checkOutTime || '').trim() || '12:00 前退房';
		payload.phone = String(form.phone || '').trim();
		payload.intro = introText.value;
		payload.hotelPolicy = ensureHotelPolicyDefaults({
			bookingNotice: form.hotelPolicy?.bookingNotice,
			checkInNotice: form.hotelPolicy?.checkInNotice,
			depositPolicy: form.hotelPolicy?.depositPolicy,
			breakfastInfo: form.hotelPolicy?.breakfastInfo
		});
	} else if (typeKey.value === 'restaurant') {
		payload.cover = coverImage.value || '';
		payload.bannerList = [...bannerList.value];
		payload.badgeText = String(form.badgeText || '').trim() || '非遗美食';
		payload.intro = introText.value;
		payload.tags = tagList.value.map((item) => item.label);
		payload.businessHours = Array.isArray(form.businessHours) ? form.businessHours : [];
	} else if (typeKey.value === 'travel') {
		payload.cover = coverImage.value || '';
		payload.intro = introText.value;
		payload.duration = String(form.duration || '').trim();
		payload.meetPoint = String(form.meetPoint || '').trim();
		payload.tags = parseCommaList(tagsText.value);
		payload.spots = parseCommaList(spotsText.value);
		payload.itinerary = Array.isArray(form.itinerary) ? form.itinerary : [];
	} else if (typeKey.value === 'culture') {
		payload.cover = coverImage.value || '';
		payload.tags = parseCommaList(tagsText.value);
		payload.intro = introText.value;
		payload.introDetail = introText.value;
		payload.services = parseCommaList(servicesText.value);
		payload.reviewCount = Number(form.reviewCount) || 0;
	}
	return payload;
}

async function onSave() {
	if (submitting.value) return;
	submitting.value = true;
	try {
		uni.showLoading({ title: '保存中...' });
		if (coverImage.value) {
			coverImage.value = await ensureUploadedUrl(coverImage.value);
		}
		const uploadedBanners = [];
		for (const path of bannerList.value) {
			uploadedBanners.push(await ensureUploadedUrl(path));
		}
		bannerList.value = uploadedBanners;
		const payload = buildPayload();
		const res = await saveMerchantStoreDecor({ decor: payload });
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '保存失败', icon: 'none' });
			return;
		}
		if (res.data) applyDecorData(res.data);
		uni.showToast({ title: ui.saveSuccess, icon: 'success' });
	} catch (e) {
		uni.showToast({ title: MERCHANT_BUSINESS_UI.networkError, icon: 'none' });
	} finally {
		submitting.value = false;
		uni.hideLoading();
	}
}

function onPreview() {
	const path = userPreviewPath.value || getUserPreviewPath(typeKey.value);
	if (!path || path.includes('id=preview')) {
		uni.showToast({ title: '请先保存装修后再预览', icon: 'none' });
		return;
	}
	uni.showToast({ title: ui.previewTip, icon: 'none' });
	setTimeout(() => {
		uni.navigateTo({ url: path });
	}, 350);
}

onLoad(async () => {
	applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
	uni.setNavigationBarTitle({ title: ui.storeDecorTitle });
	applyNavBarColor();
});

onShow(async () => {
	if (!(await requireMerchantApproved())) return;
	applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
	applyNavBarColor();
	await loadForm();
});
</script>

<style lang="scss" scoped>
$sd-input-bg: #F7F8FC;
$sd-input-border: #E8ECF4;
$sd-divider: #F0F2F6;

.sd-page {
	min-height: 100vh;
	background: var(--ry-page-bg);
	padding: 20rpx var(--ry-page-padding-x) calc(160rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.sd-loading {
	flex: 1;
	min-height: 60vh;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 24rpx;

	&__ring {
		width: 96rpx;
		height: 96rpx;
		border-radius: 50%;
		background: #FFF;
		box-shadow: var(--ry-card-shadow);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__text {
		font-size: 26rpx;
		color: var(--ry-text-hint);
	}
}

/* ── Hero Summary ── */
.sd-hero {
	position: relative;
	padding: 0;
	border-radius: var(--ry-radius-card);
	overflow: hidden;
	box-shadow: var(--ry-card-shadow);

	&__bg {
		position: absolute;
		inset: 0;
		opacity: 1;
	}

	&__content {
		position: relative;
		padding: 32rpx 28rpx 28rpx;
	}

	&__top {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		margin-bottom: 20rpx;
	}

	&__type {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 8rpx 18rpx;
		border-radius: var(--ry-radius-pill);
		font-size: 22rpx;
		font-weight: 600;
		background: rgba(255, 255, 255, 0.92) !important;
	}

	&__preview {
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 10rpx 20rpx;
		border-radius: var(--ry-radius-pill);
		background: rgba(255, 255, 255, 0.2);
		border: 1rpx solid rgba(255, 255, 255, 0.35);
		font-size: 24rpx;
		color: #FFF;
		font-weight: 500;
		backdrop-filter: blur(8px);

		&:active { opacity: 0.8; }
	}

	&__name {
		display: block;
		font-size: 36rpx;
		font-weight: 700;
		color: #FFF;
		line-height: 1.35;
		margin-bottom: 14rpx;
		text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
	}

	&__addr {
		display: flex;
		align-items: flex-start;
		gap: 8rpx;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.85);
		line-height: 1.5;
	}
}

/* ── Section Cards ── */
.sd-section {
	padding: 28rpx;
	overflow: hidden;
	box-shadow: var(--ry-card-shadow);

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
	}

	&__title {
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		line-height: 1.3;
		margin-bottom: 6rpx;
	}

	&__desc {
		display: block;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}

	&__body {
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}
}

.sd-notice {
	display: flex;
	align-items: flex-start;
	gap: 12rpx;
	padding: 18rpx 20rpx;
	margin-bottom: 20rpx;
	border-radius: 16rpx;
	background: linear-gradient(135deg, var(--mc-primary-light, #FFF5EB) 0%, rgba(255, 255, 255, 0.6) 100%);
	border: 1rpx solid rgba(0, 0, 0, 0.04);
	font-size: 24rpx;
	color: var(--ry-text-secondary);
	line-height: 1.55;
}

/* ── Form Fields ── */
.sd-field {
	margin-bottom: 20rpx;

	&--cover {
		margin-bottom: 28rpx;
	}

	&--compact {
		margin-bottom: 12rpx;
	}

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 8rpx;
	}

	&__label {
		display: block;
		font-size: 26rpx;
		color: var(--ry-text-primary);
		font-weight: 600;
		margin-bottom: 10rpx;
	}

	&__label-row {
		display: flex;
		align-items: center;
		gap: 8rpx;
		margin-bottom: 10rpx;

		.sd-field__label {
			margin-bottom: 0;
		}
	}

	&__badge {
		font-size: 20rpx;
		font-weight: 600;
		color: var(--mc-primary, #2979FF);
		padding: 4rpx 12rpx;
		border-radius: var(--ry-radius-pill);
		background: var(--mc-primary-light, rgba(47, 123, 255, 0.1));
	}

	&__hint {
		display: block;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		margin-bottom: 14rpx;
		line-height: 1.5;

		&--below {
			margin-top: 12rpx;
			margin-bottom: 0;
		}

		&--below-top {
			margin-top: 0;
			margin-bottom: 16rpx;
		}
	}

	&__input {
		width: 100%;
		height: 88rpx;
		padding: 0 24rpx;
		box-sizing: border-box;
		border-radius: 16rpx;
		background: $sd-input-bg;
		border: none;
		font-size: 28rpx;
		color: var(--ry-text-primary);

		&--sm {
			height: 76rpx;
			font-size: 26rpx;
		}
	}

	&__placeholder {
		color: #B0B8C8;
	}
}

.sd-color-input {
	display: flex;
	align-items: center;
	gap: 12rpx;

	&__dot {
		flex-shrink: 0;
		width: 36rpx;
		height: 36rpx;
		border-radius: 10rpx;
		border: 2rpx solid rgba(0, 0, 0, 0.08);
	}

	.sd-field__input {
		flex: 1;
	}
}

.sd-textarea-wrap {
	position: relative;
}

.sd-textarea {
	width: 100%;
	min-height: 200rpx;
	padding: 20rpx 24rpx 48rpx;
	box-sizing: border-box;
	border-radius: 16rpx;
	background: $sd-input-bg;
	border: none;
	font-size: 28rpx;
	line-height: 1.65;
	color: var(--ry-text-primary);

	&--sm {
		min-height: 120rpx;
		padding: 16rpx 20rpx;
		margin-top: 8rpx;
		font-size: 26rpx;
	}

	&--policy {
		min-height: 140rpx;
		padding: 16rpx 20rpx;
		margin-top: 12rpx;
		font-size: 26rpx;
		background: #FFF;
	}

	&__count {
		position: absolute;
		right: 20rpx;
		bottom: 16rpx;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		font-variant-numeric: tabular-nums;
	}
}

/* ── Upload ── */
.sd-upload {
	&--cover {
		.sd-upload__cover-item {
			position: relative;
			width: 100%;
			height: 0;
			padding-bottom: 56.25%;
			border-radius: 20rpx;
			overflow: hidden;
			box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
		}

		.sd-upload__img {
			position: absolute;
			inset: 0;
			width: 100%;
			height: 100%;
		}

		.sd-upload__overlay {
			position: absolute;
			inset: 0;
			background: rgba(0, 0, 0, 0.35);
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			opacity: 0;
			transition: opacity 0.2s;
			font-size: 24rpx;
			color: #FFF;
			font-weight: 500;
		}

		.sd-upload__cover-item:active .sd-upload__overlay {
			opacity: 1;
		}

		.sd-upload__cover-add {
			width: 100%;
			height: 0;
			padding-bottom: 56.25%;
			position: relative;
			border-radius: 20rpx;
			border: 3rpx dashed $sd-input-border;
			background: $sd-input-bg;
			transition: border-color 0.2s, background 0.2s;

			&:active {
				border-color: var(--mc-primary, #2979FF);
				background: var(--mc-primary-light, rgba(47, 123, 255, 0.06));
			}
		}

		.sd-upload__add-icon,
		.sd-upload__add-title,
		.sd-upload__add-desc {
			position: absolute;
			left: 50%;
			transform: translateX(-50%);
		}

		.sd-upload__add-icon {
			top: 28%;
			width: 80rpx;
			height: 80rpx;
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			transform: translateX(-50%);
		}

		.sd-upload__add-title {
			top: 58%;
			font-size: 28rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
			white-space: nowrap;
		}

		.sd-upload__add-desc {
			top: 72%;
			font-size: 22rpx;
			color: var(--ry-text-hint);
			white-space: nowrap;
		}
	}

	&__del {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		z-index: 3;
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		backdrop-filter: blur(4px);

		&:active { opacity: 0.85; }
	}
}

.sd-banner-scroll {
	width: 100%;
	white-space: nowrap;
}

.sd-banner-row {
	display: inline-flex;
	gap: 16rpx;
	padding: 4rpx 0;
}

.sd-banner-item {
	position: relative;
	flex-shrink: 0;
	width: 200rpx;
	height: 150rpx;
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);

	&__img {
		width: 100%;
		height: 100%;
		background: #EEF0F4;
	}

	&__idx {
		position: absolute;
		bottom: 8rpx;
		left: 8rpx;
		padding: 2rpx 10rpx;
		border-radius: 8rpx;
		background: rgba(0, 0, 0, 0.45);
		font-size: 20rpx;
		color: #FFF;
		font-weight: 600;
	}

	&--add {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		background: $sd-input-bg;
		border: 3rpx dashed $sd-input-border;
		box-shadow: none;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		font-weight: 500;

		&:active {
			border-color: var(--mc-primary, #2979FF);
			color: var(--mc-primary, #2979FF);
		}
	}
}

/* ── Tags ── */
.sd-field--tags {
	.sd-field__label {
		margin-bottom: 0;
	}
}

.sd-tags {
	&__list {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-bottom: 16rpx;
	}

	&__add {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	&__input {
		flex: 1;
		min-width: 0;
	}

	&__btn {
		flex-shrink: 0;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 76rpx;
		padding: 0 28rpx;
		border-radius: 16rpx;

		text {
			font-size: 26rpx;
			font-weight: 600;
			color: #FFF;
		}

		&:active {
			opacity: 0.88;
		}
	}
}

.sd-tag-item {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	padding: 10rpx 12rpx 10rpx 18rpx;
	border-radius: var(--ry-radius-pill);
	max-width: 100%;
	box-sizing: border-box;

	&__label {
		font-size: 24rpx;
		font-weight: 600;
		line-height: 1.3;
	}

	&__del {
		flex-shrink: 0;
		width: 32rpx;
		height: 32rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.65);

		&:active {
			opacity: 0.7;
		}
	}
}

/* ── Facilities ── */
.sd-field--facilities {
	.sd-field__hint {
		margin-bottom: 16rpx;
	}
}

.sd-facilities {
	&__list {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
		margin-bottom: 16rpx;
	}

	&__add {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	&__input {
		flex: 1;
		min-width: 0;
	}

	&__btn {
		flex-shrink: 0;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 76rpx;
		padding: 0 28rpx;
		border-radius: 16rpx;

		text {
			font-size: 26rpx;
			font-weight: 600;
			color: #FFF;
		}

		&:active {
			opacity: 0.88;
		}
	}

	&__presets {
		margin-top: 20rpx;
	}

	&__presets-label {
		display: block;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		margin-bottom: 12rpx;
	}
}

.sd-facility-item {
	display: flex;
	align-items: center;
	gap: 12rpx;
	padding: 16rpx 20rpx;
	border-radius: 16rpx;
	background: $sd-input-bg;

	&__icon {
		flex-shrink: 0;
		width: 44rpx;
		height: 44rpx;
		border-radius: 12rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__label {
		flex: 1;
		min-width: 0;
		font-size: 26rpx;
		color: var(--ry-text-primary);
		font-weight: 500;
	}

	&__del {
		flex-shrink: 0;
		width: 44rpx;
		height: 44rpx;
		display: flex;
		align-items: center;
		justify-content: center;

		&:active {
			opacity: 0.7;
		}
	}
}

.sd-chips {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
}

.sd-chip {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	padding: 8rpx 16rpx;
	border-radius: var(--ry-radius-pill);
	font-size: 22rpx;
	font-weight: 500;

	&--preset {
		background: #FFF;
		border: 1rpx solid;

		&:active {
			opacity: 0.75;
		}
	}
}

/* ── Policy Cards ── */
.sd-policy-card {
	padding: 20rpx;
	margin-bottom: 16rpx;
	border-radius: 16rpx;
	background: $sd-input-bg;

	&__head {
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	&__icon {
		flex-shrink: 0;
		width: 40rpx;
		height: 40rpx;
		border-radius: 10rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__label {
		font-size: 26rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}
}

.sd-policy-mini {
	padding: 20rpx;
	margin-bottom: 16rpx;
	border-radius: 16rpx;
	background: $sd-input-bg;

	.sd-field__input {
		margin-top: 12rpx;
	}

	&__label {
		font-size: 24rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}
}

/* ── Hours & Itinerary ── */
.sd-hours {
	margin-bottom: 16rpx;
	padding: 20rpx;
	border-radius: 16rpx;
	background: $sd-input-bg;
	border: 1rpx solid $sd-divider;

	&__head {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 16rpx;
	}

	&__badge {
		width: 36rpx;
		height: 36rpx;
		border-radius: 10rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 20rpx;
		font-weight: 700;
		color: #FFF;
	}

	&__title {
		flex: 1;
		font-size: 26rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__del {
		padding: 4rpx;

		&:active { opacity: 0.7; }
	}
}

.sd-itinerary {
	&__card {
		position: relative;
		padding: 20rpx;
		margin-bottom: 16rpx;
		border-radius: 16rpx;
		background: $sd-input-bg;
		border: 1rpx solid $sd-divider;
	}

	&__head {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 16rpx;
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
		color: #FFF;
	}

	&__idx {
		flex: 1;
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__del {
		padding: 4rpx;

		&:active { opacity: 0.7; }
	}
}

.sd-add-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	padding: 24rpx;
	margin-top: 4rpx;
	border-radius: 16rpx;
	background: $sd-input-bg;
	border: 2rpx dashed $sd-input-border;
	font-size: 26rpx;
	color: var(--ry-text-secondary);
	font-weight: 500;

	&:active {
		border-color: var(--mc-primary, #2979FF);
		color: var(--mc-primary, #2979FF);
		background: var(--mc-primary-light, rgba(47, 123, 255, 0.06));
	}
}

/* ── Footnote & Footer ── */
.sd-footnote {
	display: flex;
	align-items: flex-start;
	gap: 12rpx;
	padding: 20rpx 24rpx !important;
	font-size: 22rpx;
	color: var(--ry-text-hint);
	line-height: 1.65;
	box-shadow: none;
	background: rgba(255, 255, 255, 0.7);
	border: 1rpx dashed $sd-input-border;
}

.sd-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 100;
	padding: 16rpx var(--ry-page-padding-x);
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255, 255, 255, 0.88);
	backdrop-filter: blur(20px);
	box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.06);

	&__inner {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	&__preview {
		flex-shrink: 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 4rpx;
		width: 112rpx;
		height: 96rpx;
		border-radius: 20rpx;
		background: $sd-input-bg;
		border: 2rpx solid $sd-input-border;
		font-size: 22rpx;
		color: var(--ry-text-secondary);
		font-weight: 500;

		&:active { opacity: 0.85; }
	}

	&__save {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		height: 96rpx;
		border-radius: 20rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);

		text {
			font-size: 30rpx;
			font-weight: 700;
			color: #FFF;
			letter-spacing: 1rpx;
		}

		&:active {
			opacity: 0.92;
			transform: scale(0.985);
		}
	}
}
</style>
