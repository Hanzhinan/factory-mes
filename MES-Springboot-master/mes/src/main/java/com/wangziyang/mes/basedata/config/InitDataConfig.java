package com.wangziyang.mes.basedata.config;

import com.wangziyang.mes.common.util.IdUtil;
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
import java.util.HashMap;
import java.util.Map;

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

        // 第一步：检查并修复表结构（每次启动都执行，确保字段完整）
        checkAndFixSchema();

        // 第二步：初始化菜单数据（仅首次执行）
        if (!isMenuInitialized()) {
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
                        // 跳过ALTER TABLE语句（已在checkAndFixSchema中处理）
                        if (sql.toUpperCase().contains("ALTER TABLE")) {
                            sqlBuilder = new StringBuilder();
                            continue;
                        }
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
        } else {
            logger.info("========== 基础数据中心菜单已初始化，跳过SQL执行 ==========");
        }

        // 第三步：为管理员角色授权（每次启动都检查，确保权限完整）
        grantPermissionsToAdmin();

        // 第四步：更新菜单URL（每次启动都检查，确保URL正确）
        updateMenuUrl();
    }

    /**
     * 检查并修复表结构
     * 每次启动都执行，自动检测缺失的字段并添加
     */
    private void checkAndFixSchema() {
        logger.info("========== 开始检查表结构 ==========");
        int fixCount = 0;

        // 检查 sp_team 表是否存在，不存在则创建
        if (!isTableExists("sp_team")) {
            try {
                String createSql = "CREATE TABLE IF NOT EXISTS `sp_team` (" +
                    "`id` VARCHAR(64) NOT NULL COMMENT '主键ID'," +
                    "`team_code` VARCHAR(64) NOT NULL COMMENT '班组代码'," +
                    "`team_name` VARCHAR(128) NOT NULL COMMENT '班组名称'," +
                    "`team_descr` VARCHAR(512) COMMENT '班组描述'," +
                    "`remark` VARCHAR(512) COMMENT '备注信息'," +
                    "`status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常'," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "`create_username` VARCHAR(64) COMMENT '创建人'," +
                    "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                    "`update_username` VARCHAR(64) COMMENT '更新人'," +
                    "PRIMARY KEY (`id`)," +
                    "UNIQUE KEY `uk_team_code` (`team_code`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组管理'";
                jdbcTemplate.execute(createSql);
                fixCount++;
                logger.info("表结构修复成功: sp_team 表已创建");
            } catch (Exception e) {
                logger.error("表结构修复失败: sp_team, 错误: {}", e.getMessage());
            }
        }

        // 检查 sp_team_staff 是否为旧结构（有 staff_code 字段则为旧表）
        if (isTableExists("sp_team_staff") && isColumnExists("sp_team_staff", "staff_code")) {
            logger.info("检测到旧版 sp_team_staff 表结构，开始重建...");
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS `sp_team_staff`");
                String createSql = "CREATE TABLE IF NOT EXISTS `sp_team_staff` (" +
                    "`id` VARCHAR(64) NOT NULL COMMENT '主键ID'," +
                    "`team_id` VARCHAR(64) NOT NULL COMMENT '班组ID'," +
                    "`user_id` VARCHAR(64) NOT NULL COMMENT '系统用户ID'," +
                    "`status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常'," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "`create_username` VARCHAR(64) COMMENT '创建人'," +
                    "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                    "`update_username` VARCHAR(64) COMMENT '更新人'," +
                    "PRIMARY KEY (`id`)," +
                    "UNIQUE KEY `uk_team_user` (`team_id`, `user_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组员工绑定关系'";
                jdbcTemplate.execute(createSql);
                fixCount++;
                logger.info("表结构修复成功: sp_team_staff 表已重建为新结构");
            } catch (Exception e) {
                logger.error("表结构修复失败: sp_team_staff, 错误: {}", e.getMessage());
            }
        } else if (!isTableExists("sp_team_staff")) {
            try {
                String createSql = "CREATE TABLE IF NOT EXISTS `sp_team_staff` (" +
                    "`id` VARCHAR(64) NOT NULL COMMENT '主键ID'," +
                    "`team_id` VARCHAR(64) NOT NULL COMMENT '班组ID'," +
                    "`user_id` VARCHAR(64) NOT NULL COMMENT '系统用户ID'," +
                    "`status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常'," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "`create_username` VARCHAR(64) COMMENT '创建人'," +
                    "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                    "`update_username` VARCHAR(64) COMMENT '更新人'," +
                    "PRIMARY KEY (`id`)," +
                    "UNIQUE KEY `uk_team_user` (`team_id`, `user_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组员工绑定关系'";
                jdbcTemplate.execute(createSql);
                fixCount++;
                logger.info("表结构修复成功: sp_team_staff 表已创建");
            } catch (Exception e) {
                logger.error("表结构修复失败: sp_team_staff, 错误: {}", e.getMessage());
            }
        }

        logger.info("========== 表结构检查完成，修复{}个表 ==========", fixCount);
    }

    /**
     * 检查指定表是否存在
     */
    private boolean isTableExists(String tableName) {
        try {
            String sql = String.format(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '%s'",
                tableName
            );
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.warn("检查表是否存在失败: {}", tableName);
            return false;
        }
    }

    /**
     * 检查指定表的字段是否存在
     */
    private boolean isColumnExists(String tableName, String columnName) {
        try {
            String sql = String.format(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '%s' AND COLUMN_NAME = '%s'",
                tableName, columnName
            );
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.warn("检查字段是否存在失败: {}.{}", tableName, columnName);
            return false;
        }
    }

    /**
     * 更新班组员工定义菜单URL
     */
    private void updateMenuUrl() {
        logger.info("========== 开始更新菜单URL ==========");
        try {
            String updateSql = "UPDATE sp_sys_menu SET url = '/basedata/team/list-ui', permission = 'basedata:team' WHERE id = '201'";
            int affected = jdbcTemplate.update(updateSql);
            if (affected > 0) {
                logger.info("菜单URL更新成功: 班组员工定义 -> /basedata/team/list-ui");
            }
        } catch (Exception e) {
            logger.warn("菜单URL更新失败: {}", e.getMessage());
        }
    }

    /**
     * 为管理员角色授权基础数据中心菜单
     */
    private void grantPermissionsToAdmin() {
        logger.info("========== 开始为管理员角色授权 ==========");
        String adminRoleId = "1";
        String[] menuIds = {"20", "201", "2011", "2012", "2013", "2014", "202", "203", "204", "205", "206", "207"};
        int grantedCount = 0;
        for (String menuId : menuIds) {
            try {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sp_sys_role_menu WHERE role_id = ? AND menu_id = ?",
                    Integer.class, adminRoleId, menuId
                );
                if (count == null || count == 0) {
                    String id = IdUtil.nextId();
                    String insertSql = String.format(
                        "INSERT INTO sp_sys_role_menu (id, role_id, menu_id, create_time, update_time, create_username, update_username) VALUES ('%s', '%s', '%s', NOW(), NOW(), 'system', 'system')",
                        id, adminRoleId, menuId
                    );
                    jdbcTemplate.execute(insertSql);
                    grantedCount++;
                    logger.info("为管理员角色授权菜单: {}", menuId);
                }
            } catch (Exception e) {
                logger.warn("授权失败(可能已存在): {}", e.getMessage());
            }
        }
        logger.info("========== 管理员角色授权完成，共授权{}个菜单 ==========", grantedCount);
    }

    /**
     * 检查菜单是否已初始化
     */
    private boolean isMenuInitialized() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sp_sys_menu WHERE id = '20'", Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}