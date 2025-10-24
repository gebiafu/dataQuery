import request from '@/utils/request'

// 用户认证API
export const authApi = {
  // 登录
  login(data) {
    return request.post('/auth/login', data)
  },
  // 注册
  register(data) {
    return request.post('/auth/register', data)
  }
}

// 数据源API
export const dataSourceApi = {
  // 创建数据源
  create(data) {
    return request.post('/datasource', data)
  },
  // 更新数据源
  update(id, data) {
    return request.put(`/datasource/${id}`, data)
  },
  // 删除数据源
  delete(id) {
    return request.delete(`/datasource/${id}`)
  },
  // 分页查询
  page(params) {
    return request.get('/datasource/page', { params })
  },
  // 查询所有
  list() {
    return request.get('/datasource/list')
  },
  // 查询详情
  getById(id) {
    return request.get(`/datasource/${id}`)
  },
  // 测试连接
  test(data) {
    return request.post('/datasource/test', data)
  }
}

// 事件API
export const eventApi = {
  // 创建事件
  create(data) {
    return request.post('/event', data)
  },
  // 更新事件
  update(id, data) {
    return request.put(`/event/${id}`, data)
  },
  // 删除事件
  delete(id) {
    return request.delete(`/event/${id}`)
  },
  // 分页查询
  page(params) {
    return request.get('/event/page', { params })
  },
  // 查询所有
  list() {
    return request.get('/event/list')
  },
  // 查询详情
  getById(id) {
    return request.get(`/event/${id}`)
  },
  // 执行SQL
  execute(data) {
    return request.post('/event/execute', data)
  }
}

// 调度任务API
export const scheduleApi = {
  // 创建任务
  create(data) {
    return request.post('/schedule', data)
  },
  // 更新任务
  update(id, data) {
    return request.put(`/schedule/${id}`, data)
  },
  // 删除任务
  delete(id) {
    return request.delete(`/schedule/${id}`)
  },
  // 分页查询
  page(params) {
    return request.get('/schedule/page', { params })
  },
  // 查询详情
  getById(id) {
    return request.get(`/schedule/${id}`)
  },
  // 启动任务
  start(id) {
    return request.post(`/schedule/${id}/start`)
  },
  // 停止任务
  stop(id) {
    return request.post(`/schedule/${id}/stop`)
  }
}

// 事件日志API
export const eventLogApi = {
  // 分页查询
  page(params) {
    return request.get('/event-log/page', { params })
  }
}

// 调度日志API
export const scheduleLogApi = {
  // 分页查询
  page(params) {
    return request.get('/schedule-log/page', { params })
  }
}

// 用户管理API
export const userApi = {
  // 分页查询
  page(params) {
    return request.get('/user/page', { params })
  },
  // 创建用户
  create(data) {
    return request.post('/user', data)
  },
  // 更新用户
  update(id, data) {
    return request.put(`/user/${id}`, data)
  },
  // 删除用户
  delete(id) {
    return request.delete(`/user/${id}`)
  },
  // 获取用户详情
  getById(id) {
    return request.get(`/user/${id}`)
  },
  // 重置密码
  resetPassword(id, newPassword) {
    return request.post(`/user/${id}/reset-password`, newPassword, {
      headers: { 'Content-Type': 'text/plain' }
    })
  },
  // 修改密码
  changePassword(data) {
    return request.post('/user/change-password', data)
  }
}
