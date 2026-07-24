# 高校绿色通道系统后端

本仓库只存放后端：采用 Java 17、Spring Boot 4.1.0、Maven 多模块和 MySQL。前端位于独立仓库 [`green-channel-frontend`](https://github.com/YuCPbit/green-channel-frontend)，两端通过 8080 网关联调。

## 模块结构

| 模块 | 默认端口 | 职责 | 当前状态 |
| --- | ---: | --- | --- |
| `common-api` | - | 公共响应、异常、用户上下文、权限注解和跨服务契约 | 已提取 |
| `platform-service` | 8081 | 认证、学生、附件、字典、消息、外部集成、系统管理 | 已迁移，可运行 |
| `gift-service` | 8082 | 绿色通道、大礼包、审核和领取 | B 的业务代码已迁移 |
| `subsidy-service` | 8083 | 困难补助、资助方案、申诉、满意度问卷 | B/C 代码已整合，A 已补接管功能 |
| `workstudy-service` | 8084 | 勤工助学、调岗离岗 | B/D 代码已整合，A 已补岗位变动审批 |
| `dashboard-service` | 8085 | 看板、统计、模块化报表和 WebSocket 推送 | D 的业务代码及模块注册表已迁移 |
| `tutor-affair-service` | 8086 | 辅导员事务申请、审批与台账 | B 的业务代码已整合 |
| `gateway-service` | 8080 | 前端统一入口及六个后端业务服务的路由 | 已配置 Spring Cloud Gateway |

根目录 `pom.xml` 的 `packaging` 是 `pom`，只负责聚合模块和统一版本；每个模块都有自己的 `pom.xml`。这就是项目约定的“多个 Maven 子项目”，不再把全部代码打进同一个根 JAR。

```text
green-channel-backend/
├── common-api/            # 后端公共契约
├── *-service/             # 六个后端服务（含网关）
├── docs/
├── scripts/
└── pom.xml
```

## 构建与启动

1. 新库执行 `docs/03-数据库/数据库设计.sql` 和 `docs/03-数据库/数据库初始化数据.sql`；
   已有开发库依次执行 `docs/03-数据库/迁移-2026-07-24-A接管功能.sql` 和
   `docs/03-数据库/迁移-2026-07-24-勤工助学权限与联调.sql`。
2. 在根目录验证所有模块：

   ```bash
   mvn test
   ```

3. 启动各服务（分别在独立终端执行）：

   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=你的本机数据库密码
   export APP_AUTH_TOKEN_SECRET=至少32字符且所有服务完全相同的随机密钥

   mvn -pl platform-service -am spring-boot:run
   mvn -pl gift-service -am spring-boot:run
   mvn -pl subsidy-service -am spring-boot:run
   mvn -pl workstudy-service -am spring-boot:run
   mvn -pl dashboard-service -am spring-boot:run
   mvn -pl tutor-affair-service -am spring-boot:run
   mvn -pl gateway-service spring-boot:run
   ```

4. 网关会按路径将请求转发至对应服务；部署时可通过
   `PLATFORM_SERVICE_URL`、`GIFT_SERVICE_URL`、`SUBSIDY_SERVICE_URL`、`WORKSTUDY_SERVICE_URL`
   和 `DASHBOARD_SERVICE_URL` 覆盖服务地址。生产或其他开发域名通过逗号分隔的
   `APP_CORS_ALLOWED_ORIGINS` 配置，默认只允许前端开发地址 `http://localhost:5173`。前端安装和启动方式见前端仓库 README。

文档入口统一从 `docs/README.md` 进入。开发协作、服务接口、数据库、实施记录及历史归档均已分目录；
不要再通过旧文件名猜测哪个版本有效。禁止提交真实学生数据、数据库密码或个人凭据。
后端不再提供通用 Token 密钥回退值；未设置 `APP_AUTH_TOKEN_SECRET` 时服务会拒绝启动。
