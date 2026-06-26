# MES-Springboot - 制造执行系统

> 基于 Spring Boot 的开源 MES（制造执行系统），涵盖系统管理、工艺管理、计划管理、物料管理、基础数据中心等核心模块。

## 项目简介

本项目是一个面向制造业的 MES 系统，采用前后端一体化架构，后端基于 Spring Boot + MyBatis Plus + Shiro，前端使用 Layui + FreeMarker 模板引擎。系统支持用户管理、角色权限、菜单配置、BOM管理、工艺路线、工单管理、基础数据维护等制造执行核心功能。

## 技术栈

### 后端
- **Spring Boot 2.1.7** - 应用框架
- **MyBatis Plus 3.1.2** - ORM 持久层
- **Apache Shiro** - 权限认证框架
- **Druid 1.1.9** - 数据库连接池
- **MySQL 8.0** - 关系型数据库
- **Redis** - 缓存与会话管理
- **Swagger** - API 文档管理
- **Hutool** - Java 工具类库

### 前端
- **Layui** - 前端 UI 框架
- **FreeMarker** - 服务端模板引擎
- **ECharts** - 数据可视化
- **FontAwesome** - 图标库

### 部署
- **JDK 11**
- **Maven 3.x**
- **Docker + Nginx**（可选）

## 快速开始

### 环境要求

| 依赖 | 版本要求 |
|------|---------|
| JDK | 11+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |
| Redis | 5.0+（可选） |

### 数据库配置

1. 创建 MySQL 数据库：

```sql
CREATE DATABASE mes DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 导入初始数据：

```bash
mysql -u root -p mes < scripts/sql/MySQL-20210225.sql
```

3. 修改数据库连接配置 `mes/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mes?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Hongkong
    username: root
    password: 你的密码
```

### 启动项目

```bash
cd mes
mvn spring-boot:run
```

启动成功后访问：`http://localhost:9090`

- **默认账号**：`admin`
- **默认密码**：`admin`

## 核心功能模块

### 一、用户管理

#### 功能概述
用户管理模块负责系统用户的增删改查、角色分配、状态管理等功能。

#### 代码结构

```
mes/src/main/java/com/wangziyang/mes/system/
├── controller/admin/SysUserController.java    # 用户控制器
├── service/ISysUserService.java               # 用户服务接口
├── service/impl/SysUserServiceImpl.java       # 用户服务实现
├── mapper/SysUserMapper.java                  # 用户数据访问
├── entity/SysUser.java                        # 用户实体
├── request/SysUserPageReq.java                # 用户分页查询参数
└── dto/SysUserDTO.java                        # 用户数据传输对象
```

#### 关键接口

| 接口路径 | 方法 | 说明 |
|---------|------|------|
| `/admin/sys/user/list-ui` | GET | 用户列表页面 |
| `/admin/sys/user/list` | GET | 用户列表数据（分页） |
| `/admin/sys/user/add` | POST | 新增用户 |
| `/admin/sys/user/update` | POST | 修改用户 |
| `/admin/sys/user/delete` | POST | 删除用户 |
| `/admin/sys/user/role-assign` | POST | 角色分配 |

#### 数据库表

**sp_sys_user**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR(64) | 主键ID |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码（加密） |
| real_name | VARCHAR(50) | 真实姓名 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| status | INT | 状态（0禁用/1正常） |
| create_time | DATETIME | 创建时间 |

#### 前端模板

```
mes/src/main/resources/templates/admin/system/user/
├── list.ftl              # 用户列表页面
└── addOrUpdate.ftl       # 新增/编辑页面
```

---

### 二、角色管理

#### 功能概述
角色管理模块负责角色的创建、编辑、删除，以及菜单权限授权功能。系统采用 RBAC（基于角色的访问控制）模型。

#### 代码结构

