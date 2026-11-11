const fs = require('fs');
const path = require('path');
const root = path.join(__dirname, '..');

function fixSchedule() {
	const rel = 'mobile-design/transport/schedule.vue';
	let s = fs.readFileSync(path.join(root, rel), 'utf8');
	s = s.replace(
		/<text class="transport-schedule-table__title">\{\{ activeRoute\.name \}\}[^<]*<\/text>/,
		'<text class="transport-schedule-table__title">{{ activeRoute.name }} 时刻表</text>'
	);
	s = s.replace(
		/<text class="transport-schedule-table__hint">[^<]*<\/text>/,
		'<text class="transport-schedule-table__hint">以下为 mock 时刻</text>'
	);
	fs.writeFileSync(path.join(root, rel), s, 'utf8');
	console.log('fixed', rel);
}

function fixTrack() {
	const rel = 'mobile-design/transport/track.vue';
	const file = path.join(root, rel);
	const lines = fs.readFileSync(file, 'utf8').split(/\r?\n/);
	const headEnd = lines.findIndex((l) => l.includes('transport-track-info__head'));
	const start = lines.findIndex((l, i) => i > headEnd && l.includes('transport-track-info__row'));
	const updatesIdx = lines.findIndex((l) => l.includes('transport-track-updates'));
	let cardClose = updatesIdx - 1;
	while (cardClose > start && !lines[cardClose].trim()) cardClose--;
	if (start < 0 || cardClose <= start) {
		console.error('track row range not found', start, cardClose);
		return;
	}
	const replacement = [
		'\t\t\t\t<view class="transport-track-info__row">',
		'\t\t\t\t\t<text class="transport-track-info__label">当前站</text>',
		'\t\t\t\t\t<text class="transport-track-info__value">{{ trackInfo.currentStation }}</text>',
		'\t\t\t\t</view>',
		'\t\t\t\t<view class="transport-track-info__row">',
		'\t\t\t\t\t<text class="transport-track-info__label">下一站</text>',
		'\t\t\t\t\t<text class="transport-track-info__value">',
		'\t\t\t\t\t\t{{ trackInfo.nextStation }} · {{ trackInfo.eta }}',
		'\t\t\t\t\t</text>',
		'\t\t\t\t</view>',
		'\t\t\t\t<view class="transport-track-info__row">',
		'\t\t\t\t\t<text class="transport-track-info__label">车牌号</text>',
		'\t\t\t\t\t<text class="transport-track-info__value">{{ trackInfo.vehicleNo }}</text>',
		'\t\t\t\t</view>'
	];
	lines.splice(start, cardClose - start, ...replacement);
	fs.writeFileSync(file, lines.join('\n'), 'utf8');
	console.log('fixed', rel);
}

fixSchedule();
fixTrack();
