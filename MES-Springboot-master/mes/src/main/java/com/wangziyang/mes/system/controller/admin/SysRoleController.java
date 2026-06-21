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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller("adminSysRoleController")
@RequestMapping("/admin/sys/role")
public class SysRoleController extends BaseController {

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
        sysRoleService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @GetMapping("/menu-tree")
    @ResponseBody
    public Result menuTree(String roleId) throws Exception {
        List<TreeVO<SysMenu>> menuTree = sysMenuService.listMenuTree();
        if (StringUtils.isNotEmpty(roleId)) {
            List<SysRoleMenu> roleMenus = sysRoleMenuService.list(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
            List<String> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
            markChecked(menuTree, menuIds);
        }
        return Result.success(menuTree);
    }

    private void markChecked(List<TreeVO<SysMenu>> treeNodes, List<String> menuIds) {
        for (TreeVO<SysMenu> node : treeNodes) {
            if (menuIds.contains(node.getId())) {
                node.setChecked(true);
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                markChecked(node.getChildren(), menuIds);
            }
        }
    }

    @PostMapping("/save-permissions")
    @ResponseBody
    public Result savePermissions(String roleId, @RequestBody List<String> menuIds) {
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("role_id", roleId));
        for (String menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setId(IdUtil.nextId());
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setCreateUsername("system");
            roleMenu.setUpdateUsername("system");
            sysRoleMenuService.save(roleMenu);
        }
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
