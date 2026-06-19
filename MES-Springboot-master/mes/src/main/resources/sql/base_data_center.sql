CREATE TABLE IF NOT EXISTS `sp_team_staff` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `staff_code` VARCHAR(64) NOT NULL COMMENT '员工编号',
    `staff_name` VARCHAR(128) NOT NULL COMMENT '员工姓名',
    `team_code` VARCHAR(64) COMMENT '班组编号',
    `team_name` VARCHAR(128) COMMENT '班组名称',
    `department_id` VARCHAR(64) COMMENT '部门ID',
    `department_name` VARCHAR(128) COMMENT '部门名称',
    `position` VARCHAR(64) COMMENT '职位',
    `phone` VARCHAR(32) COMMENT '联系电话',
    `email` VARCHAR(128) COMMENT '邮箱',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-在职，1-离职，2-休假',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_username` VARCHAR(64) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_username` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_staff_code` (`staff_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组员工定义';

CREATE TABLE IF NOT EXISTS `sp_group_device` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `device_code` VARCHAR(64) NOT NULL COMMENT '设备编号',
    `device_name` VARCHAR(128) NOT NULL COMMENT '设备名称',
    `device_model` VARCHAR(128) COMMENT '设备型号',
    `device_type` VARCHAR(64) COMMENT '设备类型',
    `group_code` VARCHAR(64) COMMENT '编组编号',
    `group_name` VARCHAR(128) COMMENT '编组名称',
    `work_unit_code` VARCHAR(64) COMMENT '所属加工单元编号',
    `work_unit_name` VARCHAR(128) COMMENT '所属加工单元名称',
    `location` VARCHAR(256) COMMENT '设备位置',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-运行，1-停机，2-维护',
    `capacity` DECIMAL(10,2) COMMENT '产能',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_username` VARCHAR(64) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_username` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_code` (`device_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编组设备定义';

CREATE TABLE IF NOT EXISTS `sp_work_unit` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `unit_code` VARCHAR(64) NOT NULL COMMENT '加工单元编号',
    `unit_name` VARCHAR(128) NOT NULL COMMENT '加工单元名称',
    `unit_type` VARCHAR(64) COMMENT '单元类型',
    `location` VARCHAR(256) COMMENT '位置',
    `capacity` DECIMAL(10,2) COMMENT '产能',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-启用，1-停用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_username` VARCHAR(64) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_username` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_unit_code` (`unit_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加工单元定义';

CREATE TABLE IF NOT EXISTS `sp_warehouse_location` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `warehouse_code` VARCHAR(64) NOT NULL COMMENT '库房编号',
    `warehouse_name` VARCHAR(128) NOT NULL COMMENT '库房名称',
    `warehouse_type` VARCHAR(64) COMMENT '库房类型',
    `location_code` VARCHAR(64) NOT NULL COMMENT '库位编号',
    `location_name` VARCHAR(128) COMMENT '库位名称',
    `x_coordinate` DECIMAL(10,2) COMMENT 'X坐标',
    `y_coordinate` DECIMAL(10,2) COMMENT 'Y坐标',
    `z_coordinate` DECIMAL(10,2) COMMENT 'Z坐标',
    `capacity` DECIMAL(10,2) COMMENT '容量',
    `current_qty` DECIMAL(10,2) DEFAULT 0 COMMENT '当前数量',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-可用，1-占用，2-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_username` VARCHAR(64) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_username` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_location_code` (`location_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库房库位定义';

CREATE TABLE IF NOT EXISTS `sp_part_component` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `part_code` VARCHAR(64) NOT NULL COMMENT '零部件编号',
    `part_name` VARCHAR(128) NOT NULL COMMENT '零部件名称',
    `part_type` VARCHAR(64) COMMENT '零部件类型',
    `material_code` VARCHAR(64) COMMENT '物料编号',
    `material_name` VARCHAR(128) COMMENT '物料名称',
    `specification` VARCHAR(256) COMMENT '规格',
    `unit` VARCHAR(32) COMMENT '单位',
    `version` VARCHAR(32) DEFAULT 'V1.0' COMMENT '版本号',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-有效，1-失效',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_username` VARCHAR(64) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_username` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_part_code` (`part_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='零部件定义';

CREATE TABLE IF NOT EXISTS `sp_process_info` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `process_code` VARCHAR(64) NOT NULL COMMENT '工序编号',
    `process_name` VARCHAR(128) NOT NULL COMMENT '工序名称',
    `process_desc` VARCHAR(512) COMMENT '工序描述',
    `work_unit_code` VARCHAR(64) COMMENT '所属加工单元编号',
    `work_unit_name` VARCHAR(128) COMMENT '所属加工单元名称',
    `device_code` VARCHAR(64) COMMENT '设备编号',
    `device_name` VARCHAR(128) COMMENT '设备名称',
    `standard_time` DECIMAL(10,2) COMMENT '标准工时',
    `sequence` INT(4) DEFAULT 0 COMMENT '工序顺序',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态：0-启用，1-停用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_username` VARCHAR(64) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_username` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_process_code` (`process_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序信息定义';

DELETE FROM `sp_sys_menu` WHERE `id` IN ('20','201','202','203','204','205','206','207');
INSERT INTO `sp_sys_menu` VALUES ('20', 'basedataCenter', '基础数据中心', '#', '1', '2', 8, '0', '', 'fa fa-database', '基础数据中心模块', NOW(), 'admin', NOW(), 'admin');
INSERT INTO `sp_sys_menu` VALUES ('201', 'teamStaff', '班组员工定义', '/basedata/teamStaff/list-ui', '20', '3', 1, '0', '', 'fa fa-users', '班组员工定义', NOW(), 'admin', NOW(), 'admin');
INSERT INTO `sp_sys_menu` VALUES ('202', 'groupDevice', '编组设备定义', '/basedata/groupDevice/list-ui', '20', '3', 2, '0', '', 'fa fa-cog', '编组设备定义', NOW(), 'admin', NOW(), 'admin');
INSERT INTO `sp_sys_menu` VALUES ('203', 'workUnit', '加工单元定义', '/basedata/workUnit/list-ui', '20', '3', 3, '0', '', 'fa fa-industry', '加工单元定义', NOW(), 'admin', NOW(), 'admin');
INSERT INTO `sp_sys_menu` VALUES ('204', 'materiel', '物料信息定义', '/basedata/materiel/list-ui', '20', '3', 4, '0', '', 'fa fa-cubes', '物料信息定义', NOW(), 'admin', NOW(), 'admin');
INSERT INTO `sp_sys_menu` VALUES ('205', 'warehouseLocation', '库房库位定义', '/basedata/warehouseLocation/list-ui', '20', '3', 5, '0', '', 'fa fa-archive', '库房库位定义', NOW(), 'admin', NOW(), 'admin');
INSERT INTO `sp_sys_menu` VALUES ('206', 'partComponent', '零部件定义', '/basedata/partComponent/list-ui', '20', '3', 6, '0', '', 'fa fa-wrench', '零部件定义', NOW(), 'admin', NOW(), 'admin');
INSERT INTO `sp_sys_menu` VALUES ('207', 'processInfo', '工序信息定义', '/basedata/processInfo/list-ui', '20', '3', 7, '0', '', 'fa fa-cogs', '工序信息定义', NOW(), 'admin', NOW(), 'admin');