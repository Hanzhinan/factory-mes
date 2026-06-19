package com.wangziyang.mes.basedata.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.basedata.entity.SpTeamStaff;
import com.wangziyang.mes.basedata.request.SpTeamStaffReq;
import com.wangziyang.mes.basedata.service.ISpTeamStaffService;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/basedata/teamStaff")
public class SpTeamStaffController extends BaseController {

    @Autowired
    private ISpTeamStaffService iSpTeamStaffService;

    @ApiOperation("班组员工管理界面UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "basedata/teamStaff/list";
    }

    @ApiOperation("班组员工管理修改界面")
    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, SpTeamStaff record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SpTeamStaff spTeamStaff = iSpTeamStaffService.getById(record.getId());
            model.addAttribute("result", spTeamStaff);
        }
        return "basedata/teamStaff/addOrUpdate";
    }

    @ApiOperation("班组员工管理界面分页查询")
    @PostMapping("/page")
    @ResponseBody
    public Result page(SpTeamStaffReq req) {
        QueryWrapper<SpTeamStaff> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getStaffCode())) {
            queryWrapper.like("staff_code", req.getStaffCode());
        }
        if (StringUtils.isNotEmpty(req.getStaffName())) {
            queryWrapper.like("staff_name", req.getStaffName());
        }
        if (StringUtils.isNotEmpty(req.getTeamName())) {
            queryWrapper.like("team_name", req.getTeamName());
        }
        IPage result = iSpTeamStaffService.page(req, queryWrapper);
        return Result.success(result);
    }

    @ApiOperation("班组员工管理修改、新增")
    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(SpTeamStaff record) {
        iSpTeamStaffService.saveOrUpdate(record);
        return Result.success();
    }

    @ApiOperation("删除班组员工信息")
    @PostMapping("/delete")
    @ResponseBody
    public Result delete(SpTeamStaff req) {
        iSpTeamStaffService.removeById(req.getId());
        return Result.success();
    }
}