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

	const sqlPath = path.join(__dirname, '../sql/migration/V20260918_hotel_booking_lifecycle.sql');
	const sql = fs.readFileSync(sqlPath, 'utf8');
	await conn.query(sql);
	console.log('Migration executed:', sqlPath);

	const [table] = await conn.query("SHOW TABLES LIKE 'tb_hotel_room_calendar'");
	console.log('tb_hotel_room_calendar:', table.length ? 'OK' : 'MISSING');

	const cols = ['room_count', 'pay_expire_time', 'expected_arrival_time', 'price_snapshot_json', 'cancel_policy_json'];
	for (const col of cols) {
		const [rows] = await conn.query('SHOW COLUMNS FROM tb_order LIKE ?', [col]);
		console.log(`tb_order.${col}:`, rows[0] ? rows[0].Type : 'NOT FOUND');
	}
	await conn.end();
})().catch((err) => {
	console.error('FAIL:', err.message);
	process.exit(1);
});
