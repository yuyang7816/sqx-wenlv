/**
 * H5 腾讯地图 GL JS 封装（自定义分类水滴标 + 准确定位点）
 */
import { getTencentMapKey } from './map-config.js';

const GLJS_SRC_BASE = 'https://map.qq.com/api/gljs?v=1.exp';
let sdkPromise = null;
const pinCache = {};

const CATEGORY_PIN = {
	food: { color: '#FF6B4A', label: '餐' },
	parking: { color: '#FFB800', label: 'P' },
	navigation: { color: '#00BCD4', label: '景' }
};

function getTMap() {
	if (typeof window === 'undefined') return null;
	return window.TMap || null;
}

/** 绘制水滴形分类图标，针尖在底部中心，保证落点准确 */
export function createCategoryPinDataUrl(categoryKey = 'navigation', active = false) {
	const cacheKey = `${categoryKey}_${active ? 1 : 0}`;
	if (pinCache[cacheKey]) {
		return pinCache[cacheKey];
	}
	const meta = CATEGORY_PIN[categoryKey] || CATEGORY_PIN.navigation;
	const color = active ? '#FF6600' : meta.color;
	const w = active ? 52 : 42;
	const h = active ? 68 : 56;
	const canvas = document.createElement('canvas');
	canvas.width = w;
	canvas.height = h;
	const ctx = canvas.getContext('2d');
	const cx = w / 2;
	const cy = h * 0.36;
	const r = w * 0.32;

	// 阴影
	ctx.beginPath();
	ctx.ellipse(cx, h - 4, w * 0.22, 3.5, 0, 0, Math.PI * 2);
	ctx.fillStyle = 'rgba(0,0,0,0.18)';
	ctx.fill();

	// 水滴主体
	ctx.beginPath();
	ctx.moveTo(cx, h - 6);
	ctx.bezierCurveTo(cx + r * 1.35, cy + r * 0.9, cx + r * 1.2, cy - r * 0.9, cx, cy - r);
	ctx.bezierCurveTo(cx - r * 1.2, cy - r * 0.9, cx - r * 1.35, cy + r * 0.9, cx, h - 6);
	ctx.closePath();
	ctx.fillStyle = color;
	ctx.fill();
	ctx.strokeStyle = 'rgba(255,255,255,0.55)';
	ctx.lineWidth = 2;
	ctx.stroke();

	// 白圆
	ctx.beginPath();
	ctx.arc(cx, cy, r * 0.72, 0, Math.PI * 2);
	ctx.fillStyle = '#FFFFFF';
	ctx.fill();

	if (active) {
		ctx.beginPath();
		ctx.arc(cx, cy, r * 0.32, 0, Math.PI * 2);
		ctx.fillStyle = color;
		ctx.fill();
	} else {
		ctx.fillStyle = color;
		ctx.font = `bold ${Math.round(r * 0.95)}px Arial, sans-serif`;
		ctx.textAlign = 'center';
		ctx.textBaseline = 'middle';
		ctx.fillText(meta.label, cx, cy + 1);
	}

	const url = canvas.toDataURL('image/png');
	pinCache[cacheKey] = url;
	return url;
}

export function getMarkerIconPath(categoryKey = 'navigation', active = false) {
	if (active) {
		return '/static/map/pin-active.svg';
	}
	const map = {
		food: '/static/map/pin-food.svg',
		parking: '/static/map/pin-parking.svg',
		navigation: '/static/map/pin-scenic.svg'
	};
	return map[categoryKey] || map.navigation;
}

export function loadTencentMapSdk() {
	if (typeof window === 'undefined' || typeof document === 'undefined') {
		return Promise.reject(new Error('仅支持 H5 环境'));
	}
	const existing = getTMap();
	if (existing) {
		return Promise.resolve(existing);
	}
	if (sdkPromise) {
		return sdkPromise;
	}
	const key = getTencentMapKey();
	if (!key) {
		return Promise.reject(new Error('未在 manifest.json 配置腾讯地图 Key'));
	}
	sdkPromise = new Promise((resolve, reject) => {
		const script = document.createElement('script');
		script.src = `${GLJS_SRC_BASE}&key=${encodeURIComponent(key)}`;
		script.async = true;
		script.onload = () => {
			const TMap = getTMap();
			if (!TMap) {
				sdkPromise = null;
				reject(new Error('腾讯地图 SDK 加载失败'));
				return;
			}
			resolve(TMap);
		};
		script.onerror = () => {
			sdkPromise = null;
			reject(new Error('腾讯地图 SDK 脚本加载失败，请检查 Key 与域名白名单'));
		};
		document.head.appendChild(script);
	});
	return sdkPromise;
}

function buildMarkerStyles(TMap, categoryKey) {
	const normalSrc = createCategoryPinDataUrl(categoryKey, false);
	const activeSrc = createCategoryPinDataUrl(categoryKey, true);
	return {
		normal: new TMap.MarkerStyle({
			width: 42,
			height: 56,
			anchor: { x: 21, y: 52 },
			src: normalSrc
		}),
		active: new TMap.MarkerStyle({
			width: 52,
			height: 68,
			anchor: { x: 26, y: 64 },
			src: activeSrc
		})
	};
}

