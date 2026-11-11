const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const TYPES = ['hotel', 'restaurant', 'travel', 'culture'];

function ensurePackageType(file, type) {
	let t = fs.readFileSync(file, 'utf8');
	if (!t.includes(`PACKAGE_STORE_TYPE = '${type}'`)) {
		if (t.includes('PACKAGE_STORE_TYPE')) {
			t = t.replace(/const PACKAGE_STORE_TYPE = '[^']+';/, `const PACKAGE_STORE_TYPE = '${type}';`);
		} else if (t.includes('} = useMerchantTheme();')) {
			t = t.replace(
				/((?:const \{[\s\S]*?\} = useMerchantTheme\(\);))/,
				`const PACKAGE_STORE_TYPE = '${type}';\n$1`
			);
		}
	}

	if (!t.includes('applyStoreType')) {
		t = t.replace(
			'} = useMerchantTheme();',
			', applyStoreType } = useMerchantTheme();'
		);
		// fix double commas / broken patterns
		t = t.replace(',, applyStoreType', ', applyStoreType');
		t = t.replace('{, applyStoreType', '{ applyStoreType');
	}

	if (!t.includes('applyStoreType(PACKAGE_STORE_TYPE)')) {
		t = t.replace(/await initTheme\(\);/g, 'applyStoreType(PACKAGE_STORE_TYPE);\n\tawait initTheme();');
	}

	if (file.endsWith('store-decor.vue')) {
		t = t.replace("const typeKey = ref('hotel');", 'const typeKey = ref(PACKAGE_STORE_TYPE);');
		t = t.replace(/typeKey\.value = data\.typeKey;/, 'typeKey.value = PACKAGE_STORE_TYPE;');
		t = t.replace(
			/if \(data\.typeKey\) applyStoreType\(data\.typeKey\);/,
			'applyStoreType(PACKAGE_STORE_TYPE);'
		);
	}

	fs.writeFileSync(file, t, 'utf8');
}

for (const type of TYPES) {
	for (const page of ['services.vue', 'orders.vue', 'order-detail.vue', 'store-decor.vue']) {
		ensurePackageType(path.join(ROOT, `merchant-${type}`, page), type);
	}
	console.log('locked', type);
}
