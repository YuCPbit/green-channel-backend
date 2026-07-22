# Maven 多模块架构与迁移指南

## 1. 目标结构

根项目是聚合父项目，不产生可执行 JAR。公共契约进入 `common-api`，各业务域进入独立服务模块，前端只调用 `gateway-service` 的 8080 端口。

```text
green-channel-backend/
├── pom.xml
├── common-api/pom.xml
├── platform-service/pom.xml
├── gift-service/pom.xml
├── subsidy-service/pom.xml
├── workstudy-service/pom.xml
├── dashboard-service/pom.xml
└── gateway-service/pom.xml
```

## 2. 依赖方向

- 所有服务可以依赖 `common-api`。
- `common-api` 不依赖任何业务服务，也不包含控制器、数据库实现或启动类。
- 业务服务之间不能通过 Maven 直接依赖彼此的实现代码；需要共享的 DTO、错误码和鉴权上下文放入 `common-api`，业务调用通过 HTTP/网关完成。
- 数据库表先允许共用一个 MySQL 实例，但表所有权必须按服务明确，禁止跨服务直接调用对方 Repository。

## 3. 代码迁移顺序

1. A 的既有公共平台代码整体迁入 `platform-service`，再提取公共契约到 `common-api`。
2. B 将绿色通道和礼包代码迁入 `gift-service`；B/C 的资助、申诉与事务代码迁入 `subsidy-service`。
3. D 将勤工助学迁入 `workstudy-service`，看板报表迁入 `dashboard-service`。
4. 为每个业务服务添加独立启动类、配置文件和端口，再实现 `gateway-service` 路由。
5. 根目录执行 `mvn test`，通过后再进行前后端联调。

## 4. 合并规则

- 每位同学从最新集成分支创建自己的功能分支，不再复制一份根项目。
- PR 只修改自己负责的服务模块；修改 `common-api` 时必须说明影响的调用方。
- 合并前至少执行 `mvn test`，并在 PR 中写清 Jira 编号、接口变更和数据库脚本。
- 不要把未验证的完整业务仓库直接覆盖到某个子目录；先对比包名、配置、数据库脚本和接口路径，再迁移。