/** 等待容器出现合理宽高，避免 GL 纹理尺寸溢出 */
export function waitForMapContainerSize(el, options = {}) {
	const maxCssPx = options.maxCssPx || 4096;
	const timeoutMs = options.timeoutMs || 3000;
	const minPx = options.minPx || 8;

	return new Promise((resolve, reject) => {
		const started = Date.now();
		const check = () => {
			if (!el || !el.isConnected) {
				reject(new Error('地图容器未就绪'));
				return;
			}
			const rect = el.getBoundingClientRect();
			const w = Math.round(rect.width);
			const h = Math.round(rect.height);
			if (w >= minPx && h >= minPx && w <= maxCssPx && h <= maxCssPx) {
				resolve({ width: w, height: h });
				return;
			}
			if (Date.now() - started >= timeoutMs) {
				reject(new Error(`地图容器尺寸异常(${w}x${h})，请检查布局`));
				return;
			}
			requestAnimationFrame(check);
		};
		check();
	});
}

/**
 * 创建/管理一张腾讯地图实例
 */
export async function createTencentMap(container, options = {}) {
	const TMap = await loadTencentMapSdk();
	const el = typeof container === 'string' ? document.getElementById(container) : container;
	if (!el) {
		throw new Error('地图容器不存在');
	}
	await waitForMapContainerSize(el);
	const lat = Number(options.latitude);
	const lng = Number(options.longitude);
	const zoom = options.zoom != null ? Number(options.zoom) : 16;
	const categoryKey = options.categoryKey || 'navigation';
	const map = new TMap.Map(el, {
		center: new TMap.LatLng(lat, lng),
		zoom,
		viewMode: '2D',
		showControl: false
	});
	// 布局稳定后再校正一次，防止首屏尺寸抖动
	requestAnimationFrame(() => {
		try {
			if (typeof map.resize === 'function') map.resize();
		} catch (e) { /* ignore */ }
	});
	let currentCategory = categoryKey;
	const markerLayer = new TMap.MultiMarker({
		map,
		styles: buildMarkerStyles(TMap, currentCategory),
		geometries: []
	});
	const polylineLayer = new TMap.MultiPolyline({
		map,
		styles: {
			route: new TMap.PolylineStyle({
				color: '#2979FF',
				width: 8,
				borderWidth: 2,
				borderColor: '#FFFFFF',
				lineCap: 'round'
			})
		},
		geometries: []
	});

	let onMarkerClick = null;
	let onCenterChange = null;
	let dragTimer = null;

	markerLayer.on('click', (evt) => {
		const geo = evt?.geometry;
		if (!geo || onMarkerClick == null) return;
		onMarkerClick({
			id: geo.id,
			poiId: geo.properties?.poiId
		});
	});

	map.on('dragend', () => {
		if (dragTimer) clearTimeout(dragTimer);
		dragTimer = setTimeout(() => {
			const center = map.getCenter();
			if (center && onCenterChange) {
				onCenterChange({
					latitude: center.lat,
					longitude: center.lng
				});
			}
		}, 200);
	});

	return {
		map,
		markerLayer,
		setCategory(nextKey) {
			if (!nextKey || nextKey === currentCategory) return;
			currentCategory = nextKey;
			markerLayer.setStyles(buildMarkerStyles(TMap, currentCategory));
		},
		setCenter(latitude, longitude, nextZoom) {
			const center = new TMap.LatLng(Number(latitude), Number(longitude));
			map.setCenter(center);
			if (nextZoom != null) {
				map.setZoom(Number(nextZoom));
			}
		},
		setMarkers(pois = [], activeId = '', categoryKeyArg) {
			if (categoryKeyArg) {
				this.setCategory(categoryKeyArg);
			}
			const geometries = (pois || [])
				.filter((poi) => poi.latitude != null && poi.longitude != null)
				.map((poi, index) => ({
					id: String(poi.id || index + 1),
					styleId: String(poi.id) === String(activeId) ? 'active' : 'normal',
					position: new TMap.LatLng(Number(poi.latitude), Number(poi.longitude)),
					properties: {
						poiId: poi.id,
						title: poi.name || ''
					}
				}));
			markerLayer.setGeometries(geometries);
		},
		setRoute(points = []) {
			const path = (points || [])
				.filter((p) => p.latitude != null && p.longitude != null)
				.map((p) => new TMap.LatLng(Number(p.latitude), Number(p.longitude)));
			if (!path.length) {
				polylineLayer.setGeometries([]);
				return;
			}
			polylineLayer.setGeometries([{
				id: 'route-main',
				styleId: 'route',
				paths: path
			}]);
			try {
				const bounds = new TMap.LatLngBounds();
				path.forEach((pt) => bounds.extend(pt));
				map.fitBounds(bounds, { padding: 72 });
			} catch (e) {
				map.setCenter(path[0]);
			}
		},
		clearRoute() {
			polylineLayer.setGeometries([]);
		},
		fitPois(pois = []) {
			const list = (pois || []).filter((p) => p.latitude != null && p.longitude != null);
			if (!list.length) return;
			if (list.length === 1) {
				this.setCenter(list[0].latitude, list[0].longitude, 17);
				return;
			}
			try {
				const bounds = new TMap.LatLngBounds();
				list.forEach((poi) => {
					bounds.extend(new TMap.LatLng(Number(poi.latitude), Number(poi.longitude)));
				});
				map.fitBounds(bounds, { padding: 80 });
			} catch (e) {
				this.setCenter(list[0].latitude, list[0].longitude, 16);
			}
		},
		onMarkerClick(handler) {
			onMarkerClick = handler;
		},
		onCenterChange(handler) {
			onCenterChange = handler;
		},
		destroy() {
			if (dragTimer) clearTimeout(dragTimer);
			try {
				polylineLayer.setMap(null);
			} catch (e) { /* ignore */ }
			try {
				markerLayer.setMap(null);
			} catch (e) { /* ignore */ }
			try {
				map.destroy();
			} catch (e) { /* ignore */ }
		}
	};
}
