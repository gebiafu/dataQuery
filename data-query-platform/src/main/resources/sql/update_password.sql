-- 更新管理员密码为 admin123 的MD5值
-- 如果数据库中已存在用户，执行此脚本更新密码

UPDATE sys_user 
SET password = '0192023a7bbd73250516f069df18b500' 
WHERE username = 'admin';

-- 查看更新结果
SELECT id, username, nickname, email, status, create_time 
FROM sys_user 
WHERE username = 'admin';