```
mes/src/main/java/com/wangziyang/mes/system/
├── controller/admin/SysRoleController.java    # 角色控制器
├── service/ISysRoleService.java               # 角色服务接口
├── service/impl/SysRoleServiceImpl.java       # 角色服务实现
├── mapper/SysRoleMapper.java                  # 角色数据访问
├── entity/SysRole.java                        # 角色实体
├── entity/SysRoleMenu.java                    # 角色菜单关联实体
├── request/SysRolePageReq.java                # 角色分页查询参数
└── dto/SysRoleDTO.java                        # 角色数据传输对象
```

#### 关键接口

| 接口路径 | 方法 | 说明 |
|---------|------|------|
| `/admin/sys/role/list-ui` | GET | 角色列表页面 |
| `/admin/sys/role/list` | GET | 角色列表数据（分页） |
| `/admin/sys/role/add` | POST | 新增角色 |
| `/admin/sys/role/update` | POST | 修改角色 |
| `/admin/sys/role/delete` | POST | 删除角色 |
| `/admin/sys/role/menu-tree` | GET | 获取菜单树及已授权ID |
| `/admin/sys/role/save-permissions` | POST | 保存菜单权限 |

#### 权限授权流程

1. 进入"系统管理 > 角色管理"
2. 点击角色"编辑"按钮
3. 调用 `/admin/sys/role/menu-tree?roleId=xxx` 获取菜单树和已授权菜单ID
4. 在菜单树中勾选需要授权的菜单（支持级联选择）
5. 调用 `/admin/sys/role/save-permissions` 保存权限
6. 系统自动向上查找父级目录，确保父菜单也被授权

#### 数据库表

**sp_sys_role**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR(64) | 主键ID |
| role_name | VARCHAR(50) | 角色名称 |
| role_code | VARCHAR(50) | 角色编码 |
| description | VARCHAR(200) | 角色描述 |
| status | INT | 状态（0禁用/1正常） |

**sp_sys_role_menu**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | VARCHAR(64) | 主键ID |
| role_id | VARCHAR(64) | 角色ID |
| menu_id | VARCHAR(64) | 菜单ID |

#### 前端模板

```
mes/src/main/resources/templates/admin/system/role/
├── list.ftl              # 角色列表页面
└── addOrUpdate.ftl       # 新增/编辑页面（含菜单授权树）
```

---

### 三、基础数据中心

#### 功能概述
基础数据中心是与"系统管理"同级的核心模块，提供制造基础数据的统一管理能力，包括班组员工、设备、物料、工序等基础数据的维护。

#### 代码结构

