const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');

(async () => {
	const conn = await mysql.createConnection({
		host: '127.0.0.1',
		user: 'root',
		password: 'hanstg',
		database: 'rongeyou'
	});

	const [cols] = await conn.query("SHOW COLUMNS FROM tb_order LIKE 'verify_time'");
	if (cols.length) {
		console.log('verify_time already exists, skip ALTER');
	} else {
		const sqlPath = path.join(__dirname, '../sql/migration/V20260918_lvyou_order_verify.sql');
		const sql = fs.readFileSync(sqlPath, 'utf8');
		await conn.query(sql);
		console.log('Migration executed:', sqlPath);
	}

	const [check] = await conn.query("SHOW COLUMNS FROM tb_order LIKE 'verify_time'");
	console.log('verify_time column:', check[0] ? `${check[0].Field} ${check[0].Type}` : 'NOT FOUND');
	await conn.end();
})().catch((err) => {
	console.error('FAIL:', err.message);
	process.exit(1);
});
