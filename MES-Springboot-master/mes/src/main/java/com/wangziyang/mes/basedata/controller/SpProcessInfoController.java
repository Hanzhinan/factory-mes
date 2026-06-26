package com.wangziyang.mes.basedata.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.basedata.entity.SpProcessInfo;
import com.wangziyang.mes.basedata.request.SpProcessInfoReq;
import com.wangziyang.mes.basedata.service.ISpProcessInfoService;
import com.wangziyang.mes.common.BaseController;
import com.wangziyang.mes.common.Result;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/basedata/processInfo")
public class SpProcessInfoController extends BaseController {

    @Autowired
    private ISpProcessInfoService iSpProcessInfoService;

    @ApiOperation("工序信息管理界面UI")
    @GetMapping("/list-ui")
    @RequiresPermissions("basedata:processInfo:view")
    public String listUI(Model model) {
        return "basedata/processInfo/list";
    }

    @ApiOperation("工序信息管理修改界面")
    @GetMapping("/add-or-update-ui")
    @RequiresPermissions("basedata:processInfo:add")
    public String addOrUpdateUI(Model model, SpProcessInfo record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SpProcessInfo spProcessInfo = iSpProcessInfoService.getById(record.getId());
            model.addAttribute("result", spProcessInfo);
        }
        return "basedata/processInfo/addOrUpdate";
    }

    @ApiOperation("工序信息管理界面分页查询")
    @PostMapping("/page")
    @ResponseBody
    @RequiresPermissions("basedata:processInfo:view")
    public Result page(SpProcessInfoReq req) {
        QueryWrapper<SpProcessInfo> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getProcessCode())) {
            queryWrapper.like("process_code", req.getProcessCode());
        }
        if (StringUtils.isNotEmpty(req.getProcessName())) {
            queryWrapper.like("process_name", req.getProcessName());
        }
        if (StringUtils.isNotEmpty(req.getWorkUnitName())) {
            queryWrapper.like("work_unit_name", req.getWorkUnitName());
        }
        queryWrapper.orderByAsc("sequence");
        IPage result = iSpProcessInfoService.page(req, queryWrapper);
        return Result.success(result);
    }

    @ApiOperation("工序信息管理修改、新增")
    @PostMapping("/add-or-update")
    @ResponseBody
    @RequiresPermissions("basedata:processInfo:add")
    public Result addOrUpdate(SpProcessInfo record) {
        iSpProcessInfoService.saveOrUpdate(record);
        return Result.success();
    }

    @ApiOperation("删除工序信息")
    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("basedata:processInfo:delete")
    public Result delete(SpProcessInfo req) {
        iSpProcessInfoService.removeById(req.getId());
        return Result.success();
    }
}