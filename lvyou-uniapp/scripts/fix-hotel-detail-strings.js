const fs = require('fs');
const path = require('path');

const file = path.join(__dirname, '..', 'pages', 'mobile-design', 'hotel', 'detail.vue');
let s = fs.readFileSync(file, 'utf8');

s = s.replace(/<text class="hotel-footer">[^<]*<\/text>/, '<text class="hotel-footer">荣昌品质住宿 · 舒心之选</text>');
s = s.replace(
	/<text class="hotel-bar__btn-text hotel-bar__btn-text--outline">[^<]*<\/text>/,
	'<text class="hotel-bar__btn-text hotel-bar__btn-text--outline">联系前台</text>'
);
s = s.replace(
	/(hotel-bar__btn--primary[\s\S]*?<text class="hotel-bar__btn-text">)[^<]*(<\/text>)/,
	'$1立即预定$2'
);
s = s.replace(/<text class="hotel-room__unit">[^<]*<\/text>/, '<text class="hotel-room__unit">/晚</text>');
s = s.replace(/<text class="hotel-room__btn-text">[^<]*<\/text>/, '<text class="hotel-room__btn-text">联系商户</text>');

fs.writeFileSync(file, s, 'utf8');
console.log('hotel detail template strings fixed');
