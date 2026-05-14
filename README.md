# ThinkBoot

<p align="center">
  <b>轻量级 Spring Boot 3 快速开发框架</b>
</p>

<p align="center">
  <a href="#简介">简介</a> •
  <a href="#特性">特性</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#模块说明">模块说明</a> •
  <a href="#配置说明">配置说明</a> •
  <a href="#使用示例">使用示例</a> •
  <a href="#开源协议">开源协议</a>
</p>

---

## 简介

ThinkBoot 是一个基于 Spring Boot 3 的轻量级快速开发框架，专为 API/客户端服务场景设计。

与若依（RuoYi）等重量级框架不同，ThinkBoot **不包含复杂的角色权限体系**，只提供客户端最常用的 Token 认证功能。框架采用模块化设计，所有第三方依赖均可按需启用，无需复杂配置即可开箱即用。

**设计哲学**：让开发者更关注业务逻辑，减少繁琐的配置工作。

## 特性

- **轻量简洁**：无冗余功能，聚焦 API 服务核心需求
- **模块化设计**：各功能模块独立，按需引入
- **按需加载**：所有第三方依赖通过条件配置加载，不配置不报错
- **开箱即用**：零配置或最小配置即可启动
- **企业级特性**：内置 XSS 防护、接口幂等性、分布式锁、Spring Cache 支持
- **代码生成**：内置代码生成器，一键生成 CRUD 代码
- **多数据源**：内置动态数据源支持，轻松切换
- **统一存储**：MinIO/阿里云 OSS/腾讯云 COS 统一接口
- **免费开源**：遵循 MIT 开源协议，可自由商用

## 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.2.5 |
| Java 版本 | JDK | 17+ |
| 认证鉴权 | Sa-Token | 1.38.0 |
| ORM 框架 | MyBatis-Plus | 3.5.6 |
| 多数据源 | Dynamic Datasource | 4.3.0 |
| 缓存 | Spring Data Redis / Spring Cache | - |
| 连接池 | HikariCP | - |
| 工具库 | Hutool | 5.8.27 |
| API 文档 | SpringDoc (OpenAPI 3) | 2.5.0 |
| 对象存储 | MinIO / 阿里云 OSS / 腾讯云 COS | - |

## 快速开始

### 1. 环境要求

- JDK 17 或以上
- Maven 3.6+
- MySQL 8.0+（可选）
- Redis（可选）

### 2. 引入依赖

在你的 `pom.xml` 中引入 ThinkBoot 父 POM：

```xml
<parent>
    <groupId>com.thinkboot</groupId>
    <artifactId>think-boot</artifactId>
    <version>1.0.0</version>
</parent>
```

引入需要的模块：

```xml
<dependencies>
    <!-- Web 基础模块（统一响应、异常处理、跨域） -->
    <dependency>
        <groupId>com.thinkboot</groupId>
        <artifactId>think-boot-web</artifactId>
    </dependency>
    
    <!-- 认证模块（Sa-Token） -->
    <dependency>
        <groupId>com.thinkboot</groupId>
        <artifactId>think-boot-auth</artifactId>
    </dependency>
    
    <!-- 数据库模块（MyBatis-Plus） -->
    <dependency>
        <groupId>com.thinkboot</groupId>
        <artifactId>think-boot-database</artifactId>
    </dependency>
    
    <!-- Redis 缓存模块 -->
    <dependency>
        <groupId>com.thinkboot</groupId>
        <artifactId>think-boot-redis</artifactId>
    </dependency>
    
    <!-- 缓存抽象层模块（Spring Cache 注解支持） -->
    <dependency>
        <groupId>com.thinkboot</groupId>
        <artifactId>think-boot-cache</artifactId>
    </dependency>
    
    <!-- 安全模块（XSS、幂等性、分布式锁） -->
    <dependency>
        <groupId>com.thinkboot</groupId>
        <artifactId>think-boot-security</artifactId>
    </dependency>
    
    <!-- 对象存储模块 -->
    <dependency>
        <groupId>com.thinkboot</groupId>
        <artifactId>think-boot-storage</artifactId>
    </dependency>
</dependencies>
```

### 3. 配置文件

创建 `application.yml`：

```yaml
server:
  port: 8080

# 数据源配置（不使用时可不配置）
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/your_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
          username: root
          password: your_password
          driver-class-name: com.mysql.cj.jdbc.Driver
  
  # Redis 配置（不使用时可不配置）
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0

# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true

# ThinkBoot 模块开关
think-boot:
  auth:
    enabled: true        # 启用认证模块
  database:
    enabled: true        # 启用数据库模块
  redis:
    enabled: false       # 启用 Redis 模块
  storage:
    type: minio          # 对象存储类型：minio/aliyun/tencent
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: default
  cors:
    enabled: true        # 启用跨域
  swagger:
    enabled: true        # 启用 API 文档
```

