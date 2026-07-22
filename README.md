# 高校绿色通道系统

前后端唯一仓库：后端采用 Java 17、Spring Boot 4.1.0、Maven 多模块和 MySQL，前端采用 Vue 3 与 Vite。

## 模块结构

| 模块 | 默认端口 | 职责 | 当前状态 |
| --- | ---: | --- | --- |
| `common-api` | - | 公共响应、异常、用户上下文、权限注解和跨服务契约 | 已提取 |
| `platform-service` | 8081 | 认证、学生、附件、字典、消息、外部集成、系统管理 | 已迁移，可运行 |
| `gift-service` | 8082 | 绿色通道、大礼包、审核和领取 | B 的业务代码已迁移 |
| `subsidy-service` | 8083 | 困难补助、资助方案、申诉、辅导员事务 | C 的业务代码已迁移 |
| `workstudy-service` | 8084 | 勤工助学 | D 的业务代码已迁移 |
| `dashboard-service` | 8085 | 看板、统计、模块化报表和 WebSocket 推送 | D 的业务代码及模块注册表已迁移 |
| `gateway-service` | 8080 | 前端统一入口及五个后端服务的路由 | 已配置 Spring Cloud Gateway |
| `frontend` | 5173 | 登录菜单和各业务页面，只通过 8080 网关访问后端 | 已并入唯一仓库 |

根目录 `pom.xml` 的 `packaging` 是 `pom`，只负责聚合模块和统一版本；每个模块都有自己的 `pom.xml`。这就是项目约定的“多个 Maven 子项目”，不再把全部代码打进同一个根 JAR。

```text
green-channel-backend/
├── frontend/              # Vue 3 + Vite
├── common-api/            # 后端公共契约
├── *-service/             # 六个后端服务（含网关）
├── docs/
├── scripts/
└── pom.xml
```

## 构建与启动

1. 执行 `docs/03-数据库/数据库设计.sql` 和 `docs/03-数据库/数据库初始化数据.sql`。
2. 在根目录验证所有模块：

   ```bash
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

4. 启动前端：

   ```bash
   cd frontend
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
