package com.thinkboot.codegen.example;

import com.baomidou.mybatisplus.annotation.IdType;
import com.thinkboot.codegen.ThinkBootCodeGenerator;

/**
 * ThinkBoot 代码生成器使用示例
 * 运行此方法即可根据数据库表生成 CRUD 代码
 * 
 * 使用步骤：
 * 1. 修改数据库连接信息（url、username、password）
 * 2. 设置要生成的表名
 * 3. 设置模块名称（如 system、order）
 * 4. 设置代码输出路径
 * 5. 运行 main 方法
 * 
 * 生成的代码包括：
 * - Entity（实体类）
 * - Mapper（数据访问层）
 * - Service（业务逻辑接口）
 * - ServiceImpl（业务逻辑实现）
 * - Controller（控制器）
 * - Mapper.xml（MyBatis XML 映射文件）
 */
public class CodeGeneratorExample {

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
                .author("ThinkBoot")
                
                // 代码输出路径
                .outputPath("D:/project/think-boot-example/src/main/java")
                
                // 父包名
                .parentPackage("com.thinkboot")
                
                // 忽略表前缀（sys_ 会被忽略）
                .ignoreTablePrefix("sys_")
                
                // 是否继承 BaseEntity（自动填充 created_time、updated_time）
                .useBaseEntity(true)
                
                // 是否启用逻辑删除
                .useLogicDelete(false)
                
                // 主键类型
                .idType(IdType.ASSIGN_ID)
                
                // 生成 Swagger 注解
                .enableSwagger(true)
                
                // 生成 Lombok 注解
                .enableLombok(true)
                
                // 生成 REST 风格 Controller
                .enableRestStyle(true)
                
                // 执行生成
                .generate();
    }
}
