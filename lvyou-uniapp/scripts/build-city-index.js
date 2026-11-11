const fs = require('fs');
const path = require('path');
const { pcaTextArr } = require('element-china-area-data');

const LETTER = {
	北: 'B', 天: 'T', 石: 'S', 唐: 'T', 秦: 'Q', 邯: 'H', 邢: 'X', 保: 'B', 张: 'Z', 承: 'C',
	沧: 'C', 廊: 'L', 衡: 'H', 太: 'T', 大: 'D', 阳: 'Y', 长: 'C', 晋: 'J', 朔: 'S', 运: 'Y',
	忻: 'X', 临: 'L', 吕: 'L', 呼: 'H', 包: 'B', 乌: 'W', 赤: 'C', 通: 'T', 鄂: 'E', 巴: 'B',
	兴: 'X', 锡: 'X', 阿: 'A', 沈: 'S', 鞍: 'A', 抚: 'F', 本: 'B', 丹: 'D', 锦: 'J', 营: 'Y',
	阜: 'F', 辽: 'L', 盘: 'P', 铁: 'T', 朝: 'C', 葫: 'H', 吉: 'J', 四: 'S', 白: 'B', 松: 'S',
	延: 'Y', 哈: 'H', 齐: 'Q', 鸡: 'J', 鹤: 'H', 双: 'S', 伊: 'Y', 佳: 'J', 七: 'Q', 牡: 'M',
	黑: 'H', 绥: 'S', 上: 'S', 南: 'N', 无: 'W', 徐: 'X', 常: 'C', 苏: 'S', 连: 'L', 淮: 'H',
	盐: 'Y', 扬: 'Y', 镇: 'Z', 泰: 'T', 宿: 'S', 杭: 'H', 宁: 'N', 温: 'W', 嘉: 'J', 湖: 'H',
	绍: 'S', 金: 'J', 衢: 'Q', 舟: 'Z', 台: 'T', 丽: 'L', 合: 'H', 芜: 'W', 蚌: 'B', 马: 'M',
	铜: 'T', 安: 'A', 黄: 'H', 滁: 'C', 六: 'L', 亳: 'B', 池: 'C', 宣: 'X', 福: 'F', 厦: 'X',
	莆: 'P', 三: 'S', 泉: 'Q', 漳: 'Z', 龙: 'L', 景: 'J', 萍: 'P', 九: 'J', 新: 'X', 鹰: 'Y',
	赣: 'G', 宜: 'Y', 济: 'J', 青: 'Q', 淄: 'Z', 枣: 'Z', 东: 'D', 烟: 'Y', 潍: 'W', 威: 'W',
	日: 'R', 德: 'D', 聊: 'L', 滨: 'B', 菏: 'H', 郑: 'Z', 开: 'K', 洛: 'L', 平: 'P', 焦: 'J',
	濮: 'P', 许: 'X', 漯: 'L', 商: 'S', 信: 'X', 周: 'Z', 驻: 'Z', 武: 'W', 十: 'S', 襄: 'X',
	荆: 'J', 孝: 'X', 咸: 'X', 随: 'S', 恩: 'E', 株: 'Z', 湘: 'X', 邵: 'S', 岳: 'Y', 益: 'Y',
	郴: 'C', 永: 'Y', 怀: 'H', 娄: 'L', 广: 'G', 韶: 'S', 深: 'S', 珠: 'Z', 汕: 'S', 佛: 'F',
	江: 'J', 湛: 'Z', 茂: 'M', 肇: 'Z', 惠: 'H', 梅: 'M', 河: 'H', 清: 'Q', 中: 'Z', 潮: 'C',
	揭: 'J', 云: 'Y', 柳: 'L', 桂: 'G', 梧: 'W', 防: 'F', 钦: 'Q', 贵: 'G', 玉: 'Y', 百: 'B',
	贺: 'H', 来: 'L', 崇: 'C', 海: 'H', 儋: 'D', 重: 'C', 成: 'C', 自: 'Z', 攀: 'P', 泸: 'L',
	绵: 'M', 遂: 'S', 内: 'N', 乐: 'L', 眉: 'M', 达: 'D', 雅: 'Y', 资: 'Z', 甘: 'G', 凉: 'L',
	遵: 'Z', 毕: 'B', 黔: 'Q', 昆: 'K', 曲: 'Q', 昭: 'Z', 普: 'P', 楚: 'C', 红: 'H', 文: 'W',
	西: 'X', 怒: 'N', 迪: 'D', 拉: 'L', 昌: 'C', 林: 'L', 山: 'S', 那: 'N', 宝: 'B', 渭: 'W',
	汉: 'H', 榆: 'Y', 兰: 'L', 酒: 'J', 庆: 'Q', 定: 'D', 陇: 'L', 果: 'G', 银: 'Y', 吴: 'W',
	固: 'G', 克: 'K', 吐: 'T', 博: 'B', 喀: 'K', 和: 'H', 塔: 'T', 仙: 'X', 潜: 'Q', 神: 'S',
	五: 'W', 琼: 'Q', 万: 'W', 屯: 'T', 澄: 'C', 陵: 'L', 图: 'T', 可: 'K', 胡: 'H'
};

function shortName(name) {
	return String(name || '')
		.replace(/(特别行政区|自治区|自治州|地区|盟|林区)$/, '')
		.replace(/(市|县)$/, '')
		.trim();
}

function letterOf(name) {
	const letter = LETTER[name[0]];
	if (!letter) {
		throw new Error('missing letter for ' + name);
	}
	return letter;
}

const cities = [];
for (const province of pcaTextArr) {
	const provinceName = province.label;
	const children = province.children || [];
	const municipality = /市$/.test(provinceName) && children.every((item) => item.label === '市辖区' || item.label === '县');
	if (municipality) {
		cities.push({ name: provinceName, shortName: shortName(provinceName), province: provinceName, letter: letterOf(provinceName) });
		continue;
	}
	for (const city of children) {
		if (city.label.includes('直辖')) {
			for (const county of city.children || []) {
				cities.push({
					name: county.label,
					shortName: shortName(county.label),
					province: provinceName,
					letter: letterOf(county.label)
				});
			}
			continue;
		}
		if (city.label === '市辖区' || city.label === '县') continue;
		cities.push({
			name: city.label,
			shortName: shortName(city.label),
			province: provinceName,
			letter: letterOf(city.label)
		});
	}
}

cities.sort((a, b) => a.letter.localeCompare(b.letter) || a.name.localeCompare(b.name, 'zh'));
const groups = [];
for (const city of cities) {
	const last = groups[groups.length - 1];
	if (!last || last.letter !== city.letter) {
		groups.push({ letter: city.letter, cities: [city] });
	} else {
		last.cities.push(city);
	}
}

const out = `/** 全国城市索引，供选择页字母列表使用 */\nexport const CITY_INDEX_GROUPS = ${JSON.stringify(groups, null, '\t')};\n`;
const target = path.join(__dirname, '..', 'common', 'city-index.js');
fs.writeFileSync(target, out);
console.log('cities', cities.length, 'groups', groups.length);
