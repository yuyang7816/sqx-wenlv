const fs = require('fs');
const path = require('path');

const pagesJsonPath = path.join(__dirname, '..', 'pages.json');
const raw = fs.readFileSync(pagesJsonPath, 'utf8');
const config = JSON.parse(raw.replace(/\/\/.*$/gm, ''));

for (const page of config.pages) {
	if (page.style) {
		delete page.style.navigationStyle;
		page.style.navigationBarTextStyle = page.style.navigationBarTextStyle || 'black';
		page.style.navigationBarBackgroundColor = page.style.navigationBarBackgroundColor || '#ffffff';
		page.style.h5 = {
			...(page.style.h5 || {}),
			titleNView: false
		};
	}
}

config.globalStyle = {
	...config.globalStyle,
	navigationBarTextStyle: 'black',
	navigationBarBackgroundColor: '#ffffff',
	h5: {
		...(config.globalStyle.h5 || {}),
		titleNView: false
	}
};

const output = JSON.stringify(config, null, '\t')
	.replace(/\n/g, '\r\n')
	.replace(/^/m, '{\r\n\t"easycom":');

// Keep original easycom comment block - simpler to patch manually
const original = fs.readFileSync(pagesJsonPath, 'utf8');
const easycomEnd = original.indexOf('"pages":');
const tailStart = original.lastIndexOf('"globalStyle"');
const easycomPart = original.slice(0, easycomEnd);

const pagesPart = JSON.stringify(config.pages, null, '\t').replace(/\n/g, '\r\n');
const globalPart = `\t"globalStyle": ${JSON.stringify(config.globalStyle, null, '\t').replace(/\n/g, '\r\n')},\r\n\t"tabBar": ${JSON.stringify(config.tabBar, null, '\t').replace(/\n/g, '\r\n')},\r\n\t"uniIdRouter": {}\r\n}\r\n`;

const result = easycomPart + '"pages": ' + pagesPart.replace(/^\t/gm, '\t') + ',\r\n' + globalPart;

fs.writeFileSync(pagesJsonPath, result, 'utf8');
console.log('pages.json updated');