### 4. 运行项目

```bash
# 编译
mvn clean install

# 运行示例项目
cd think-boot-example
mvn spring-boot:run
```

访问 API 文档：http://localhost:8080/swagger-ui.html

## 模块说明

### think-boot-core

核心模块，提供基础工具类和常量。

**包含内容**：
- 常量定义：`CommonConstants`、`RedisConstants`
- 枚举：`ResultCode`
- 异常：`BusinessException`
- 工具类：`ServletUtils`、`ExcelUtils`
- 对象转换：`BaseConverter`（MapStruct 接口）

**使用示例**：
```java
// DTO 转实体
@Mapper(componentModel = "spring")
public interface UserConverter extends BaseConverter<UserDTO, User> {
}

// 使用
@Autowired
private UserConverter userConverter;

User user = userConverter.to(userDTO);
List<User> users = userConverter.toList(userDTOList);
```

### think-boot-web

Web 层模块，提供统一响应、异常处理、跨域配置等。

**包含内容**：
- 统一响应：`R<T>` 类
- 分页响应：`PageResult<T>` 类
- 全局异常处理：`GlobalExceptionHandler`
- Jackson 配置：时间格式统一
- CORS 配置：跨域支持
- 日志配置：TraceId 追踪
- Swagger 配置：API 文档

**使用示例**：
```java
@RestController
public class UserController {
    
    @GetMapping("/user/{id}")
    public R<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return R.ok(user);
    }
    
    @PostMapping("/user")
    public R<Void> createUser(@RequestBody User user) {
        userService.save(user);
        return R.ok();
    }
}
```

### think-boot-auth

认证模块，基于 Sa-Token 实现。

**包含内容**：
- 认证配置：`SaTokenConfigure`（支持 Redis 集成）
- 拦截器配置：`SaTokenWebMvcConfig`
- 认证服务：`AuthService`
- 登录用户：`LoginUser`
- 忽略认证注解：`@IgnoreAuth`（与 Sa-Token 原生 `@SaIgnore` 等效）

**Sa-Token 配置**：

ThinkBoot 完整支持 Sa-Token 的所有配置项，在 `application.yml` 中配置即可：

```yaml
# ==========================================
# Sa-Token 配置（文档：https://sa-token.cc）
# ==========================================
sa-token:
    # token 名称（同时也是 cookie 名称，建议前端在 Header 中传递此字段）
    token-name: Authorization
    # token 有效期（单位：秒）默认30天，-1 代表永久有效
    timeout: 2592000
    # token 最低活跃频率（单位：秒），默认-1 代表不限制，永不冻结
    active-timeout: -1
    # 是否允许同一账号多地同时登录（为 true 时允许一起登录，为 false 时新登录挤掉旧登录）
    is-concurrent: true
    # 在多人登录同一账号时，是否共用一个 token（为 true 时所有登录共用一个 token，为 false 时每次登录新建一个 token）
    is-share: false
    # token 风格（uuid、simple-uuid、random-32、random-64、random-128、tik）
    token-style: uuid
    # 是否输出操作日志
    is-log: false
```

**Sa-Token 集成 Redis**（可选）：

Sa-Token 默认将数据保存在内存中（读写速度最快），但存在以下限制：
- 重启后数据会丢失
- 无法在分布式环境中共享数据

集成 Redis 可解决上述问题，实现重启数据不丢失、分布式环境多节点会话一致。

**1. 添加 Maven 依赖**：

```xml
<!-- Sa-Token 整合 RedisTemplate -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-redis-template</artifactId>
    <version>${sa-token.version}</version>
</dependency>

<!-- 提供 Redis 连接池 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

**2. 在 `application.yml` 中配置 Redis**：

```yaml
spring:
  data:
    redis:
      # Redis 数据库索引（默认为 0）
      database: 0
      # Redis 服务器地址
      host: 127.0.0.1
      # Redis 服务器连接端口
      port: 6379
      # Redis 服务器连接密码（默认为空）
      password:
      # 连接超时时间
      timeout: 10s
      lettuce:
        pool:
          # 连接池最大连接数
          max-active: 200
          # 连接池最大阻塞等待时间（使用负值表示没有限制）
          max-wait: -1ms
          # 连接池中的最大空闲连接
          max-idle: 10
          # 连接池中的最小空闲连接
          min-idle: 0
