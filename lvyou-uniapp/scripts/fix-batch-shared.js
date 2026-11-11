const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');

function readStyles(rel) {
	const s = fs.readFileSync(path.join(root, rel), 'utf8');
	const idx = s.indexOf('<style lang="scss" scoped>');
	return idx >= 0 ? s.slice(idx) : '</style>';
}

function patchPage(rel, template, script) {
	const styles = readStyles(rel);
	const content = `${template}\n\n${script}\n\n${styles.startsWith('<style') ? styles : '<style lang="scss" scoped>' + styles}`;
	fs.writeFileSync(path.join(root, rel), content, { encoding: 'utf8' });
}

const MSG_MOCK_MAP = {
	'1': {
		id: 1,
		category: 'system',
		categoryLabel: '系统通知',
		title: '餐饮预约提醒',
		content: '您已成功预约老字号·荣昌卤鹅旗舰店堂食，预约时间：2026年8月10日 12:00，人数 2 人。如需改期请提前联系门店。',
		time: '今天 10:30',
		linkUrl: '/mobile-design/canteen/records',
		linkText: '查看预约'
	},
	'2': {
		id: 2,
		category: 'activity',
		categoryLabel: '活动提醒',
		title: '精彩活动上线',
		content: '荣昌非遗体验周将于 8 月 15 日至 8 月 20 日在万灵古镇举行，包含陶艺体验、卤鹅品鉴等活动，名额有限，请提前预约。',
		time: '昨天 18:00',
		linkUrl: '/mobile-design/heritage/catalog',
		linkText: '查看活动'
	},
	'3': {
		id: 3,
		category: 'system',
		categoryLabel: '系统通知',
		title: '景区预约成功',
		content: '订单号 RY202608081422001 已确认，景区：安陶小镇，预约日期：2026年8月10日，人数 2 人。请携带有效证件按时入园。',
		time: '08-08 14:22',
		linkUrl: '/mobile-design/scenic/detail?id=1',
		linkText: '查看景区'
	},
	'4': {
		id: 4,
		category: 'activity',
		categoryLabel: '活动提醒',
		title: '景区暑期优惠',
		content: '濑溪河湿地公园暑期门票 8 折优惠，活动至 2026 年 8 月 31 日。适合亲子休闲与摄影打卡，欢迎预约入园。',
		time: '08-05 09:15',
		linkUrl: '/mobile-design/scenic/detail?id=1',
		linkText: '查看景区'
	},
	'5': {
		id: 5,
		category: 'system',
		categoryLabel: '系统通知',
		title: '反馈已回复',
		content: '您于 2026 年 7 月 30 日提交的反馈（编号 FB202607301640）已处理完毕，预计 1-3 个工作日内完成优化。如有疑问请联系 023-46789012。',
		time: '08-01 16:40',
		linkUrl: '/mobile-design/feedback/index',
		linkText: '查看反馈'
	}
};

module.exports = { patchPage, readStyles, root, MSG_MOCK_MAP };