```
mes/src/main/java/com/wangziyang/mes/basedata/
├── config/
│   └── InitDataConfig.java                    # 初始化数据配置
├── controller/                                # 控制器层
│   ├── SpTeamController.java                  # 班组管理
│   ├── SpTeamStaffController.java             # 班组员工管理
│   ├── SpGroupDeviceController.java           # 编组设备定义
│   ├── SpWorkUnitController.java              # 加工单元定义
│   ├── SpMaterielController.java              # 物料信息定义
│   ├── SpWarehouseLocationController.java     # 库房库位定义
│   ├── SpProcessInfoController.java           # 工序信息定义
│   ├── SpPartComponentController.java         # 零部件定义
│   └── SpTableManagerController.java          # 动态表管理
├── entity/                                    # 实体类
│   ├── SpTeam.java                            # 班组实体
│   ├── SpTeamStaff.java                       # 班组员工实体
│   ├── SpGroupDevice.java                     # 编组设备实体
│   ├── SpWorkUnit.java                        # 加工单元实体
│   ├── SpMaterile.java                        # 物料实体
│   ├── SpWarehouseLocation.java               # 库房库位实体
│   ├── SpProcessInfo.java                     # 工序信息实体
│   ├── SpPartComponent.java                   # 零部件实体
│   └── SpTableManager.java                    # 动态表实体
├── service/                                   # 服务接口
│   ├── ISpTeamService.java
│   ├── ISpTeamStaffService.java
│   ├── ISpGroupDeviceService.java
│   ├── ISpWorkUnitService.java
│   ├── ISpMaterileService.java
│   ├── ISpWarehouseLocationService.java
│   ├── ISpProcessInfoService.java
│   └── ISpPartComponentService.java
├── service/impl/                              # 服务实现
│   ├── SpTeamServiceImpl.java
│   ├── SpTeamStaffServiceImpl.java
│   ├── SpGroupDeviceServiceImpl.java
│   ├── SpWorkUnitServiceImpl.java
│   ├── SpMaterileServiceImpl.java
│   ├── SpWarehouseLocationServiceImpl.java
│   ├── SpProcessInfoServiceImpl.java
│   └── SpPartComponentServiceImpl.java
├── mapper/                                    # MyBatis Mapper
│   ├── SpTeamMapper.java
│   ├── SpTeamStaffMapper.java
│   ├── SpGroupDeviceMapper.java
│   ├── SpWorkUnitMapper.java
│   ├── SpMaterileMapper.java
│   ├── SpWarehouseLocationMapper.java
│   ├── SpProcessInfoMapper.java
│   └── SpPartComponentMapper.java
└── request/                                   # 请求参数类
    ├── SpTeamPageReq.java
    ├── SpGroupDeviceReq.java
    ├── SpWorkUnitReq.java
    ├── SpMaterielReq.java
    ├── SpWarehouseLocationReq.java
    ├── SpProcessInfoReq.java
    └── SpPartComponentReq.java
```

#### 功能模块详情

