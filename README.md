# 高校绿色通道系统后端

基于 Java 17、Spring Boot 4.1.0、Maven 多模块和 MySQL 的后端聚合工程。

## 模块结构

| 模块 | 默认端口 | 职责 | 当前状态 |
| --- | ---: | --- | --- |
| `common-api` | - | 公共响应、异常、用户上下文、权限注解和跨服务契约 | 已提取 |
| `platform-service` | 8081 | 认证、学生、附件、字典、消息、外部集成、系统管理 | 已迁移，可运行 |
| `gift-service` | 8082 | 绿色通道、大礼包、审核和领取 | 已建 Maven 子项目，待迁入 B 的代码 |
| `subsidy-service` | 8083 | 困难补助、资助方案、申诉、辅导员事务 | 已建 Maven 子项目，待迁入 B/C 的代码 |
| `workstudy-service` | 8084 | 勤工助学 | 已建 Maven 子项目，待迁入 D 的代码 |
| `dashboard-service` | 8085 | 看板、统计、报表 | 已建 Maven 子项目，待迁入 D 的代码 |
| `gateway-service` | 8080 | 前端统一入口、路由与鉴权传递 | 已建 Maven 子项目，待实现 |

根目录 `pom.xml` 的 `packaging` 是 `pom`，只负责聚合模块和统一版本；每个模块都有自己的 `pom.xml`。这就是项目约定的“多个 Maven 子项目”，不再把全部代码打进同一个根 JAR。

## 构建与启动

1. 执行 `docs/数据库设计.sql` 和 `docs/数据库初始化数据.sql`。
2. 在根目录验证所有模块：

   ```bash
   mvn test
   ```

3. 当前先启动公共平台服务：

   ```bash
   DB_USERNAME=root DB_PASSWORD=你的密码 mvn -pl platform-service -am spring-boot:run
   ```

4. 健康检查：`GET http://localhost:8081/api/health`。

开发账号及协作约定参见 `docs/开发协作规范.md`，迁移规则参见 `docs/Maven多模块架构与迁移指南.md`。禁止提交真实学生数据、数据库密码或个人凭据。