```

> **提示**：SpringBoot3.x 使用 `spring.data.redis`，SpringBoot2.x 使用 `spring.redis`。

**3. 自定义序列化方案**（可选）：

框架默认使用 Jackson 作为 JSON 序列化方案。如需更换，可引入以下依赖：

```xml
<!-- Sa-Token 整合 Fastjson -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-fastjson</artifactId>
    <version>${sa-token.version}</version>
</dependency>

<!-- Sa-Token 整合 Fastjson2 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-fastjson2</artifactId>
    <version>${sa-token.version}</version>
</dependency>
```

> **注意**：
> - 集成 Redis 后，框架自动保存数据，所有上层 API 保持不变
> - `sa-token-redis-template` 版本应与 `sa-token-spring-boot3-starter` 版本一致
> - ThinkBoot 框架已内置 `sa-token-redis-jackson`，默认使用 Jackson 序列化

**使用示例**：
```java
@RestController
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public R<String> login(@RequestParam String username, @RequestParam String password) {
        // 验证用户名密码...
        String token = authService.login(userId, username);
        return R.ok(token);
    }
    
    @GetMapping("/user/info")
    public R<LoginUser> getUserInfo() {
        return R.ok(authService.getLoginUser());
    }
}
```

**跳过认证的方式**：

方式一：使用 `@IgnoreAuth` 注解（推荐）
```java
@RestController
public class AuthController {
    
    @IgnoreAuth
    @PostMapping("/login")
    public R<String> login(@RequestParam String username, @RequestParam String password) {
        // 不需要登录即可访问
        return R.ok("token");
    }
    
    @IgnoreAuth
    @PostMapping("/register")
    public R<Void> register(@RequestBody User user) {
        // 注册接口不需要登录
        return R.ok();
    }
}
```

方式二：使用 Sa-Token 原生 `@SaIgnore` 注解（与 `@IgnoreAuth` 等效）
```java
@RestController
public class AuthController {
    
