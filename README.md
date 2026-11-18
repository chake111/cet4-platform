# cet4-platform

CET-4 模拟考试平台。前端、后端、部署配置收在同一个仓库，一个地址访问。

## 目录结构

| 目录 | 说明 |
| --- | --- |
| `backend/` | Spring Boot 3 + MyBatis-Plus + MySQL + Redis，端口 8080 |
| `frontend/` | 前端 SPA，Vue 3 + Vite + Pinia + Element Plus |
| `deploy/` | nginx 配置、前端镜像 Dockerfile |

## 一键启动

```bash
docker compose up -d --build
```

| 入口 | 地址 |
| --- | --- |
| 前端 | http://localhost/ |
| 后端健康检查 | http://localhost/api/health |

改端口：`CET4_APP_PORT=8081 docker compose up -d --build`

演示账号（密码均为 `123456`）：

- 学生：`student1` / `student2` / `student3`
- 管理员：`admin1`

## 数据初始化

首次启动 compose 时自动执行 `backend/src/main/resources/sql/` 下的 `init.sql` 与全部种子（用户、2024/2025 年试卷）。需要重置数据时：

```bash
docker compose down -v
docker compose up -d --build
```

## 本地开发

需要本机 MySQL（`cet4_platform` 库）和 Redis：

```bash
# 后端
mvn -f backend/pom.xml spring-boot:run

# 前端（http://localhost:5173/）
cd frontend && npm install && npm run dev
```

## 测试

```bash
mvn -f backend/pom.xml test
cd frontend && npm test
```

前端测试为 282 个 Vitest 用例；后端测试不依赖真实 MySQL/Redis。

## AI 评分

写作与翻译评分调用 DeepSeek，默认 `sk-placeholder` 会降级为 0 分。需要真实评分时设置：

```bash
CET4_DEEPSEEK_API_KEY=sk-xxx docker compose up -d
```