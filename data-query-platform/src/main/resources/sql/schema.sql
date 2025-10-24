-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'user',  -- admin(管理员), user(普通用户)
    status INTEGER DEFAULT 1,
    deleted INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 数据源表
CREATE TABLE IF NOT EXISTS data_source (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,  -- mysql, postgresql
    host VARCHAR(100) NOT NULL,
    port INTEGER NOT NULL,
    database_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(200) NOT NULL,
    description TEXT,
    status INTEGER DEFAULT 1,
    create_user_id BIGINT,
    deleted INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 事件表
CREATE TABLE IF NOT EXISTS query_event (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    event_type VARCHAR(20) NOT NULL,  -- query, insert, update, delete
    data_source_id BIGINT NOT NULL,
    sql_content TEXT NOT NULL,
    description TEXT,
    status INTEGER DEFAULT 1,
    create_user_id BIGINT,
    deleted INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (data_source_id) REFERENCES data_source(id)
);

-- 调度任务表
CREATE TABLE IF NOT EXISTS schedule_job (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    event_id BIGINT NOT NULL,
    cron_expression VARCHAR(100) NOT NULL,
    job_status VARCHAR(20) DEFAULT 'STOP',  -- RUNNING, STOP
    description TEXT,
    create_user_id BIGINT,
    deleted INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES query_event(id)
);

-- 调度任务执行日志表
CREATE TABLE IF NOT EXISTS schedule_log (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    job_name VARCHAR(100),
    event_id BIGINT,
    event_name VARCHAR(100),
    execute_status VARCHAR(20),  -- SUCCESS, FAIL
    execute_result TEXT,
    error_message TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration BIGINT,  -- 执行时长(毫秒)
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 事件执行日志表
CREATE TABLE IF NOT EXISTS event_log (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT,
    event_name VARCHAR(100),
    event_type VARCHAR(20),
    data_source_id BIGINT,
    data_source_name VARCHAR(100),
    sql_content TEXT,
    execute_status VARCHAR(20),  -- SUCCESS, FAIL
    execute_result TEXT,
    error_message TEXT,
    execute_user_id BIGINT,
    execute_user_name VARCHAR(50),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_data_source_user ON data_source(create_user_id);
CREATE INDEX idx_query_event_user ON query_event(create_user_id);
CREATE INDEX idx_query_event_datasource ON query_event(data_source_id);
CREATE INDEX idx_schedule_job_event ON schedule_job(event_id);
CREATE INDEX idx_schedule_log_job ON schedule_log(job_id);
CREATE INDEX idx_event_log_event ON event_log(event_id);
CREATE INDEX idx_event_log_user ON event_log(execute_user_id);
CREATE INDEX idx_event_log_time ON event_log(create_time);

-- 插入默认用户 (密码: admin123，MD5加密)
INSERT INTO sys_user (username, password, nickname, email, role) 
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '管理员', 'admin@example.com', 'admin')
ON CONFLICT (username) DO NOTHING;
