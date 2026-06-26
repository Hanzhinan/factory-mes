package com.wangziyang.mes.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.common.util.TreeUtil;
import com.wangziyang.mes.system.dto.SysMenuDTO;
import com.wangziyang.mes.system.entity.SysMenu;
import com.wangziyang.mes.system.mapper.SysMenuMapper;
import com.wangziyang.mes.system.service.ISysMenuService;
import com.wangziyang.mes.system.vo.TreeVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author SongPeng
 * @since 2019-10-16
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private static final Logger logger = LoggerFactory.getLogger(SysMenuServiceImpl.class);

    @Autowired
    private SysMenuMapper sysMenuMapper;

    /**
     * 根据角色id查询菜单列表
     *
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public List<SysMenuDTO> listByRoleId(String roleId) throws Exception {
        return sysMenuMapper.listByRoleId(roleId);
    }

    /**
     * 系统首页初始化菜单树数据
     *
     * @return 系统首页初始化菜单树数据
     * @throws Exception 异常
     */
    @Override
    public Map<String, Object> listIndexMenuTree() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>(4);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.orderBy(true, true, "sort_num");
        List<SysMenu> sysMenus = sysMenuMapper.selectList(queryWrapper);

        Map<String, String> clearInfo = new HashMap<>(2);
        clearInfo.put("clearUrl", "json/clear.json");

        Map<String, String> homeInfo = new HashMap<>(4);
        homeInfo.put("name", "首页");
        homeInfo.put("icon", "fa fa-home");
        homeInfo.put("url", "admin/welcome-ui");

        Map<String, String> logoInfo = new HashMap<>(4);
        logoInfo.put("name", "黑科制造MES");
        logoInfo.put("image", "/image/logo.png");
        logoInfo.put("url", "");

        Map<String, Object> menuInfo = new LinkedHashMap<>(8);

        Subject subject = SecurityUtils.getSubject();
        Set<String> userPermissions = new HashSet<>();
        Set<String> roleMenuIds = new HashSet<>();
        String currentUsername = "anonymous";
        int roleCount = 0;
        int menuCount = 0;
        if (subject != null && subject.isAuthenticated()) {
            Object principal = subject.getPrincipal();
            if (principal instanceof com.wangziyang.mes.system.dto.SysUserDTO) {
                com.wangziyang.mes.system.dto.SysUserDTO user = (com.wangziyang.mes.system.dto.SysUserDTO) principal;
                currentUsername = user.getUsername();
                if (user.getSysRoleDTOs() != null) {
                    roleCount = user.getSysRoleDTOs().size();
                    for (com.wangziyang.mes.system.dto.SysRoleDTO role : user.getSysRoleDTOs()) {
                        if (role.getSysMenuDtos() != null) {
                            menuCount += role.getSysMenuDtos().size();
                            for (com.wangziyang.mes.system.dto.SysMenuDTO menu : role.getSysMenuDtos()) {
                                roleMenuIds.add(menu.getId());
                                if (StringUtils.isNotEmpty(menu.getPermission())) {
                                    String[] perms = menu.getPermission().trim().split(",");
                                    for (String perm : perms) {
                                        userPermissions.add(perm.trim());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("==========[菜单权限过滤] 用户=" + currentUsername + ", 角色数=" + roleCount + ", 关联菜单数=" + menuCount + ", 角色菜单ID=" + roleMenuIds + ", 解析后权限=" + userPermissions + "==========");
        logger.info("用户 {} 角色数:{} 关联菜单数:{} 角色菜单ID:{} 权限:{}", currentUsername, roleCount, menuCount, roleMenuIds, userPermissions);

        List<TreeVO<SysMenu>> menus = new ArrayList<>();
        Set<String> allowedMenuIds = new HashSet<>();

        // admin 用户或没有任何权限时，显示全部菜单
        if ("admin".equals(currentUsername) || userPermissions.isEmpty() || roleMenuIds.isEmpty()) {
            for (SysMenu m : sysMenus) {
                allowedMenuIds.add(m.getId());
            }
        } else {
            // 直接使用角色关联的菜单ID列表，而不是通过permission字段匹配
            for (SysMenu m : sysMenus) {
                if (roleMenuIds.contains(m.getId())) {
                    allowedMenuIds.add(m.getId());
                }
            }

            // 向上查找所有父级目录，确保父目录也显示
            boolean changed = true;
            while (changed) {
                changed = false;
                for (SysMenu m : sysMenus) {
                    String pid = m.getParentId();
                    if ("0".equals(pid)) continue;
                    if (allowedMenuIds.contains(m.getId()) && !allowedMenuIds.contains(pid)) {
                        allowedMenuIds.add(pid);
                        changed = true;
                    }
                }
            }
        }
        logger.info("用户 {} 最终允许的菜单ID数量: {}", currentUsername, allowedMenuIds.size());

        for (SysMenu m : sysMenus) {
            if (allowedMenuIds.contains(m.getId())) {
                TreeVO<SysMenu> tree = new TreeVO<>();
                tree.setId(m.getId());
                tree.setPid(m.getParentId());
                tree.setCode(m.getCode());
                tree.setName(m.getName());
                tree.setUrl(m.getUrl());
                tree.setIcon(m.getIcon());
                tree.setType(m.getType());
                tree.setPermission(m.getPermission());
                tree.setTarget("_self");
                menus.add(tree);
            }
        }
        List<TreeVO<SysMenu>> treeVOS = TreeUtil.buildList(menus, "0");
        for (TreeVO<SysMenu> mTree : treeVOS) {
            menuInfo.put(mTree.getCode(), mTree);
        }

        result.put("clearInfo", clearInfo);
        result.put("homeInfo", homeInfo);
        result.put("logoInfo", logoInfo);
        result.put("menuInfo", menuInfo);

        return result;
    }

    /**
     * 用户搜索系统首页初始化菜单树数据
     * @param menuName 菜单名字
     * @return 菜单树数据
     * @throws Exception 异常
     */
    @Override
    public Map<String, Object> listIndexMenuSearchTree(String menuName) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>(4);
        Map<String, Object> menuInfo = new LinkedHashMap<>(8);
        List<SysMenu> sysMenus = sysMenuMapper.listBySearchByName(menuName);
        List<TreeVO<SysMenu>> menus = new ArrayList<>();
        for (SysMenu m : sysMenus) {
            TreeVO<SysMenu> tree = new TreeVO<>();
            tree.setId(m.getId());
            tree.setPid(m.getParentId());
            tree.setCode(m.getCode());
            tree.setName(m.getName());
            tree.setUrl(m.getUrl());
            tree.setIcon(m.getIcon());
            tree.setType(m.getType());
            tree.setPermission(m.getPermission());
            // TODO 是否需要更改？
            tree.setTarget("_self");
            menus.add(tree);
        }
        List<TreeVO<SysMenu>> treeVOS = TreeUtil.buildList(menus, "0");
        for (TreeVO<SysMenu> mTree : treeVOS) {
            menuInfo.put(mTree.getCode(), mTree);
        }
        result.put("menuInfo", menuInfo);
        return result;
    }

    /**
     * 获取系统菜单树
     *
     * @return 系统菜单树
     * @throws Exception 异常
     */
    @Override
    public List<TreeVO<SysMenu>> listMenuTree() throws Exception {
        List<TreeVO<SysMenu>> menus = new ArrayList<>();
        List<SysMenu> sysMenus = sysMenuMapper.selectList(null);
        for (SysMenu m : sysMenus) {
            TreeVO<SysMenu> tree = new TreeVO<>();
            tree.setId(m.getId());
            tree.setPid(m.getParentId());
            tree.setCode(m.getCode());
            tree.setName(m.getName());
            tree.setUrl(m.getUrl());
            tree.setIcon(m.getIcon());
            tree.setType(m.getType());
            tree.setPermission(m.getPermission());
            menus.add(tree);
        }
        return TreeUtil.buildList(menus, "0");
    }
}
