package com.wangziyang.mes.basedata.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.basedata.entity.SpTeamStaff;
import com.wangziyang.mes.basedata.request.SpTeamStaffPageReq;
import com.wangziyang.mes.basedata.service.ISpTeamStaffService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/basedata/teamStaff")
public class SpTeamStaffController extends BaseController {

    @Autowired
    private ISpTeamStaffService spTeamStaffService;

    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "basedata/teamStaff/list";
    }

    @PostMapping("/page")
    @ResponseBody
    public Result page(SpTeamStaffPageReq req) {
        QueryWrapper<SpTeamStaff> qw = new QueryWrapper<>();
        qw.orderByDesc("create_time");
        if (StringUtils.isNotEmpty(req.getTeamId())) {
            qw.eq("team_id", req.getTeamId());
        }
        IPage<Map<String, Object>> result = spTeamStaffService.pageWithUserInfo(req, qw);
        return Result.success(result);
    }

    @PostMapping("/add")
    @ResponseBody
    public Result add(String teamId, String userId) {
        QueryWrapper<SpTeamStaff> qw = new QueryWrapper<>();
        qw.eq("team_id", teamId);
        qw.eq("user_id", userId);
        if (spTeamStaffService.count(qw) > 0) {
            return Result.failure("该用户已绑定到此班组");
        }
        SpTeamStaff record = new SpTeamStaff();
        record.setTeamId(teamId);
        record.setUserId(userId);
        record.setStatus(1);
        spTeamStaffService.save(record);
        return Result.success();
    }

    @PostMapping("/delete")
    @ResponseBody
    public Result delete(String id) {
        spTeamStaffService.removeById(id);
        return Result.success();
    }

    @GetMapping("/user-tree")
    @ResponseBody
    public Result userTree() {
        List<?> list = spTeamStaffService.listUserTree();
        return Result.success(list);
    }
}