| 子模块 | Controller | 表名 | 说明 |
|--------|-----------|------|------|
| 班组管理 | [`SpTeamController`](file:///C:/Users/罗小兵/Downloads/MES-Springboot-master/MES-Springboot-master/mes/src/main/java/com/wangziyang/mes/basedata/controller/SpTeamController.java) | `sp_team` | 班组创建、代码、名称、描述、状态管理 |
| 班组员工管理 | [`SpTeamStaffController`](file:///C:/Users/罗小兵/Downloads/MES-Springboot-master/MES-Springboot-master/mes/src/main/java/com/wangziyang/mes/basedata/controller/SpTeamStaffController.java) | `sp_team_staff` | 班组与系统用户绑定管理 |
| 编组设备定义 | [`SpGroupDeviceController`](file:///C:/Users/罗小兵/Downloads/MES-Springboot-master/MES-Springboot-master/mes/src/main/java/com/wangziyang/mes/basedata/controller/SpGroupDeviceController.java) | `sp_group_device` | 设备编号、型号、类型、编组、产能管理 |
| 加工单元定义 | [`SpWorkUnitController`](file:///C:/Users/罗小兵/Downloads/MES-Springboot-master/MES-Springboot-master/mes/src/main/java/com/wangziyang/mes/basedata/controller/SpWorkUnitController.java) | `sp_work_unit` | 加工单元编号、类型、位置、产能管理 |
| 物料信息定义 | [`SpMaterielController`](file:///C:/Users/罗小兵/Downloads/MES-Springboot-master/MES-Springboot-master/mes/src/main/java/com/wangziyang/mes/basedata/controller/SpMaterielController.java) | `sp_materile` | 物料基础信息维护 |
| 库房库位定义 | [`SpWarehouseLocationController`](file:///C:/Users/罗小兵/Downloads/MES-Springboot-master/MES-Springboot-master/mes/src/main/java/com/wangziyang/mes/basedata/controller/SpWarehouseLocationController.java) | `sp_warehouse_location` | 库房/库位编号、3D坐标、容量管理 |
| 零部件定义 | [`SpPartComponentController`](file:///C:/Users/罗小兵/Downloads/MES-Springboot-master/MES-Springboot-master/mes/src/main/java/com/wangziyang/mes/basedata/controller/SpPartComponentController.java) | `sp_part_component` | 零部件编号、物料关联、规格、版本管理 |
| 工序信息定义 | [`SpProcessInfoController`](file:///C:/Users/罗小兵/Downloads/MES-Springboot-master/MES-Springboot-master/mes/src/main/java/com/wangziyang/mes/basedata/controller/SpProcessInfoController.java) | `sp_process_info` | 工序编号、所属单元、设备、标准工时管理 |

#### 前端模板

```
mes/src/main/resources/templates/basedata/
├── team/                                      # 班组管理
│   ├── list.ftl
│   └── addOrUpdate.ftl
├── teamStaff/                                 # 班组员工管理
│   └── list.ftl
├── groupDevice/                               # 编组设备定义
│   ├── list.ftl
│   └── addOrUpdate.ftl
├── workUnit/                                  # 加工单元定义
│   ├── list.ftl
│   └── addOrUpdate.ftl
├── materiel/                                  # 物料信息定义
│   ├── list.ftl
│   └── addOrUpdate.ftl
├── warehouseLocation/                         # 库房库位定义
│   ├── list.ftl
│   └── addOrUpdate.ftl
├── processInfo/                               # 工序信息定义
│   ├── list.ftl
│   └── addOrUpdate.ftl
└── partComponent/                             # 零部件定义
    ├── list.ftl
    └── addOrUpdate.ftl
```

---

## 权限体系

### RBAC 模型

系统采用基于角色的访问控制（RBAC）模型：

```
用户 → 角色 → 菜单/按钮权限
```

- **菜单权限**：控制用户可访问的页面
- **按钮权限**：控制页面内的操作（增删改查）
- **数据权限**：按机构划分数据范围（预留）

### 授权流程

```
1. 进入"系统管理 > 角色管理"
   ↓
2. 点击角色"编辑"
   ↓
3. 调用 /admin/sys/role/menu-tree 获取菜单树
   ↓
4. 在菜单树中勾选需要授权的菜单
   ↓
5. 调用 /admin/sys/role/save-permissions 保存权限
   ↓
6. 系统自动向上查找父级目录，确保父菜单可见
   ↓
7. 拥有该角色的用户即可获得对应权限
```

### 菜单树结构

```
基础数据中心
├── 班组员工定义
│   ├── 班组管理
│   └── 员工管理
├── 编组设备定义
├── 加工单元定义
├── 物料信息定义
├── 库房库位定义
├── 零部件定义
└── 工序信息定义
```

---

## 初始化机制

项目通过 `InitDataConfig` 实现自动化初始化，启动时按以下步骤执行：

1. **表结构检查与修复** - 自动检测缺失的表和字段并创建
2. **菜单数据初始化** - 首次启动时导入基础数据中心菜单
3. **管理员账号保障** - 每次启动检查管理员账号是否存在
4. **角色权限授权** - 自动为管理员角色授予基础数据中心菜单权限
5. **用户角色关联修复** - 确保数据库中 `username='admin'` 的账号正确绑定到超级管理员角色

### 配置开关

```yaml
basedata:
  init:
    enabled: true   # 设为 false 可禁用自动初始化（生产环境推荐）
```

---

## 项目结构

```
MES-Springboot-master/
├── mes/                          # 主项目
│   ├── src/main/java/
│   │   └── com/wangziyang/mes/
│   │       ├── basedata/         # 基础数据中心模块
│   │       │   ├── config/       # 初始化配置
│   │       │   ├── controller/   # 控制器层
│   │       │   ├── entity/       # 实体类
│   │       │   ├── mapper/       # MyBatis Mapper
│   │       │   ├── request/      # 请求参数对象
│   │       │   ├── service/      # 业务逻辑层
│   │       │   └── common/       # 公共模块（字典、动态表）
│   │       ├── common/           # 公共组件
│   │       │   ├── config/       # 通用配置
│   │       │   ├── enums/        # 枚举定义
│   │       │   └── util/         # 工具类
│   │       ├── system/           # 系统管理模块
│   │       │   ├── controller/   # 用户/角色/菜单控制器
│   │       │   ├── entity/       # 系统实体类
│   │       │   ├── service/      # 系统业务逻辑
│   │       │   ├── request/      # 请求参数
│   │       │   ├── dto/          # 数据传输对象
│   │       │   └── config/shiro/ # Shiro 安全配置
│   │       ├── technology/       # 工艺管理模块
│   │       ├── order/            # 工单管理模块
│   │       ├── digitization/     # 数字化平台模块
│   │       └── dst/              # 数字孪生模块
│   └── src/main/resources/
│       ├── templates/            # FreeMarker 前端模板
│       ├── static/               # 静态资源（JS/CSS/图片）
│       └── sql/                  # SQL 初始化脚本
├── scripts/sql/                  # 数据库初始化脚本
├── docs/                         # 开发文档
└── pom.xml                       # Maven 配置
```

---

## 数据库表结构

### 系统管理相关表

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sp_sys_user` | 系统用户 | id, username, password, real_name, status |
| `sp_sys_role` | 系统角色 | id, role_name, role_code, description, status |
| `sp_sys_menu` | 系统菜单 | id, menu_name, menu_url, parent_id, permission |
| `sp_sys_user_role` | 用户角色关联 | id, user_id, role_id |
| `sp_sys_role_menu` | 角色菜单关联 | id, role_id, menu_id |
| `sp_sys_department` | 部门组织 | id, dept_name, parent_id |
| `sp_sys_dict` | 数据字典 | id, dict_type, dict_code, dict_value |

### 基础数据中心相关表

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sp_team` | 班组管理 | id, team_code, team_name, status |
| `sp_team_staff` | 班组员工绑定 | id, team_id, user_id, status |
| `sp_group_device` | 编组设备 | id, device_code, device_name, group_code |
| `sp_work_unit` | 加工单元 | id, unit_code, unit_name, unit_type |
| `sp_warehouse_location` | 库房库位 | id, warehouse_code, location_code, x/y/z_coordinate |
| `sp_part_component` | 零部件 | id, part_code, part_name, material_code |
| `sp_process_info` | 工序信息 | id, process_code, process_name, standard_time |

---

## 开发规范

### 代码结构规范
- **请求参数**：每张表的分页查询参数严格对应一个请求参数对象（如 `SysUserPageReq`）
- **枚举管理**：公共枚举放在 `CommonEnum`，模块枚举放在对应包下
- **资源管理**：数据库操作使用 try-with-resources 确保资源正确关闭
- **泛型安全**：`QueryWrapper` 必须指定泛型类型

### 命名规范
- **Entity**: `Sp` + 功能名 + 实体类型（如 `SpGroupDevice`）
- **Controller**: `Sp` + 功能名 + `Controller`
- **Service**: `I` + `Sp` + 功能名 + `Service`（接口）
- **ServiceImpl**: `Sp` + 功能名 + `ServiceImpl`（实现）
- **Mapper**: `Sp` + 功能名 + `Mapper`
- **Request**: `Sp` + 功能名 + `Req` 或 `PageReq`

---

## 常见问题

### 端口被占用

```
Connector configured to listen on port 9090 failed to start
```

**解决**：查找并终止占用 9090 端口的进程

```bash
# Windows
netstat -ano | findstr "9090"
taskkill /PID <进程ID> /F
```

### 管理员账号被误删

系统每次启动时会自动检查并恢复管理员账号，确保不会因误操作导致无法登录。

- **账号**：`admin`
- **密码**：`admin`

### 角色菜单授权后显示全选

已修复。当授权某个子菜单时，系统会自动保存父级目录，但加载时只返回叶子节点ID，避免 Layui tree 组件级联导致全选问题。

---

## License

[MIT License](LICENSE)

## 致谢

本项目基于 [MES-Springboot](https://gitee.com/wangziyangyang/MES-Springboot) 开源项目进行扩展开发。