    @SaIgnore
    @PostMapping("/login")
    public R<String> login(@RequestParam String username, @RequestParam String password) {
        return R.ok("token");
    }
}
```

方式三：在 Controller 类上标注（整个类跳过认证）
```java
@IgnoreAuth
@RestController
public class PublicController {
    // 所有接口都不需要登录
}
```

方式四：在配置文件中配置白名单
```yaml
think-boot:
  auth:
    enabled: true
    exclude-paths:
      - /login
      - /register
      - /api/public/**
```

> **说明**：ThinkBoot 的 `exclude-paths` 配置与 Sa-Token 原生的路由拦截配置**完全兼容**。ThinkBoot 在内部使用 `SaRouter.notMatch()` 实现白名单排除，你也可以直接使用 Sa-Token 原生的 `SaRouter` 进行更复杂的路由鉴权（见下方）。

**使用 Sa-Token 原生路由拦截**（高级用法）：

如果你需要更复杂的路由鉴权规则（如按模块划分不同权限），可以创建自定义配置类覆盖 ThinkBoot 的默认拦截器：

```java
@Configuration
public class CustomSaTokenConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 登录校验 -- 拦截所有路由，排除登录和公开接口
            SaRouter.match("/**")
                .notMatch("/login", "/register", "/api/public/**")
                .check(r -> StpUtil.checkLogin());

            // 权限校验 -- 不同模块校验不同权限
            SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
            SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
            
            // 角色校验
            SaRouter.match("/manager/**", r -> StpUtil.checkRoleOr("admin", "manager"));
        })).addPathPatterns("/**");
    }
}
```

> **提示**：自定义拦截器会覆盖 ThinkBoot 的默认配置，此时 `think-boot.auth.exclude-paths` 配置将不再生效。

**使用 Sa-Token 原生功能**：

ThinkBoot 完全兼容 Sa-Token 的所有原生功能，你可以直接使用：

```java
// 会话登录
StpUtil.login(userId);

// 判断是否登录
StpUtil.isLogin();
StpUtil.checkLogin();

// 获取登录账号 ID
StpUtil.getLoginId();
StpUtil.getLoginIdAsString();
StpUtil.getLoginIdAsLong();

// 获取 token 信息
StpUtil.getTokenValue();
StpUtil.getTokenTimeout();

// 注销登录
StpUtil.logout();
StpUtil.logout(userId);        // 强制指定账号下线
StpUtil.kickout(userId);       // 踢下线
StpUtil.replaced(userId);      // 顶下线

// 角色权限校验（需要时自行配置）
@SaCheckRole("admin")
@SaCheckPermission("user:add")
```

**Sa-Token Session 会话**：

Sa-Token 提供三种类型的 Session，用于缓存高频读写数据：

```java
// 1. Account-Session（基于账号 ID 的会话）
StpUtil.getSession().set("user", user);           // 写入数据
SysUser user = (SysUser) StpUtil.getSession().get("user");  // 读取数据

// 2. Token-Session（基于 Token 的会话）
StpUtil.getTokenSession().set("data", value);     // 写入数据
Object data = StpUtil.getTokenSession().get("data"); // 读取数据

// 3. Custom-Session（自定义会话，以特定值作为 SessionId）
SaSessionCustomUtil.getSessionById("goods-10001").set("stock", 100);
```

> **注意**：SaSession 与 HttpSession 是**完全不同**的两个对象，请勿混用。使用 Sa-Token 时，请在任何情况下均使用 SaSession。

更多 Sa-Token 功能请参考官方文档：[https://sa-token.cc](https://sa-token.cc)

### think-boot-database

数据库模块，基于 MyBatis-Plus 实现。

**包含内容**：
- MyBatis-Plus 配置：分页、乐观锁、防全表更新
- 实体基类：`BaseEntity`（自动填充创建/更新时间）
- 分页查询：`PageQuery`

**使用示例**：
```java
// 实体类
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    private String nickname;
}

// Mapper
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

// Service
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}

// Controller
@GetMapping("/users")
public R<PageResult<User>> list(PageQuery query) {
    Page<User> page = new Page<>(query.getCurrent(), query.getSize());
    Page<User> result = userService.page(page);
    return R.ok(new PageResult<>(result));
}
```

**数据源配置**：

单数据源配置（默认方式，开箱即用）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

多数据源配置（需要时切换）：
```yaml
spring:
  datasource:
    dynamic:
      primary: master          # 默认数据源
      strict: false            # 严格模式：true-未匹配到数据源抛异常，false-未匹配使用默认数据源
      datasource:
        master:                # 主库（写操作）
          url: jdbc:mysql://localhost:3306/db1
          username: root
          password: root123
          driver-class-name: com.mysql.cj.jdbc.Driver
        slave:                 # 从库（读操作）
          url: jdbc:mysql://localhost:3306/db2
          username: root
          password: root123
          driver-class-name: com.mysql.cj.jdbc.Driver
```

**切换数据源示例**：
```java
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    // 使用从库查询
    @DS("slave")
    public List<User> queryFromSlave() {
        return list();
    }
    
    // 使用主库写入（默认）
    public void saveUser(User user) {
        save(user);
    }
}
```

**支持的数据源类型**：
- MySQL（默认内置）
- PostgreSQL
- Oracle
- SQL Server
- 其他 MyBatis-Plus 支持的数据库

> **注意**：使用其他数据库时，需要在 `pom.xml` 中添加对应驱动依赖，并确保 `PaginationInnerInterceptor` 能自动识别数据库类型。

### think-boot-codegen

代码生成模块，基于 MyBatis-Plus Generator 封装。

**包含内容**：
- 代码生成器：`ThinkBootCodeGenerator`
- 使用示例：`CodeGeneratorExample`

**使用示例**：

在你的项目中创建代码生成器类：

```java
import com.baomidou.mybatisplus.annotation.IdType;
import com.thinkboot.codegen.ThinkBootCodeGenerator;

public class CodeGenerator {

    public static void main(String[] args) {
        new ThinkBootCodeGenerator()
                // 数据库连接配置
                .url("jdbc:mysql://localhost:3306/thinkboot?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai")
                .username("root")
                .password("root")
                
                // 要生成的表名（可设置多个）
                .tableName("sys_user", "sys_role", "sys_menu")
                
                // 模块配置
                .moduleName("system")
                
                // 作者信息
                .author("Your Name")
                
                // 代码输出路径
                .outputPath("D:/your-project/src/main/java")
                
                // 忽略表前缀（sys_ 会被忽略，生成 User/Role/Menu）
                .ignoreTablePrefix("sys_")
                
                // 是否继承 BaseEntity
                .useBaseEntity(true)
                
                // 主键类型
                .idType(IdType.ASSIGN_ID)
                
                // 执行生成
                .generate();
    }
}
```

**生成的代码包括**：
- Entity（实体类）：继承 BaseEntity，包含 Swagger 注解
- Mapper（数据访问层）：继承 BaseMapper
- Service（业务逻辑接口）：继承 IService
- ServiceImpl（业务逻辑实现）：继承 ServiceImpl
- Controller（控制器）：REST 风格
- Mapper.xml（MyBatis XML 映射文件）

**支持的配置项**：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| url | 数据库连接 URL | - |
| username | 数据库用户名 | root |
| password | 数据库密码 | root |
| tableName | 要生成的表名 | - |
| moduleName | 模块名称 | - |
| author | 作者 | thinkboot |
| outputPath | 代码输出路径 | 当前目录 |
| parentPackage | 父包名 | com.thinkboot |
| ignoreTablePrefix | 忽略的表前缀 | sys_ |
| useBaseEntity | 是否继承 BaseEntity | false |
| useLogicDelete | 是否启用逻辑删除 | false |
| idType | 主键类型 | ASSIGN_ID |

### think-boot-redis

Redis 缓存模块。

**包含内容**：
- Redis 配置：JSON 序列化
- 工具类：`RedisUtils`

**使用示例**：
```java
@Autowired
private RedisUtils redisUtils;

// 字符串操作
redisUtils.set("key", "value", 3600);
String value = (String) redisUtils.get("key");

// Hash 操作
redisUtils.hSet("user:1", "name", "张三");
String name = (String) redisUtils.hGet("user:1", "name");

// 删除
redisUtils.delete("key");
```

### think-boot-cache

缓存抽象层模块，基于 Spring Cache 实现。

**包含内容**：
- Spring Cache 注解支持：`@Cacheable`、`@CachePut`、`@CacheEvict`
- Redis CacheManager 配置
- Jackson2JsonRedisSerializer 序列化
- 多缓存配置：default（1小时）、short（10分钟）、long（24小时）

**使用示例**：
```java
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    // 查询时缓存数据（1小时过期）
    @Cacheable(value = "user", key = "#id")
    public User getById(Long id) {
        return super.getById(id);
    }
    
    // 更新时同步缓存
    @CachePut(value = "user", key = "#user.id")
    public User updateUser(User user) {
        updateById(user);
        return user;
    }
    
    // 删除时清除缓存
    @CacheEvict(value = "user", key = "#id")
    public void deleteUser(Long id) {
        removeById(id);
    }
    
    // 使用不同过期时间的缓存（short = 10分钟）
    @Cacheable(value = "short", key = "#code")
    public String getSmsCode(String phone) {
        // 生成并发送短信验证码
        return generateCode();
    }
}
```

**支持的注解**：

| 注解 | 说明 | 使用场景 |
|------|------|----------|
| `@Cacheable` | 查询时缓存 | 详情查询、列表查询 |
| `@CachePut` | 更新缓存 | 数据修改后同步缓存 |
| `@CacheEvict` | 清除缓存 | 数据删除时清除缓存 |

### think-boot-security

安全防护模块，提供 XSS 防护、接口幂等性、分布式锁等企业级安全特性。

**包含内容**：
- XSS 防护：自动过滤请求参数中的 XSS 攻击代码
- 接口幂等性：基于 Redis Token 机制，防止重复提交
- 分布式锁：基于 Redis SETNX + Lua 脚本，支持 SpEL 表达式

#### XSS 防护

**开箱即用，无需任何配置**，框架自动拦截以下攻击：
- `<script>` 标签注入
- `javascript:` 协议注入
- `eval()` 函数调用
- 其他 HTML 标签注入

XSS 防护会自动排除 Swagger、API Docs 等开发工具路径。

#### 接口幂等性

**使用示例**：

```java
@RestController
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    // 方式一：自动模式（基于请求参数 MD5 生成幂等 key）
    @PostMapping("/order/create")
    @Idempotent(time = 5, message = "订单提交过于频繁")
    public R<Void> createOrder(@RequestBody OrderDTO dto) {
        orderService.createOrder(dto);
        return R.ok();
    }
    
    // 方式二：Token 模式（前端先获取 Token，提交时携带 Token）
    @PostMapping("/order/submit")
    @Idempotent(useToken = true, message = "Token 已失效")
    public R<Void> submitOrder(
            @RequestHeader("X-Idempotent-Token") String token,
            @RequestBody OrderDTO dto) {
        orderService.createOrder(dto);
        return R.ok();
    }
    
    // 获取幂等 Token
    @GetMapping("/order/token")
    public R<String> getIdempotentToken() {
        String token = idempotentTokenService.getToken();
        return R.ok(token);
    }
}
```

**参数说明**：

| 参数 | 说明 | 默认值 |
|------|------|--------|
| time | 幂等校验时间（秒） | 3 |
| message | 提示信息 | "请勿重复提交" |
| useToken | 是否使用 Token 模式 | false |

**两种模式说明**：
- **自动模式**：基于请求参数的 MD5 值生成幂等 key，适用于参数相同的请求不会重复提交的场景
- **Token 模式**：前端先调用 `/order/token` 获取 Token，提交时在请求头携带 `X-Idempotent-Token`，适用于严格防重复提交的场景

#### 分布式锁

**使用示例**：

```java
@Service
public class InventoryService {
    
    @Autowired
    private ProductMapper productMapper;
    
    // 分布式锁示例：库存扣减
    @DistributedLock(key = "'inventory:' + #productId", leaseTime = 10)
    public void deductStock(Long productId, int quantity) {
        Product product = productMapper.selectById(productId);
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);
    }
    
    // 支持等待时间
    @DistributedLock(key = "'order:lock:' + #orderId", waitTime = 3, leaseTime = 30)
    public void processOrder(Long orderId) {
        // 处理订单逻辑
    }
}
```

**参数说明**：

| 参数 | 说明 | 默认值 |
|------|------|--------|
| key | 锁的 key（支持 SpEL 表达式） | 必填 |
| waitTime | 等待时间（秒） | 3 |
| leaseTime | 锁自动释放时间（秒） | 10 |
| message | 提示信息 | "操作过于频繁，请稍后再试" |

**SpEL 表达式示例**：
- `key = "'user:lock:' + #id"` - 基于方法参数
- `key = "'order:lock:' + #order.id"` - 基于对象属性
- `key = "'cache:lock:' + #result.data"` - 基于返回值

#### 事务管理

**使用示例**：
```java
@Service
public class OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private InventoryService inventoryService;
    
    // 事务回滚示例
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Order order) {
        // 1. 创建订单
        orderMapper.insert(order);
        
        // 2. 扣减库存（如果失败，整个事务回滚）
        inventoryService.deductStock(order.getProductId(), order.getQuantity());
        
        // 3. 如果这里抛出异常，订单和库存都会回滚
        if (order.getAmount() < 0) {
            throw new RuntimeException("订单金额不能为负");
        }
    }
}
```

**推荐配置**：

在 `application.yml` 中优化 HikariCP 连接池参数：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # 最大连接数
      minimum-idle: 5              # 最小空闲连接
      connection-timeout: 30000    # 连接超时（毫秒）
      idle-timeout: 600000         # 空闲连接超时（毫秒）
      max-lifetime: 1800000        # 连接最大生命周期（毫秒）
      pool-name: ThinkBootHikariPool
```

### think-boot-storage

对象存储模块，支持 MinIO、阿里云 OSS、腾讯云 COS。

**包含内容**：
- 统一接口：`StorageService`
- MinIO 实现：`MinioServiceImpl`
- 阿里云 OSS 实现：`AliyunOssServiceImpl`
- 腾讯云 COS 实现：`TencentCosServiceImpl`

**配置示例**：

**MinIO**：
```yaml
think-boot:
  storage:
    type: minio
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: default
```

**阿里云 OSS**：
```yaml
think-boot:
  storage:
    type: aliyun
    aliyun:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: your_access_key_id
      access-key-secret: your_access_key_secret
      bucket-name: your_bucket
```

**腾讯云 COS**：
```yaml
think-boot:
  storage:
    type: tencent
    tencent:
      secret-id: your_secret_id
      secret-key: your_secret_key
      region: ap-guangzhou
      bucket-name: your_bucket
```

**使用示例**：
```java
@Autowired
private StorageService storageService;

// 上传文件
@PostMapping("/upload")
public R<StorageResult> upload(@RequestParam("file") MultipartFile file) {
    StorageResult result = storageService.upload(
        file.getOriginalFilename(), 
        file.getInputStream(), 
        file.getContentType()
    );
    return R.ok(result);
}

// 下载文件
@GetMapping("/download/{key}")
public void download(@PathVariable String key, HttpServletResponse response) {
    try (InputStream is = storageService.download(key)) {
        response.getOutputStream().write(is.readAllBytes());
    }
}

// 删除文件
@DeleteMapping("/file/{key}")
public R<Void> delete(@PathVariable String key) {
    storageService.delete(key);
    return R.ok();
}
```

## 配置说明

### 完整配置参考

```yaml
think-boot:
  # 认证模块
  auth:
    enabled: true              # 是否启用（默认 false）
    exclude-paths:             # 排除路径（可选，与 @IgnoreAuth 注解配合使用）
      - /login
      - /register
  
  # 数据库模块
  database:
    enabled: true              # 是否启用（默认 false）
  
  # Redis 模块
  redis:
    enabled: false             # 是否启用（默认 false）
  
  # 对象存储模块
  storage:
    type: minio                # 存储类型：minio/aliyun/tencent
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket-name: default
    aliyun:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: 
      access-key-secret: 
      bucket-name: 
    tencent:
      secret-id: 
      secret-key: 
      region: ap-guangzhou
      bucket-name: 
  
  # 跨域配置
  cors:
    enabled: true              # 是否启用（默认 true）
  
  # Swagger 文档
  swagger:
    enabled: false             # 是否启用（默认 false）
```

## 使用示例

### 创建 Controller

```java
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户增删改查")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public R<User> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "创建用户")
    public R<Void> create(@RequestBody User user) {
        userService.save(user);
        return R.ok();
    }
    
    @PutMapping
    @Operation(summary = "更新用户")
    public R<Void> update(@RequestBody User user) {
        userService.updateById(user);
        return R.ok();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public R<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok();
    }
    
    @GetMapping("/list")
    @Operation(summary = "分页查询用户")
    public R<PageResult<User>> list(PageQuery query) {
        Page<User> page = new Page<>(query.getCurrent(), query.getSize());
        Page<User> result = userService.page(page);
        return R.ok(new PageResult<>(result));
    }
}
```

### 使用 Excel 工具

```java
@RestController
public class ExcelController {
    
    @PostMapping("/import")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        List<User> users = ExcelUtils.readExcel(file, User.class);
        userService.saveBatch(users);
        return R.ok();
    }
    
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        List<User> users = userService.list();
        ExcelUtils.writeExcel(response, users, "用户列表");
    }
}
```

### 使用限流注解

```java
@RestController
public class ApiController {
    
    // 默认限流：60 秒内最多 100 次请求
    @RateLimit
    @GetMapping("/api/data")
    public R<List<String>> getData() {
        return R.ok(List.of("data1", "data2"));
    }
    
    // IP 维度限流：60 秒内最多 50 次请求
    @RateLimit(key = "api:query", time = 60, count = 50, limitType = RateLimit.LimitType.IP)
    @GetMapping("/api/query")
    public R<String> query() {
        return R.ok("success");
    }
    
    // 用户维度限流：60 秒内最多 20 次请求
    @RateLimit(key = "api:submit", time = 60, count = 20, limitType = RateLimit.LimitType.USER, message = "提交过于频繁")
    @PostMapping("/api/submit")
    public R<Void> submit() {
        return R.ok();
    }
}
```

### 使用操作日志注解

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @OperationLog(title = "用户管理", description = "创建用户", businessType = OperationLog.BusinessType.INSERT)
    @PostMapping
    public R<Void> create(@RequestBody User user) {
        userService.save(user);
        return R.ok();
    }
    
    @OperationLog(title = "用户管理", description = "更新用户", businessType = OperationLog.BusinessType.UPDATE)
    @PutMapping
    public R<Void> update(@RequestBody User user) {
        userService.updateById(user);
        return R.ok();
    }
    
    @OperationLog(title = "用户管理", description = "删除用户", businessType = OperationLog.BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok();
    }
}
```

## 项目结构

```
ThinkBoot/
├── pom.xml                              # 父 POM，统一管理依赖版本
├── think-boot-core/                     # 核心模块
│   └── src/main/java/com/thinkboot/core/
│       ├── constant/                    # 常量
│       ├── enums/                       # 枚举
│       ├── exception/                   # 异常
│       └── utils/                       # 工具类
│           └── excel/                   # Excel 工具
│       └── converter/                   # 对象转换
├── think-boot-web/                      # Web 模块
│   └── src/main/java/com/thinkboot/web/
│       ├── annotation/                  # 注解（限流、操作日志）
│       ├── aspect/                      # AOP 切面
│       ├── config/                      # 配置类
│       │   └── log/                     # 日志配置
│       ├── handler/                     # 异常处理
│       └── result/                      # 响应封装
├── think-boot-auth/                     # 认证模块
│   └── src/main/java/com/thinkboot/auth/
│       ├── annotation/                  # 注解
│       ├── config/                      # Sa-Token 配置
│       ├── domain/                      # 登录用户
│       └── service/                     # 认证服务
├── think-boot-database/                 # 数据库模块
│   └── src/main/java/com/thinkboot/database/
│       ├── config/                      # MyBatis-Plus 配置
│       └── domain/                      # 实体基类
├── think-boot-redis/                    # Redis 模块
│   └── src/main/java/com/thinkboot/redis/
│       ├── config/                      # Redis 配置
│       └── utils/                       # Redis 工具
├── think-boot-storage/                  # 对象存储模块
│   └── src/main/java/com/thinkboot/storage/
│       ├── config/                      # 存储配置
│       ├── domain/                      # 存储结果
│       └── service/                     # 存储服务
├── think-boot-codegen/                  # 代码生成模块
│   └── src/main/java/com/thinkboot/codegen/
│       ├── ThinkBootCodeGenerator       # 代码生成器
│       └── example/                     # 使用示例
├── think-boot-security/                 # 安全模块
│   └── src/main/java/com/thinkboot/security/
│       ├── annotation/                  # 注解（幂等性、分布式锁）
│       ├── aspect/                      # AOP 切面
│       ├── config/                      # 自动配置类
│       ├── filter/                      # XSS 过滤器
│       └── service/                     # 幂等性 Token 服务
├── think-boot-cache/                    # 缓存抽象层模块
│   └── src/main/java/com/thinkboot/cache/
│       └── config/                      # CacheManager 配置
└── think-boot-example/                  # 示例项目
    └── src/main/
        ├── java/                        # Java 代码
        └── resources/
            ├── application.yml          # 配置文件
            └── sql/                     # SQL 脚本
```

## 开源协议

ThinkBoot 基于 [MIT](LICENSE) 开源协议发布，完全免费，可自由商用。

## 质量保障

- **严格测试**：核心模块全部通过单元测试验证，确保无 BUG 后再发布每个版本
- **高性能**：采用异步日志写入、Redis 连接池优化等最佳实践
- **简单易用**：零配置或最小配置即可启动，无需复杂学习成本
- **开箱即用**：引入依赖即可享受完整功能
- **完善文档**：每个模块都有详细的使用示例和配置说明

> 本框架面向企业级项目，每个版本发布前都经过严格的测试，确保每个功能模块的严密性。

## 常见问题（FAQ）

### 1. 不配置某个模块会影响框架启动吗？

**不会！** ThinkBoot 采用 `@ConditionalOnProperty` 条件加载机制，不配置的功能不会加载，不会报错。

例如：不配置 Redis，框架正常运行，只是限流功能会使用警告日志并跳过检查。

### 2. 如何跳过认证？

有三种方式：
- 使用 `@IgnoreAuth` 注解（推荐）
- 在类上标注 `@IgnoreAuth`（整个类跳过认证）
- 在配置文件中使用 `think-boot.auth.exclude-paths`

### 3. 单数据源和多数据源如何选择？

- **单数据源**：90% 的场景使用单数据源即可
- **多数据源**：需要读写分离、分库分表时使用

多数据源配置以注释形式提供在 `application.yml` 中，需要时取消注释即可。

### 4. 代码生成器如何使用？

修改 `think-boot-codegen` 模块中的 `CodeGeneratorExample` 类，配置数据库连接和表名，然后运行 main 方法即可生成完整的 CRUD 代码。

### 5. 如何使用幂等性防止重复提交？

有两种方式：
- **自动模式**：基于请求参数的 MD5 值生成幂等 key
  ```java
  @Idempotent(time = 5, message = "请勿重复提交")
  @PostMapping("/order")
  public R<Void> createOrder(@RequestBody OrderDTO dto) { ... }
  ```
- **Token 模式**：前端先获取 Token，提交时携带 Token
  ```java
  @Idempotent(useToken = true, message = "Token 已失效")
  @PostMapping("/order")
  public R<Void> createOrder(@RequestHeader("X-Idempotent-Token") String token, @RequestBody OrderDTO dto) { ... }
  ```

### 6. 如何使用分布式锁？

在方法上添加 `@DistributedLock` 注解，支持 SpEL 表达式：

```java
@DistributedLock(key = "'inventory:' + #productId", leaseTime = 10)
public void deductStock(Long productId, int quantity) { ... }
```

### 7. 如何使用 Spring Cache？

直接使用 Spring 的 `@Cacheable`、`@CachePut`、`@CacheEvict` 注解即可：

```java
@Cacheable(value = "user", key = "#id")
public User getById(Long id) {
    return super.getById(id);
}
```

## 最佳实践

### 1. 按需引入模块

不要一次性引入所有模块，只引入需要的模块。例如：
- 如果只是提供 REST API，引入 `think-boot-web` 和 `think-boot-auth` 即可
- 如果需要缓存，再引入 `think-boot-redis`

### 2. 使用 @IgnoreAuth 注解

在 Controller 方法上使用 `@IgnoreAuth` 注解跳过认证，比在配置文件中配置更灵活。

### 3. 使用 BaseEntity

让实体类继承 `BaseEntity`，自动包含 `createdTime`、`updatedTime` 等审计字段，MyBatis-Plus 会自动填充。

### 4. 使用代码生成器

开发新功能时，先使用代码生成器生成基础的 CRUD 代码，然后在此基础上添加业务逻辑。

### 5. 单元测试

为业务代码编写单元测试，ThinkBoot 的核心模块已经覆盖了完整的单元测试，确保功能正确性。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 仓库地址

- GitHub: https://github.com/hongxinge/ThinkBoot
- Gitee: https://gitee.com/hongxinge/think-boot