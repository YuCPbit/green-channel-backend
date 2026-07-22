# 高校绿色通道系统后端

基于 Java 17、Spring Boot 4.1.0、Maven 多模块和 MySQL 的后端聚合工程。

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

根目录 `pom.xml` 的 `packaging` 是 `pom`，只负责聚合模块和统一版本；每个模块都有自己的 `pom.xml`。这就是项目约定的“多个 Maven 子项目”，不再把全部代码打进同一个根 JAR。

## 构建与启动

1. 执行 `docs/数据库设计.sql` 和 `docs/数据库初始化数据.sql`。
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

4. 前端统一访问 `http://localhost:8080`。网关会按路径将请求转发至对应服务；部署时可通过
   `PLATFORM_SERVICE_URL`、`GIFT_SERVICE_URL`、`SUBSIDY_SERVICE_URL`、`WORKSTUDY_SERVICE_URL`
   和 `DASHBOARD_SERVICE_URL` 覆盖服务地址。

开发账号及协作约定参见 `docs/开发协作规范.md`，网关路径、公共契约和可执行请求参见
`docs/服务接口与调用示例.md`，迁移规则参见 `docs/Maven多模块架构与迁移指南.md`。禁止提交真实学生数据、数据库密码或个人凭据。
