/**
 * 将各业态分包内页面路径改为本包路径，酒店房型组件改为本包引用
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const TYPES = ['hotel', 'restaurant', 'travel', 'culture'];

function patchFile(filePath, replacements) {
	if (!fs.existsSync(filePath)) return false;
	let text = fs.readFileSync(filePath, 'utf8');
	let next = text;
	for (const [from, to] of replacements) {
		next = next.split(from).join(to);
	}
	if (next !== text) {
		fs.writeFileSync(filePath, next, 'utf8');
		return true;
	}
	return false;
}

for (const type of TYPES) {
	const pkg = `merchant-${type}`;
	const dir = path.join(ROOT, pkg);
	const pages = ['services.vue', 'service-edit.vue', 'store-decor.vue', 'orders.vue', 'order-detail.vue'];
	for (const page of pages) {
		const file = path.join(dir, page);
		patchFile(file, [
			['/merchant-portal/center/service-edit', `/${pkg}/service-edit`],
			['/merchant-portal/center/services', `/${pkg}/services`],
			['/merchant-portal/center/store-decor', `/${pkg}/store-decor`],
			['/merchant-portal/center/orders', `/${pkg}/orders`],
			['/merchant-portal/center/order-detail', `/${pkg}/order-detail`],
			['/merchant-portal/center/room-standards', '/merchant-hotel/room-standards'],
			['/merchant-portal/center/room-standard-edit', '/merchant-hotel/room-standard-edit'],
			["@/merchant-portal/center/components/hotel-room-fields.vue", '@/merchant-hotel/components/hotel-room-fields.vue']
		]);
	}
}

// hotel room pages
for (const page of ['room-standards.vue', 'room-standard-edit.vue']) {
	patchFile(path.join(ROOT, 'merchant-hotel', page), [
		['/merchant-portal/center/room-standard-edit', '/merchant-hotel/room-standard-edit'],
		['/merchant-portal/center/room-standards', '/merchant-hotel/room-standards'],
		["@/merchant-portal/center/components/hotel-room-fields.vue", '@/merchant-hotel/components/hotel-room-fields.vue']
	]);
}

console.log('patched merchant biz packages');
