# 高校绿色通道系统后端

基于 Java 17、Spring Boot 4 和 MySQL 的高校绿色通道系统后端服务。

## 本地启动

1. 执行 `docs/数据库设计.sql` 和 `docs/数据库初始化数据.sql`。
2. 配置数据库后启动：

   ```bash
   DB_USERNAME=root DB_PASSWORD=你的密码 mvn spring-boot:run
   ```

3. 健康检查：`GET http://localhost:8080/api/health`。

开发账号及协作约定参见 `docs/开发协作规范.md`。禁止提交真实学生数据、数据库密码或个人凭据。

