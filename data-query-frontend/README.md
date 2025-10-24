# 数据查询平台 - 前端

基于 Vue 3 + Element Plus 的数据查询平台前端应用。

## 快速开始

### 安装依赖
```bash
npm install
```

### 开发模式
```bash
npm run dev
```

应用将在 http://localhost:3000 启动

### 生产构建
```bash
npm run build
```

### 预览构建结果
```bash
npm run preview
```

## 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0

## 主要功能

- 用户登录认证
- 数据源管理（MySQL、PostgreSQL）
- SQL事件管理和测试
- 定时调度任务管理
- 执行日志查询

## 技术栈

- Vue 3
- Vite
- Element Plus
- Vue Router
- Pinia
- Axios

## 项目结构

```
src/
├── api/           # API接口
├── assets/        # 静态资源
├── router/        # 路由配置
├── utils/         # 工具函数
├── views/         # 页面组件
├── App.vue        # 根组件
└── main.js        # 入口文件
```

## 默认账号

- 用户名: admin
- 密码: admin123
