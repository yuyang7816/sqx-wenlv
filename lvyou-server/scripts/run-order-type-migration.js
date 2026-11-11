const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');

(async () => {
	const conn = await mysql.createConnection({
		host: '127.0.0.1',
		user: 'root',
		password: 'hanstg',
		database: 'rongeyou',
		multipleStatements: true
	});

	const [cols] = await conn.query("SHOW COLUMNS FROM tb_order LIKE 'order_type'");
	if (cols.length) {
		console.log('order_type already exists, skip ALTER');
	} else {
		const sqlPath = path.join(__dirname, '../sql/migration/V20260918_lvyou_order_type.sql');
		const sql = fs.readFileSync(sqlPath, 'utf8');
		await conn.query(sql);
		console.log('Migration executed:', sqlPath);
	}

	const [check] = await conn.query("SHOW COLUMNS FROM tb_order LIKE 'order_type'");
	console.log('order_type column:', check[0] ? `${check[0].Field} ${check[0].Type}` : 'NOT FOUND');
	await conn.end();
})().catch((err) => {
	console.error('FAIL:', err.message);
	process.exit(1);
});
