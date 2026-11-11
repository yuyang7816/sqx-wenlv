const mysql = require('D:/ideaproject/luyingshebeizulin_work/luyingzulin_admin/node_modules/mysql2/promise');

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'hanstg' });
  for (const db of ['luyingzulin', 'rongeyou']) {
    const [rows] = await conn.query(
      `SELECT param_key, param_value FROM \`${db}\`.sys_config
       WHERE param_key IN ('sys_h5_domain','sys_domain','file_local_upload_path','file_local_root_path','pay_ali_appid','pay_ali_sandbox')
       ORDER BY param_key`
    );
    console.log('---', db);
    rows.forEach((r) => console.log(r.param_key, r.param_value));
  }
  await conn.end();
})();
