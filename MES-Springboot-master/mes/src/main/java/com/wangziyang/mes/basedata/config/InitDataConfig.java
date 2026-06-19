package com.wangziyang.mes.basedata.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class InitDataConfig implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitDataConfig.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${basedata.init.enabled:false}")
    private boolean initEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (!initEnabled) {
            logger.info("========== 基础数据中心初始化已禁用(basedata.init.enabled=false)，跳过执行 ==========");
            return;
        }
        if (isInitialized()) {
            logger.info("========== 基础数据中心已初始化，跳过执行 ==========");
            return;
        }
        logger.info("========== 开始执行基础数据中心初始化SQL ==========");
        ClassPathResource resource = new ClassPathResource("sql/base_data_center.sql");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sqlBuilder.append(line);
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString();
                    try {
                        jdbcTemplate.execute(sql);
                        count++;
                        logger.info("执行SQL成功, 第{}条: {}", count, sql.substring(0, Math.min(50, sql.length())) + "...");
                    } catch (Exception e) {
                        logger.warn("SQL执行失败(可能已存在): {}", e.getMessage());
                    }
                    sqlBuilder = new StringBuilder();
                }
            }
            logger.info("========== 基础数据中心初始化SQL执行完成，共执行{}条 ==========", count);
        } catch (Exception e) {
            logger.error("基础数据中心初始化失败: {}", e.getMessage(), e);
        }
    }

    private boolean isInitialized() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sp_sys_menu WHERE id = '20'", Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}