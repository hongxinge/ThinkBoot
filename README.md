# ThinkBoot

<p align="center">
  <b>轻量级 Spring Boot 3 快速开发框架</b>
</p>

<p align="center">
  <a href="#-简介">简介</a> •
  <a href="#-特性">特性</a> •
  <a href="#-快速开始">快速开始</a> •
  <a href="#-模块说明">模块说明</a> •
  <a href="#-配置说明">配置说明</a> •
  <a href="#-使用示例">使用示例</a> •
  <a href="#-开源协议">开源协议</a>
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
| 缓存 | Spring Data Redis | - |
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
- 认证配置：`SaTokenConfigure`
- 拦截器配置：`SaTokenWebMvcConfig`
- 认证服务：`AuthService`
- 登录用户：`LoginUser`
- 忽略认证注解：`@IgnoreAuth`

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

方式二：在 Controller 类上标注（整个类跳过认证）
```java
@IgnoreAuth
@RestController
public class PublicController {
    // 所有接口都不需要登录
}
```

方式三：在配置文件中配置白名单
```yaml
think-boot:
  auth:
    enabled: true
    exclude-paths:
      - /login
      - /register
      - /api/public/**
```

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

**多数据源配置**：
```yaml
spring:
  datasource:
    dynamic:
      primary: master
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/db1
          username: root
          password: root123
        slave:
          url: jdbc:mysql://localhost:3306/db2
          username: root
          password: root123
```

```java
// 使用从库
@DS("slave")
public List<User> queryFromSlave() {
    return list();
}
```

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
└── think-boot-example/                  # 示例项目
    └── src/main/
        ├── java/                        # Java 代码
        └── resources/
            ├── application.yml          # 配置文件
            └── sql/                     # SQL 脚本
```

## 开源协议

ThinkBoot 基于 [MIT](LICENSE) 开源协议发布，完全免费，可自由商用。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 仓库地址

- Gitee: https://gitee.com/hongxinge/think-boot