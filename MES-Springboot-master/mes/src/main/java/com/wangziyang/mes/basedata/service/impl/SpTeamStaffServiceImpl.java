package com.wangziyang.mes.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangziyang.mes.basedata.entity.SpTeamStaff;
import com.wangziyang.mes.basedata.mapper.SpTeamStaffMapper;
import com.wangziyang.mes.basedata.service.ISpTeamStaffService;
import com.wangziyang.mes.system.entity.SysUser;
import com.wangziyang.mes.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SpTeamStaffServiceImpl extends ServiceImpl<SpTeamStaffMapper, SpTeamStaff> implements ISpTeamStaffService {

    @Autowired
    private SysUserMapper sysUserMapper;

    public IPage<Map<String, Object>> pageWithUserInfo(Page<SpTeamStaff> page, QueryWrapper<SpTeamStaff> queryWrapper) {
        IPage<SpTeamStaff> result = super.page(page, queryWrapper);
        
        List<Map<String, Object>> records = new ArrayList<>();
        for (SpTeamStaff item : result.getRecords()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("teamId", item.getTeamId());
            map.put("userId", item.getUserId());
            map.put("status", item.getStatus());
            
            if (item.getUserId() != null) {
                SysUser user = sysUserMapper.selectById(item.getUserId());
                if (user != null) {
                    map.put("userName", user.getName() + "(" + user.getUsername() + ")");
                } else {
                    map.put("userName", item.getUserId());
                }
            }
            
            records.add(map);
        }
        
        IPage<Map<String, Object>> pageResult = new Page<>();
        pageResult.setRecords(records);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());
        
        return pageResult;
    }

    @Override
    public List<Map<String, Object>> listUserTree() {
        List<SysUser> userList = sysUserMapper.selectList(null);
        List<Map<String, Object>> tree = new ArrayList<>();
        Map<String, Map<String, Object>> deptMap = new LinkedHashMap<>();

        for (SysUser user : userList) {
            if ("1".equals(user.getDeleted())) {
                continue;
            }
            String deptId = user.getDeptId();
            if (deptId == null) {
                deptId = "未分配部门";
            }
            Map<String, Object> deptNode = deptMap.get(deptId);
            if (deptNode == null) {
                deptNode = new LinkedHashMap<>();
                deptNode.put("id", deptId);
                deptNode.put("name", deptId);
                deptNode.put("spread", true);
                List<Map<String, Object>> children = new ArrayList<>();
                deptNode.put("children", children);
                deptMap.put(deptId, deptNode);
                tree.add(deptNode);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) deptNode.get("children");
            Map<String, Object> userNode = new LinkedHashMap<>();
            userNode.put("id", user.getId());
            userNode.put("name", user.getName() + "(" + user.getUsername() + ")");
            userNode.put("isUser", true);
            children.add(userNode);
        }
        return tree;
    }
}
