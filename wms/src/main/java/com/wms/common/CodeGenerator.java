package com.wms.common;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import java.util.Scanner;
public class CodeGenerator {
    /**
     * <p>
     * 
读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输⼊" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输⼊正确的" + tip + "！");
    }
    /**
     * 
操作步骤：
     *  1.修改数据源包括地址密码信息，对应代码标记：⼀、 下同
     *  2.模块配置，可以修改包名
     *  3.修改模板（这步可忽略）
     * @param args
     */
    public static void main(String[] args) {
        //代码⽣成器
        String projectPath = System.getProperty("user.dir")+"/wms";
        
        // 创建代码⽣成器（直接传数据库连接参数）
        FastAutoGenerator.create("jdbc:mysql://localhost:3308/wms02?useUnicode=true&characterEncoding=UTF8&useSSL=false", "root", "123456")
            .globalConfig(builder -> builder
                .outputDir(projectPath + "/src/main/java")
                .author("wms")
                .disableOpenDir()
                .enableSpringdoc())
            
            .packageConfig(builder -> builder
                .parent("com.wms")
                .entity("entity")
                .mapper("mapper")
                .service("service")
                .serviceImpl("service.impl")
                .controller("controller"))
            
            .strategyConfig(builder -> builder
                .addInclude(scanner("表名，多个英⽤逗号分割").split(","))
                .enableCapitalMode()
                .enableSkipView())
            
            .templateConfig(builder -> builder
                .disable(TemplateType.ENTITY)
                .disable(TemplateType.SERVICE)
                .disable(TemplateType.SERVICE_IMPL)
                .disable(TemplateType.MAPPER)
                .disable(TemplateType.CONTROLLER))
            
            .templateEngine(new FreemarkerTemplateEngine())
            .execute();
    }
}