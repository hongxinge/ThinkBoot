package com.thinkboot.codegen;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ThinkBoot 代码生成器
 * 基于 MyBatis-Plus Generator 封装，简化配置
 * 
 * 使用示例：
 * <pre>
 * ThinkBootCodeGenerator generator = new ThinkBootCodeGenerator();
 * generator.url("jdbc:mysql://localhost:3306/thinkboot?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai")
 *         .username("root")
 *         .password("root")
 *         .tableName("sys_user", "sys_role")
 *         .moduleName("system")
 *         .author("张三")
 *         .outputPath("D:/project/think-boot-example/src/main/java")
 *         .generate();
 * </pre>
 */
public class ThinkBootCodeGenerator {

    private String url = "jdbc:mysql://localhost:3306/thinkboot?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
    private String username = "root";
    private String password = "root";
    private String[] tableNames = {};
    private String moduleName = "";
    private String author = "thinkboot";
    private String outputPath = System.getProperty("user.dir") + "/src/main/java";
    private boolean enableSwagger = true;
    private boolean enableLombok = true;
    private boolean enableRestStyle = true;
    private boolean enableEntityFile = true;
    private boolean enableMapperFile = true;
    private boolean enableServiceFile = true;
    private boolean enableServiceImplFile = true;
    private boolean enableControllerFile = true;
    private String parentPackage = "com.thinkboot";
    private String entityBaseClass = "BaseEntity";
    private boolean useBaseEntity = false;
    private String[] ignoreTablePrefix = {"sys_"};
    private IdType idType = IdType.ASSIGN_ID;
    private boolean useLogicDelete = false;
    private String logicDeleteField = "deleted";

    /**
     * 设置数据库连接 URL
     */
    public ThinkBootCodeGenerator url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置数据库用户名
     */
    public ThinkBootCodeGenerator username(String username) {
        this.username = username;
        return this;
    }

    /**
     * 设置数据库密码
     */
    public ThinkBootCodeGenerator password(String password) {
        this.password = password;
        return this;
    }

    /**
     * 设置要生成的表名
     */
    public ThinkBootCodeGenerator tableName(String... tableNames) {
        this.tableNames = tableNames;
        return this;
    }

    /**
     * 设置模块名称（如 system、order）
     */
    public ThinkBootCodeGenerator moduleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    /**
     * 设置作者
     */
    public ThinkBootCodeGenerator author(String author) {
        this.author = author;
        return this;
    }

    /**
     * 设置代码输出路径
     */
    public ThinkBootCodeGenerator outputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    /**
     * 设置父包名
     */
    public ThinkBootCodeGenerator parentPackage(String parentPackage) {
        this.parentPackage = parentPackage;
        return this;
    }

    /**
     * 是否启用 Swagger 注解
     */
    public ThinkBootCodeGenerator enableSwagger(boolean enable) {
        this.enableSwagger = enable;
        return this;
    }

    /**
     * 是否启用 Lombok
     */
    public ThinkBootCodeGenerator enableLombok(boolean enable) {
        this.enableLombok = enable;
        return this;
    }

    /**
     * 是否启用 REST 风格
     */
    public ThinkBootCodeGenerator enableRestStyle(boolean enable) {
        this.enableRestStyle = enable;
        return this;
    }

    /**
     * 设置实体基类（如 BaseEntity）
     */
    public ThinkBootCodeGenerator entityBaseClass(String entityBaseClass) {
        this.entityBaseClass = entityBaseClass;
        return this;
    }

    /**
     * 是否使用实体基类（自动继承）
     */
    public ThinkBootCodeGenerator useBaseEntity(boolean use) {
        this.useBaseEntity = use;
        return this;
    }

    /**
     * 设置要忽略的表前缀
     */
    public ThinkBootCodeGenerator ignoreTablePrefix(String... prefixes) {
        this.ignoreTablePrefix = prefixes;
        return this;
    }

    /**
     * 设置主键类型
     */
    public ThinkBootCodeGenerator idType(IdType idType) {
        this.idType = idType;
        return this;
    }

    /**
     * 是否启用逻辑删除
     */
    public ThinkBootCodeGenerator useLogicDelete(boolean use) {
        this.useLogicDelete = use;
        return this;
    }

    /**
     * 设置逻辑删除字段名
     */
    public ThinkBootCodeGenerator logicDeleteField(String logicDeleteField) {
        this.logicDeleteField = logicDeleteField;
        return this;
    }

    /**
     * 禁用实体文件生成
     */
    public ThinkBootCodeGenerator disableEntity() {
        this.enableEntityFile = false;
        return this;
    }

    /**
     * 禁用 Mapper 文件生成
     */
    public ThinkBootCodeGenerator disableMapper() {
        this.enableMapperFile = false;
        return this;
    }

    /**
     * 禁用 Service 接口文件生成
     */
    public ThinkBootCodeGenerator disableService() {
        this.enableServiceFile = false;
        return this;
    }

    /**
     * 禁用 ServiceImpl 文件生成
     */
    public ThinkBootCodeGenerator disableServiceImpl() {
        this.enableServiceImplFile = false;
        return this;
    }

    /**
     * 禁用 Controller 文件生成
     */
    public ThinkBootCodeGenerator disableController() {
        this.enableControllerFile = false;
        return this;
    }

    /**
     * 执行代码生成
     */
    public void generate() {
        if (tableNames.length == 0) {
            throw new IllegalArgumentException("请设置至少一个表名");
        }

        String packagePath = parentPackage + (moduleName.isEmpty() ? "" : "." + moduleName);

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author(author)
                            .outputDir(outputPath)
                            .commentDate("yyyy-MM-dd")
                            .disableOpenDir();
                })
                .packageConfig(builder -> {
                    builder.parent(packagePath)
                            .entity("domain.entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, outputPath + "/" + packagePath.replace(".", "/") + "/mapper/xml"));
                })
                .strategyConfig(builder -> {
                    builder.addInclude(tableNames)
                            .addTablePrefix(ignoreTablePrefix)
                            .entityBuilder()
                            .enableLombok()
                            .logicDeleteColumnName(useLogicDelete ? logicDeleteField : null)
                            .idType(idType)
                            .enableTableFieldAnnotation()
                            .enableChainModel()
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel);

                    if (useBaseEntity) {
                        builder.entityBuilder().superClass(parentPackage + ".database.domain." + entityBaseClass)
                                .addSuperEntityColumns("id", "created_time", "updated_time", "created_by", "updated_by");
                    }

                    builder.controllerBuilder()
                            .enableRestStyle()
                            .enableHyphenStyle();

                    builder.serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl");

                    builder.mapperBuilder()
                            .enableBaseResultMap()
                            .enableBaseColumnList()
                            .formatMapperFileName("%sMapper")
                            .formatXmlFileName("%sMapper");
                })
                .templateEngine(new VelocityTemplateEngine())
                .execute();

        System.out.println("代码生成完成！");
        System.out.println("输出路径: " + outputPath);
        System.out.println("生成的表: " + Arrays.toString(tableNames));
    }
}
