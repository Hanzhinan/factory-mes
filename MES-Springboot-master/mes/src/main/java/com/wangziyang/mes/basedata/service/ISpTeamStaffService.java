package com.wangziyang.mes.basedata.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wangziyang.mes.basedata.entity.SpTeamStaff;

import java.util.List;
import java.util.Map;

public interface ISpTeamStaffService extends IService<SpTeamStaff> {

    IPage<Map<String, Object>> pageWithUserInfo(Page<SpTeamStaff> page, QueryWrapper<SpTeamStaff> queryWrapper);

    List<Map<String, Object>> listUserTree();
}
