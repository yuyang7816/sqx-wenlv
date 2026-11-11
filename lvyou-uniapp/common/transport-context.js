/**
 * 实时公交页面上下文（城市/参考站/方向）
 */
import { getAppLocationParams } from './location-params.js';

export function buildTransportRouteQuery(route = {}, extra = {}) {
	const query = {
		routeId: route.id || extra.routeId || '',
		city: route.city || extra.city || '',
		station: route.referenceStation || extra.station || '',
		direction: route.direction || extra.direction || 1
	};
	return Object.fromEntries(
		Object.entries({ ...query, ...extra }).filter(([, value]) => value !== '' && value != null)
	);
}

export function serializeTransportQuery(params = {}) {
	return Object.entries(params)
		.filter(([, value]) => value !== '' && value != null)
		.map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
		.join('&');
}

function decodeParam(value) {
	if (value == null || value === '') {
		return '';
	}
	try {
		return decodeURIComponent(String(value));
	} catch (e) {
		return String(value);
	}
}

export function parseTransportOptions(options = {}) {
	return {
		routeId: decodeParam(options.routeId || options.id),
		city: decodeParam(options.city),
		station: decodeParam(options.station),
		direction: Number(options.direction || 1)
	};
}

export async function buildTransportRequestParams(routeContext = {}, extra = {}) {
	const locationQuery = await getAppLocationParams();
	return {
		lat: locationQuery.lat,
		lng: locationQuery.lng,
		city: routeContext.city || extra.city || locationQuery.city || '',
		district: locationQuery.district || '',
		station: routeContext.station || extra.station || '',
		direction: routeContext.direction || extra.direction || 1,
		...extra
	};
}
