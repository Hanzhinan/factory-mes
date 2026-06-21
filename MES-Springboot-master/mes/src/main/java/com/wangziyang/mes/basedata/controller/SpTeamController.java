package com.wangziyang.mes.basedata.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import com.wangziyang.mes.basedata.entity.SpTeam;
import com.wangziyang.mes.basedata.request.SpTeamPageReq;
import com.wangziyang.mes.basedata.service.ISpTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/basedata/team")
public class SpTeamController extends BaseController {

    @Autowired
    private ISpTeamService spTeamService;

    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "basedata/team/list";
    }

    @PostMapping("/page")
    @ResponseBody
    public Result page(SpTeamPageReq req) {
        QueryWrapper<SpTeam> qw = new QueryWrapper<>();
        qw.orderByDesc("create_time");
        if (StringUtils.isNotEmpty(req.getTeamCode())) {
            qw.like("team_code", req.getTeamCode());
        }
        if (StringUtils.isNotEmpty(req.getTeamName())) {
            qw.like("team_name", req.getTeamName());
        }
        IPage<SpTeam> result = spTeamService.page(req, qw);
        return Result.success(result);
    }

    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, SpTeam record) {
        SpTeam result;
        if (StringUtils.isNotEmpty(record.getId())) {
            result = spTeamService.getById(record.getId());
        } else {
            result = new SpTeam();
            result.setStatus(1);
        }
        model.addAttribute("result", result);
        return "basedata/team/addOrUpdate";
    }

    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(SpTeam record) {
        spTeamService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @PostMapping("/delete")
    @ResponseBody
    public Result delete(String id) {
        spTeamService.removeById(id);
        return Result.success();
    }
}
