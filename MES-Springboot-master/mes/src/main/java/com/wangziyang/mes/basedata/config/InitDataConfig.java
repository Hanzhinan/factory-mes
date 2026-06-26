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
import java.util.List;
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

        // 第三步：确保管理员账号存在（每次启动都检查，防止误删）
        ensureAdminUserExists();

        // 第四步：为管理员角色授权（每次启动都检查，确保权限完整）
        grantPermissionsToAdmin();

        // 第五步：更新菜单URL（每次启动都检查，确保URL正确）
        updateMenuUrl();

        // 第六步：初始化测试库位数据
        initWarehouseLocationTestData();
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

        // 检查 sp_work_unit 表的 team_id 字段
        if (isTableExists("sp_work_unit") && !isColumnExists("sp_work_unit", "team_id")) {
            try {
                String alterSql = "ALTER TABLE `sp_work_unit` ADD COLUMN `team_id` VARCHAR(64) COMMENT '关联班组ID' AFTER `status`";
                jdbcTemplate.execute(alterSql);
                fixCount++;
                logger.info("表结构修复成功: sp_work_unit 表已添加 team_id 字段");
            } catch (Exception e) {
                logger.error("表结构修复失败: sp_work_unit.team_id, 错误: {}", e.getMessage());
            }
        }

        // 检查 sp_process_info 表的 team_id 字段
        if (isTableExists("sp_process_info") && !isColumnExists("sp_process_info", "team_id")) {
            try {
                String alterSql = "ALTER TABLE `sp_process_info` ADD COLUMN `team_id` VARCHAR(64) COMMENT '关联班组ID' AFTER `status`";
                jdbcTemplate.execute(alterSql);
                fixCount++;
                logger.info("表结构修复成功: sp_process_info 表已添加 team_id 字段");
            } catch (Exception e) {
                logger.error("表结构修复失败: sp_process_info.team_id, 错误: {}", e.getMessage());
            }
        }

        // 检查 sp_group_device 表的 team_id 字段
        if (isTableExists("sp_group_device") && !isColumnExists("sp_group_device", "team_id")) {
            try {
                String alterSql = "ALTER TABLE `sp_group_device` ADD COLUMN `team_id` VARCHAR(64) COMMENT '关联班组ID' AFTER `capacity`";
                jdbcTemplate.execute(alterSql);
                fixCount++;
                logger.info("表结构修复成功: sp_group_device 表已添加 team_id 字段");
            } catch (Exception e) {
                logger.error("表结构修复失败: sp_group_device.team_id, 错误: {}", e.getMessage());
            }
        }

        // 检查 sp_process_info 表的 device_id 字段
        if (isTableExists("sp_process_info") && !isColumnExists("sp_process_info", "device_id")) {
            try {
                String alterSql = "ALTER TABLE `sp_process_info` ADD COLUMN `device_id` VARCHAR(64) COMMENT '关联编组设备ID' AFTER `device_code`";
                jdbcTemplate.execute(alterSql);
                fixCount++;
                logger.info("表结构修复成功: sp_process_info 表已添加 device_id 字段");
            } catch (Exception e) {
                logger.error("表结构修复失败: sp_process_info.device_id, 错误: {}", e.getMessage());
            }
        }

        // 检查 sp_materile 表是否存在，不存在则创建
        if (!isTableExists("sp_materile")) {
            try {
                String createSql = "CREATE TABLE IF NOT EXISTS `sp_materile` (" +
                    "`id` VARCHAR(64) NOT NULL COMMENT '主键ID'," +
                    "`materiel` VARCHAR(255) COMMENT '物料编码'," +
                    "`materiel_desc` VARCHAR(255) COMMENT '物料描述'," +
                    "`unit` VARCHAR(255) COMMENT '基本单位'," +
                    "`product_group` VARCHAR(255) COMMENT '产品组'," +
                    "`mat_type` VARCHAR(255) NOT NULL COMMENT '物料类型'," +
                    "`model` VARCHAR(255) COMMENT '型号'," +
                    "`size` VARCHAR(255) COMMENT '尺寸'," +
                    "`flow_id` VARCHAR(255) COMMENT '流程ID'," +
                    "`flow_desc` VARCHAR(255) COMMENT '流程描述'," +
                    "`material` VARCHAR(255) COMMENT '材质'," +
                    "`lead_time` INT(4) COMMENT '物料需求提前期(天)'," +
                    "`safety_stock` DECIMAL(10,2) COMMENT '安全库存'," +
                    "`material_source` VARCHAR(255) COMMENT '物料来源'," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "`create_username` VARCHAR(64) COMMENT '创建人'," +
                    "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                    "`update_username` VARCHAR(64) COMMENT '更新人'," +
                    "`is_deleted` CHAR(2) DEFAULT '0' COMMENT '逻辑删除：1 表示删除，0 表示未删除，2 表示禁用'," +
                    "PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基础物料表'";
                jdbcTemplate.execute(createSql);
                fixCount++;
                logger.info("表结构修复成功: sp_materile 表已创建");
            } catch (Exception e) {
                logger.error("表结构修复失败: sp_materile, 错误: {}", e.getMessage());
            }
        } else {
            // 如果表已存在，检查并添加缺失的字段
            if (!isColumnExists("sp_materile", "material")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_materile` ADD COLUMN `material` VARCHAR(255) COMMENT '材质' AFTER `flow_desc`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_materile 表已添加 material 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_materile.material, 错误: {}", e.getMessage());
                }
            }
            if (!isColumnExists("sp_materile", "lead_time")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_materile` ADD COLUMN `lead_time` INT(4) COMMENT '物料需求提前期(天)' AFTER `material`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_materile 表已添加 lead_time 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_materile.lead_time, 错误: {}", e.getMessage());
                }
            }
            if (!isColumnExists("sp_materile", "safety_stock")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_materile` ADD COLUMN `safety_stock` DECIMAL(10,2) COMMENT '安全库存' AFTER `lead_time`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_materile 表已添加 safety_stock 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_materile.safety_stock, 错误: {}", e.getMessage());
                }
            }
            if (!isColumnExists("sp_materile", "material_source")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_materile` ADD COLUMN `material_source` VARCHAR(255) COMMENT '物料来源' AFTER `safety_stock`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_materile 表已添加 material_source 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_materile.material_source, 错误: {}", e.getMessage());
                }
            }
        }

        // 检查 sp_warehouse_location 表的3D模型绑定字段
        if (isTableExists("sp_warehouse_location")) {
            if (!isColumnExists("sp_warehouse_location", "shelf_id")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_warehouse_location` ADD COLUMN `shelf_id` VARCHAR(64) COMMENT '货架ID' AFTER `status`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_warehouse_location 表已添加 shelf_id 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_warehouse_location.shelf_id, 错误: {}", e.getMessage());
                }
            }
            if (!isColumnExists("sp_warehouse_location", "shelf_row")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_warehouse_location` ADD COLUMN `shelf_row` INT(4) COMMENT '货架行号' AFTER `shelf_id`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_warehouse_location 表已添加 shelf_row 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_warehouse_location.shelf_row, 错误: {}", e.getMessage());
                }
            }
            if (!isColumnExists("sp_warehouse_location", "shelf_column")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_warehouse_location` ADD COLUMN `shelf_column` INT(4) COMMENT '货架列号' AFTER `shelf_row`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_warehouse_location 表已添加 shelf_column 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_warehouse_location.shelf_column, 错误: {}", e.getMessage());
                }
            }
            if (!isColumnExists("sp_warehouse_location", "shelf_layer")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_warehouse_location` ADD COLUMN `shelf_layer` INT(4) COMMENT '货架层号' AFTER `shelf_column`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_warehouse_location 表已添加 shelf_layer 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_warehouse_location.shelf_layer, 错误: {}", e.getMessage());
                }
            }
            if (!isColumnExists("sp_warehouse_location", "color")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_warehouse_location` ADD COLUMN `color` VARCHAR(32) COMMENT '3D显示颜色' AFTER `shelf_layer`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_warehouse_location 表已添加 color 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_warehouse_location.color, 错误: {}", e.getMessage());
                }
            }
            if (!isColumnExists("sp_warehouse_location", "area_code")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_warehouse_location` ADD COLUMN `area_code` VARCHAR(64) COMMENT '库区编码' AFTER `color`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_warehouse_location 表已添加 area_code 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_warehouse_location.area_code, 错误: {}", e.getMessage());
                }
            }
            if (!isColumnExists("sp_warehouse_location", "model_type")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE `sp_warehouse_location` ADD COLUMN `model_type` VARCHAR(64) COMMENT '模型类型(SHELF/RACK/LOCATION)' AFTER `area_code`");
                    fixCount++;
                    logger.info("表结构修复成功: sp_warehouse_location 表已添加 model_type 字段");
                } catch (Exception e) {
                    logger.error("表结构修复失败: sp_warehouse_location.model_type, 错误: {}", e.getMessage());
                }
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

        try {
            String updateSql = "UPDATE sp_sys_menu SET url = '/basedata/materiel/list-ui', permission = 'basedata:materiel' WHERE url = '/basedata/materile/list-ui'";
            int affected = jdbcTemplate.update(updateSql);
            if (affected > 0) {
                logger.info("菜单URL更新成功: 物料维护 -> /basedata/materiel/list-ui");
            }
        } catch (Exception e) {
            logger.warn("物料维护菜单URL更新失败: {}", e.getMessage());
        }
    }

    /**
     * 为管理员角色授权基础数据中心菜单
     */
    private void grantPermissionsToAdmin() {
        logger.info("========== 开始为管理员角色授权 ==========");
        String adminRoleId = "1185025876737396738";
        int grantedCount = 0;
        try {
            List<String> allMenuIds = jdbcTemplate.queryForList(
                "SELECT id FROM sp_sys_menu",
                String.class
            );
            for (String menuId : allMenuIds) {
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
            }
        } catch (Exception e) {
            logger.error("为管理员角色授权失败: {}", e.getMessage());
        }
        logger.info("========== 管理员角色授权完成，共授权{}个菜单 ==========", grantedCount);
    }

    /**
     * 确保管理员账号存在（防止误删导致无法登录）
     */
    private void ensureAdminUserExists() {
        logger.info("========== 开始检查管理员账号 ==========");
        try {
            String roleId = "1185025876737396738";
            String userId = "1184019107907227649";
            String password = "9d7281eeaebded0b091340cfa658a7e8";

            Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sp_sys_role WHERE id = ?", Integer.class, roleId);
            if (roleCount == null || roleCount == 0) {
                jdbcTemplate.execute(String.format(
                    "INSERT INTO sp_sys_role (id, name, code, descr, is_deleted, create_time, create_username, update_time, update_username) VALUES ('%s', '超级管理员', 'admin', '超级管理员', '0', NOW(), 'system', NOW(), 'system')",
                    roleId
                ));
                logger.info("超级管理员角色创建成功");
            }

            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sp_sys_user WHERE username = 'admin'", Integer.class);
            if (userCount == null || userCount == 0) {
                jdbcTemplate.execute(String.format(
                    "INSERT INTO sp_sys_user (id, name, username, password, mobile, sex, is_deleted, create_time, create_username, update_time, update_username) VALUES ('%s', '超级管理员', 'admin', '%s', '13776337796', '0', '0', NOW(), 'system', NOW(), 'system')",
                    userId, password
                ));
                logger.info("管理员账号创建成功: username=admin, password=admin");
            } else {
                logger.info("管理员账号已存在，跳过创建");
            }

            Integer roleMenuCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sp_sys_role_menu WHERE role_id = ?", Integer.class, roleId);
            if (roleMenuCount == null || roleMenuCount == 0) {
                String roleMenuId = IdUtil.nextId();
                jdbcTemplate.execute(String.format(
                    "INSERT INTO sp_sys_role_menu (id, role_id, menu_id, create_time, create_username, update_time, update_username) VALUES ('%s', '%s', '1', NOW(), 'system', NOW(), 'system')",
                    roleMenuId, roleId
                ));
                logger.info("管理员角色菜单授权创建成功");
            }

            // 确保固定ID的管理员账号关联角色
            Integer userRoleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sp_sys_user_role WHERE user_id = ?", Integer.class, userId);
            if (userRoleCount == null || userRoleCount == 0) {
                String userRoleId = IdUtil.nextId();
                jdbcTemplate.execute(String.format(
                    "INSERT INTO sp_sys_user_role (id, user_id, role_id, create_time, create_username, update_time, update_username) VALUES ('%s', '%s', '%s', NOW(), 'system', NOW(), 'system')",
                    userRoleId, userId, roleId
                ));
                logger.info("管理员用户角色关联创建成功");
            }

            // 同时确保数据库中 username='admin' 的账号也关联到该角色（兼容旧数据）
            try {
                String actualAdminUserId = jdbcTemplate.queryForObject(
                    "SELECT id FROM sp_sys_user WHERE username = 'admin' LIMIT 1", String.class);
                if (actualAdminUserId != null) {
                    Integer actualUserRoleCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM sp_sys_user_role WHERE user_id = ? AND role_id = ?",
                        Integer.class, actualAdminUserId, roleId);
                    if (actualUserRoleCount == null || actualUserRoleCount == 0) {
                        String userRoleId = IdUtil.nextId();
                        jdbcTemplate.execute(String.format(
                            "INSERT INTO sp_sys_user_role (id, user_id, role_id, create_time, create_username, update_time, update_username) VALUES ('%s', '%s', '%s', NOW(), 'system', NOW(), 'system')",
                            userRoleId, actualAdminUserId, roleId
                        ));
                        logger.info("旧管理员用户角色关联修复成功");
                    }
                }
            } catch (Exception ex) {
                logger.warn("修复旧管理员用户角色关联时出错: {}", ex.getMessage());
            }
        } catch (Exception e) {
            logger.error("检查/创建管理员账号失败: {}", e.getMessage());
        }
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

    /**
     * 初始化测试库位数据（仅在表为空时插入）
     */
    private void initWarehouseLocationTestData() {
        logger.info("========== 开始初始化测试库位数据 ==========");
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sp_warehouse_location", Integer.class);
            if (count != null && count >= 5) {
                logger.info("库位数据已足够，跳过初始化。当前共 {} 条数据", count);
                return;
            }

            jdbcTemplate.execute("DELETE FROM sp_warehouse_location");
            logger.info("已清空现有库位数据");

            String[][] testData = {
                {"WAREHOUSE-01", "原材料仓库", "原材料库", "LOC-A-01-01-01", "A区-01库位-01层", "A-AREA", "SHELF-01", "1", "1", "1", "100", "50", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-01", "原材料仓库", "原材料库", "LOC-A-01-01-02", "A区-01库位-02层", "A-AREA", "SHELF-01", "1", "1", "2", "100", "30", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-01", "原材料仓库", "原材料库", "LOC-A-01-01-03", "A区-01库位-03层", "A-AREA", "SHELF-01", "1", "1", "3", "100", "0", "1", "#FF6347", "SHELF"},
                {"WAREHOUSE-01", "原材料仓库", "原材料库", "LOC-A-01-02-01", "A区-02库位-01层", "A-AREA", "SHELF-01", "1", "2", "1", "100", "80", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-01", "原材料仓库", "原材料库", "LOC-A-01-02-02", "A区-02库位-02层", "A-AREA", "SHELF-01", "1", "2", "2", "100", "60", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-01", "原材料仓库", "原材料库", "LOC-A-02-01-01", "A区-03库位-01层", "A-AREA", "SHELF-02", "2", "1", "1", "100", "40", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-01", "原材料仓库", "原材料库", "LOC-A-02-01-02", "A区-03库位-02层", "A-AREA", "SHELF-02", "2", "1", "2", "100", "20", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-01", "原材料仓库", "原材料库", "LOC-A-02-01-03", "A区-03库位-03层", "A-AREA", "SHELF-02", "2", "1", "3", "100", "100", "1", "#FF6347", "SHELF"},
                {"WAREHOUSE-01", "原材料仓库", "原材料库", "LOC-A-02-02-01", "A区-04库位-01层", "A-AREA", "SHELF-02", "2", "2", "1", "100", "0", "2", "#D3D3D3", "SHELF"},

                {"WAREHOUSE-02", "成品仓库", "成品库", "LOC-B-01-01-01", "B区-01库位-01层", "B-AREA", "SHELF-03", "1", "1", "1", "200", "150", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-02", "成品仓库", "成品库", "LOC-B-01-01-02", "B区-01库位-02层", "B-AREA", "SHELF-03", "1", "1", "2", "200", "80", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-02", "成品仓库", "成品库", "LOC-B-01-01-03", "B区-01库位-03层", "B-AREA", "SHELF-03", "1", "1", "3", "200", "200", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-02", "成品仓库", "成品库", "LOC-B-01-01-04", "B区-01库位-04层", "B-AREA", "SHELF-03", "1", "1", "4", "200", "120", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-02", "成品仓库", "成品库", "LOC-B-02-01-01", "B区-02库位-01层", "B-AREA", "SHELF-04", "2", "1", "1", "200", "90", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-02", "成品仓库", "成品库", "LOC-B-02-01-02", "B区-02库位-02层", "B-AREA", "SHELF-04", "2", "1", "2", "200", "180", "1", "#FF6347", "SHELF"},
                {"WAREHOUSE-02", "成品仓库", "成品库", "LOC-B-02-02-01", "B区-03库位-01层", "B-AREA", "SHELF-04", "2", "2", "1", "200", "60", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-02", "成品仓库", "成品库", "LOC-B-02-02-02", "B区-03库位-02层", "B-AREA", "SHELF-04", "2", "2", "2", "200", "140", "0", "#90EE90", "SHELF"},

                {"WAREHOUSE-03", "半成品仓库", "半成品库", "LOC-C-01-01-01", "C区-01库位-01层", "C-AREA", "SHELF-05", "1", "1", "1", "150", "100", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-03", "半成品仓库", "半成品库", "LOC-C-01-01-02", "C区-01库位-02层", "C-AREA", "SHELF-05", "1", "1", "2", "150", "50", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-03", "半成品仓库", "半成品库", "LOC-C-01-01-03", "C区-01库位-03层", "C-AREA", "SHELF-05", "1", "1", "3", "150", "0", "1", "#FF6347", "SHELF"},
                {"WAREHOUSE-03", "半成品仓库", "半成品库", "LOC-C-02-01-01", "C区-02库位-01层", "C-AREA", "SHELF-06", "2", "1", "1", "150", "80", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-03", "半成品仓库", "半成品库", "LOC-C-02-01-02", "C区-02库位-02层", "C-AREA", "SHELF-06", "2", "1", "2", "150", "30", "0", "#90EE90", "SHELF"},
                {"WAREHOUSE-03", "半成品仓库", "半成品库", "LOC-C-02-01-03", "C区-02库位-03层", "C-AREA", "SHELF-06", "2", "1", "3", "150", "0", "2", "#D3D3D3", "SHELF"}
            };

            for (String[] row : testData) {
                String id = IdUtil.nextId();
                String insertSql = String.format(
                    "INSERT INTO sp_warehouse_location (id, warehouse_code, warehouse_name, warehouse_type, " +
                    "location_code, location_name, area_code, shelf_id, shelf_row, shelf_column, shelf_layer, " +
                    "capacity, current_qty, status, color, model_type, " +
                    "create_time, create_username, update_time, update_username) " +
                    "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %s, %s, %s, %s, %s, %s, '%s', '%s', " +
                    "NOW(), 'system', NOW(), 'system')",
                    id, row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9], row[10], row[11], row[12], row[13], row[14]
                );
                jdbcTemplate.execute(insertSql);
            }

            logger.info("测试库位数据初始化成功，共插入 {} 条数据", testData.length);
        } catch (Exception e) {
            logger.error("初始化测试库位数据失败: {}", e.getMessage());
        }
    }
}