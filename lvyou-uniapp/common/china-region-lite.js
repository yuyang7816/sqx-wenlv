/**
 * 省市区数据与工具（H5 等平台 u-picker 使用完整数据）
 * 微信小程序/App 优先使用原生 picker mode="region"
 */

import {
	pcaTextArr
} from 'element-china-area-data';

export const REGION_TREE = pcaTextArr.map(province => ({
	name: province.label,
	cities: (province.children || []).map(city => ({
		name: city.label,
		districts: (city.children || []).map(district => district.label)
	}))
}));

export function formatRegionText(values = []) {
	const list = (values || []).filter(Boolean);
	if (!list.length) return '';
	if (list.length >= 3 && list[0] === list[1]) {
		return `${list[0]} ${list[2]}`;
	}
	return list.join(' ');
}

export function parseRegionValue(regionText = '') {
	const parts = regionText.trim().split(/\s+/).filter(Boolean);
	if (!parts.length) return [];
	if (parts.length >= 3) return parts.slice(0, 3);
	if (parts.length === 2) return [parts[0], parts[0], parts[1]];
	return [parts[0], parts[0], parts[0]];
}

export function buildRegionColumns(provinceIndex = 0, cityIndex = 0) {
	const province = REGION_TREE[provinceIndex] || REGION_TREE[0];
	const city = province.cities[cityIndex] || province.cities[0];
	return [
		REGION_TREE.map(item => item.name),
		province.cities.map(item => item.name),
		city.districts
	];
}

export function findRegionIndexes(values = []) {
	if (!values.length) return [0, 0, 0];
	const provinceIndex = Math.max(REGION_TREE.findIndex(item => item.name === values[0]), 0);
	const province = REGION_TREE[provinceIndex];
	const cityIndex = Math.max(province.cities.findIndex(item => item.name === values[1]), 0);
	const city = province.cities[cityIndex];
	const districtIndex = Math.max(city.districts.findIndex(item => item === values[2]), 0);
	return [provinceIndex, cityIndex, districtIndex];
}

export function getRegionValuesByIndexes(provinceIndex, cityIndex, districtIndex) {
	const province = REGION_TREE[provinceIndex] || REGION_TREE[0];
	const city = province.cities[cityIndex] || province.cities[0];
	const district = city.districts[districtIndex] || city.districts[0];
	return [province.name, city.name, district];
}
