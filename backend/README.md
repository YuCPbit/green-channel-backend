# 后端工程

本目录是独立的 Maven 多模块后端工程。聚合父 POM 为 `pom.xml`，公共契约位于 `common-api/`，各业务服务和网关位于对应的 `*-service/`。

```bash
mvn test
```

后端只向前端暴露 `gateway-service` 的 8080 统一入口。完整端口、环境变量和启动步骤见项目根目录 `README.md` 与 `docs/04-实施与验收/团队拉取与启动验收.md`。
