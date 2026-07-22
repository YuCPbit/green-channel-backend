# 高校绿色通道系统

本仓库是项目唯一集成仓库，但前后端保持独立工程：`backend/` 使用 Java 17、Spring Boot 4.1.0、Maven 多模块和 MySQL，`frontend/` 使用 Vue 3 与 Vite。两者分别安装、构建和启动，不存在前端作为后端 Maven 子模块的关系。

## 模块结构

| 模块 | 默认端口 | 职责 | 当前状态 |
| --- | ---: | --- | --- |
| `backend/common-api` | - | 公共响应、异常、用户上下文、权限注解和跨服务契约 | 已提取 |
| `backend/platform-service` | 8081 | 认证、学生、附件、字典、消息、外部集成、系统管理 | 已迁移，可运行 |
| `backend/gift-service` | 8082 | 绿色通道、大礼包、审核和领取 | B 的业务代码已迁移 |
| `backend/subsidy-service` | 8083 | 困难补助、资助方案、申诉、辅导员事务 | C 的业务代码已迁移 |
| `backend/workstudy-service` | 8084 | 勤工助学 | D 的业务代码已迁移 |
| `backend/dashboard-service` | 8085 | 看板、统计、模块化报表和 WebSocket 推送 | D 的业务代码及模块注册表已迁移 |
| `backend/gateway-service` | 8080 | 前端统一入口及五个后端服务的路由 | 已配置 Spring Cloud Gateway |
| `frontend` | 5173 | 登录菜单和各业务页面，只通过 8080 网关访问后端 | 独立 Vue 工程 |

`backend/pom.xml` 的 `packaging` 是 `pom`，只负责聚合后端模块和统一版本；每个后端模块都有自己的 `pom.xml`。`frontend/` 不由 Maven 管理。

```text
<project-repository>/
├── backend/               # 独立后端工程
│   ├── pom.xml            # Maven 聚合父 POM
│   ├── common-api/
│   ├── *-service/         # 六个后端服务（含网关）
│   └── scripts/
├── frontend/              # 独立 Vue 3 + Vite 工程
└── docs/                  # 项目公共文档
```

## 构建与启动

1. 执行 `docs/03-数据库/数据库设计.sql` 和 `docs/03-数据库/数据库初始化数据.sql`。
2. 进入后端目录验证所有后端模块：

   ```bash
   cd backend
   mvn test
   ```

3. 启动各服务（分别在独立终端执行）：

   ```bash
   DB_USERNAME=root DB_PASSWORD=你的密码 mvn -pl platform-service -am spring-boot:run
   DB_USERNAME=root DB_PASSWORD=你的密码 mvn -pl gift-service -am spring-boot:run
   DB_USERNAME=root DB_PASSWORD=你的密码 mvn -pl subsidy-service -am spring-boot:run
   DB_USERNAME=root DB_PASSWORD=你的密码 mvn -pl workstudy-service -am spring-boot:run
   DB_USERNAME=root DB_PASSWORD=你的密码 mvn -pl dashboard-service -am spring-boot:run
   mvn -pl gateway-service spring-boot:run
   ```

4. 从项目根目录启动前端：

   ```bash
   cd ../frontend
   npm ci
   npm run dev
   ```

   浏览器访问 `http://localhost:5173`；前端统一通过 `http://localhost:8080` 网关调用后端。

5. 网关会按路径将请求转发至对应服务；部署时可通过
   `PLATFORM_SERVICE_URL`、`GIFT_SERVICE_URL`、`SUBSIDY_SERVICE_URL`、`WORKSTUDY_SERVICE_URL`
   和 `DASHBOARD_SERVICE_URL` 覆盖服务地址。生产或其他开发域名通过逗号分隔的
   `APP_CORS_ALLOWED_ORIGINS` 配置，默认只允许 `http://localhost:5173`。

文档入口统一从 `docs/README.md` 进入。开发协作、服务接口、数据库、实施记录及历史归档均已分目录；
不要再通过旧文件名猜测哪个版本有效。禁止提交真实学生数据、数据库密码或个人凭据。
