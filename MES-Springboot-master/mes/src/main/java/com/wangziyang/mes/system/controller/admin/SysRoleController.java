package com.wangziyang.mes.system.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.common.util.IdUtil;
import com.wangziyang.mes.system.entity.SysMenu;
import com.wangziyang.mes.system.entity.SysRole;
import com.wangziyang.mes.system.entity.SysRoleMenu;
import com.wangziyang.mes.system.request.SysRolePageReq;
import com.wangziyang.mes.system.service.ISysMenuService;
import com.wangziyang.mes.system.service.ISysRoleMenuService;
import com.wangziyang.mes.system.service.ISysRoleService;
import com.wangziyang.mes.system.vo.TreeVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("adminSysRoleController")
@RequestMapping("/admin/sys/role")
public class SysRoleController extends BaseController {

    Logger logger = LoggerFactory.getLogger(SysRoleController.class);

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysMenuService sysMenuService;

    @Autowired
    private ISysRoleMenuService sysRoleMenuService;

    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "admin/system/role/list";
    }

    @PostMapping("/page")
    @ResponseBody
    public Result page(SysRolePageReq req) {
        QueryWrapper qw = new QueryWrapper();
        qw.orderByDesc(req.getOrderBy());
        IPage result = sysRoleService.page(req, qw);
        return Result.success(result);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, SysRole record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SysRole result = sysRoleService.getById(record.getId());
            model.addAttribute("result", result);
        }
        return "admin/system/role/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(SysRole record) {
        logger.info("========== 开始处理角色保存/更新 ==========");
        logger.info("接收到的角色数据: id={}, name={}, code={}, descr={}, deleted={}", 
            record.getId(), record.getName(), record.getCode(), record.getDescr(), record.getDeleted());
        
        if (StringUtils.isEmpty(record.getId())) {
            logger.info("id为空，执行新增操作");
            sysRoleService.save(record);
        } else {
            logger.info("id不为空，执行更新操作, id={}", record.getId());
            sysRoleService.updateById(record);
        }
        
        logger.info("角色保存/更新处理结束, id={}", record.getId());
        return Result.success(record.getId());
    }

    @GetMapping("/menu-tree")
    @ResponseBody
    public Result menuTree(String roleId) throws Exception {
        List<TreeVO<SysMenu>> menuTree = sysMenuService.listMenuTree();
        // 只保留基础数据中心及其子菜单
        menuTree = filterBaseDataCenterMenu(menuTree);
        
        Map<String, Object> result = new HashMap<>();
        result.put("tree", menuTree);
        
        if (StringUtils.isNotEmpty(roleId)) {
            List<SysRoleMenu> roleMenus = sysRoleMenuService.list(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
            List<String> allMenuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
            // 只返回叶子节点的ID，避免父节点级联勾选所有子节点
            List<String> leafMenuIds = filterLeafMenuIds(menuTree, allMenuIds);
            logger.info("角色ID: {}, 原始授权菜单ID: {}, 过滤后叶子节点ID: {}", roleId, allMenuIds, leafMenuIds);
            result.put("authorizedIds", leafMenuIds);
        } else {
            result.put("authorizedIds", new ArrayList<>());
        }
        
        return Result.success(result);
    }
    
    /**
     * 过滤出只属于叶子节点的菜单ID
     */
    private List<String> filterLeafMenuIds(List<TreeVO<SysMenu>> treeNodes, List<String> menuIds) {
        List<String> leafIds = new ArrayList<>();
        for (TreeVO<SysMenu> node : treeNodes) {
            if (node.getChildren() == null || node.getChildren().isEmpty()) {
                if (menuIds.contains(node.getId())) {
                    leafIds.add(node.getId());
                }
            } else {
                leafIds.addAll(filterLeafMenuIds(node.getChildren(), menuIds));
            }
        }
        return leafIds;
    }

    /**
     * 过滤菜单树，只保留基础数据中心（id=20）及其子菜单
     */
    private List<TreeVO<SysMenu>> filterBaseDataCenterMenu(List<TreeVO<SysMenu>> treeNodes) {
        List<TreeVO<SysMenu>> result = new ArrayList<>();
        for (TreeVO<SysMenu> node : treeNodes) {
            if ("20".equals(node.getId())) {
                result.add(node);
            } else if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                List<TreeVO<SysMenu>> found = filterBaseDataCenterMenu(node.getChildren());
                if (!found.isEmpty()) {
                    result.addAll(found);
                }
            }
        }
        return result;
    }

    private void markChecked(List<TreeVO<SysMenu>> treeNodes, List<String> menuIds) {
        for (TreeVO<SysMenu> node : treeNodes) {
            // 不设置任何节点的 checked 属性，全部保持 false
            // 前端使用 tree.setChecked 方法来精确控制勾选状态
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                markChecked(node.getChildren(), menuIds);
            }
        }
    }

    @PostMapping("/save-permissions")
    @ResponseBody
    public Result savePermissions(String roleId, @RequestBody List<String> menuIds) {
        logger.info("========== 开始保存角色权限 ==========");
        logger.info("角色ID: {}, 提交菜单ID列表: {}", roleId, menuIds);

        // 查询所有菜单，向上查找所有父级ID，确保叶子节点的父目录都被授权
        java.util.Set<String> allMenuIds = new java.util.LinkedHashSet<>(menuIds);
        if (!menuIds.isEmpty()) {
            java.util.List<SysMenu> allMenus = sysMenuService.list();
            java.util.Map<String, String> idToParentMap = new java.util.HashMap<>();
            for (SysMenu m : allMenus) {
                idToParentMap.put(m.getId(), m.getParentId());
            }
            for (String menuId : menuIds) {
                String pid = idToParentMap.get(menuId);
                while (pid != null && !"0".equals(pid) && !allMenuIds.contains(pid)) {
                    allMenuIds.add(pid);
                    pid = idToParentMap.get(pid);
                }
            }
        }
        logger.info("补充父级目录后, 最终保存菜单ID列表: {}", allMenuIds);

        List<SysRoleMenu> oldRoleMenus = sysRoleMenuService.list(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
        logger.info("旧权限记录数量: {}", oldRoleMenus.size());

        if (!oldRoleMenus.isEmpty()) {
            List<String> oldIds = oldRoleMenus.stream().map(SysRoleMenu::getId).collect(Collectors.toList());
            sysRoleMenuService.removeByIds(oldIds);
            logger.info("已删除旧权限记录: {}", oldIds.size());
        }

        for (String menuId : allMenuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setId(IdUtil.nextId());
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setCreateUsername("system");
            roleMenu.setUpdateUsername("system");
            sysRoleMenuService.save(roleMenu);
        }

        logger.info("已保存新权限记录: {}", allMenuIds.size());
        logger.info("========== 角色权限保存结束 ==========");
        return Result.success();
    }

    @PostMapping("/delete")
    @ResponseBody
    public Result delete(String id) {
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("role_id", id));
        sysRoleService.removeById(id);
        return Result.success();
    }
}
