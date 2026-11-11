# 更新日志

所有版本变更记录于此。版本遵循语义化版本规范，日期格式为 `YYYY-MM-DD`。

## [1.0.0] - 2026-11-11

首个带版本标记的可评估版本。定位：面向客户端 API 的服务端开发框架，不含管理端页面与复杂角色权限体系。

### 修复

- **默认配置无法启动**：`logback-spring.xml` 引用了 Logback 1.4 中不存在的 `ch.qos.logback.core.filter.ThresholdFilter`，改为 `classic.filter.ThresholdFilter`。
- **Redis 增强链路静默失效**：限流 / 幂等 / 分布式锁 / 缓存改为默认启用，依赖缺失时启动显式失败，不再静默降级。
- **分布式锁 Lua 报错**：`ERR Lua redis lib command arguments must be strings or integers`，改用独立 `StringRedisTemplate` 执行脚本。
- **免密签发 Token**：删除示例中的 `/api/v1/auth/login` 无密码登录入口。
- **畸形 JSON 返回 500**：补充 `HttpMessageNotReadableException` → 400 处理器。
- **认证失败响应结构不一致**：统一返回 `R` 结构。
- **客户端 API 泄露密码哈希**：`User.password` 增加 `WRITE_ONLY`，并加入序列化契约与端到端回归门禁。
- 其他加固：缓存命名空间冲突、幂等 key 缺少用户维度、幂等 Token 非原子消费、锁值可预测、三套对象存储实现缺少 `validateKey`。

### 工程化

- 移除 `think-boot-example` 的 `skipTests=true`；新增启动装配契约、代码契约与端到端冒烟测试，全仓用例由 189 增至 202。
- 数据库凭据支持 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` 环境变量覆盖。
- 新增 GitHub Actions CI：全量构建测试 + 真实 MySQL / Redis 下的默认配置启动冒烟。
- README 补齐核心设计思想与敏感字段最佳实践，并同步默认配置、分发方式与实测结论。

### 已验证

- `mvn -B clean package`：11 个模块 SUCCESS、202 条用例全绿。
- JDK 17 + MySQL 8.4.13 + Redis 真机：默认配置启动、登录鉴权、幂等 / 分布式锁、Spring Cache、分页、异常映射、注销全部通过，日志 ERROR 为 0。
- 数据库验证范围：MySQL 8.4.13（本机真机）与 MySQL 8.0（CI）；H2 仅用于测试上下文。PostgreSQL / Oracle / SQL Server 未做自动化验证。

### 分发方式

- 框架以**源码工程**形式分发：从 Gitee / GitHub 下载，执行 `mvn -B clean install` 安装到本机 Maven 仓库后即可开始业务开发；团队内各自安装即可，无需搭建私有仓库。`1.0.0` 未发布到 Maven Central，也不要求发布。详见 README「2. 引入依赖」。
- 运行时 ORM 层无方言耦合，但开箱即用体验只对 MySQL 成立；其它关系库需自备驱动与建表脚本，详见 README「think-boot-database」。

### 已知限制

- 三套对象存储 SDK（MinIO / 阿里云 OSS / 腾讯云 COS）依据官方文档实现，本版本暂未安排真机服务验证。
- XSS 规则尚未配置化；Redis 多态反序列化白名单待收紧。
- 示例未使用 `@OperationLog` 注解，操作日志表默认无数据。
- CI 工作流以 GitHub 为准，Gitee 仓库不运行该工作流。
