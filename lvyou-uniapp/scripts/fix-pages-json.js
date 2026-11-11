const fs = require('fs');
const path = require('path');

const pagesJsonPath = path.join(__dirname, '..', 'pages.json');
let content = fs.readFileSync(pagesJsonPath, 'utf8');

content = content.replace(/,?\s*"navigationStyle"\s*:\s*"custom"/g, '');

if (!content.includes('"titleNView"')) {
	content = content.replace(
		'"backgroundColor": "#F5F7FF"',
		'"backgroundColor": "#F5F7FF",\n\t\t"h5": {\n\t\t\t"titleNView": false\n\t\t}'
	);
}

fs.writeFileSync(pagesJsonPath, content, 'utf8');
console.log('pages.json updated');
