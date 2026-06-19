package com.wangziyang.mes.basedata.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.basedata.entity.SpGroupDevice;
import com.wangziyang.mes.basedata.request.SpGroupDeviceReq;
import com.wangziyang.mes.basedata.service.ISpGroupDeviceService;
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
@RequestMapping("/basedata/groupDevice")
public class SpGroupDeviceController extends BaseController {

    @Autowired
    private ISpGroupDeviceService iSpGroupDeviceService;

    @ApiOperation("编组设备管理界面UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "basedata/groupDevice/list";
    }

    @ApiOperation("编组设备管理修改界面")
    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, SpGroupDevice record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SpGroupDevice spGroupDevice = iSpGroupDeviceService.getById(record.getId());
            model.addAttribute("result", spGroupDevice);
        }
        return "basedata/groupDevice/addOrUpdate";
    }

    @ApiOperation("编组设备管理界面分页查询")
    @PostMapping("/page")
    @ResponseBody
    public Result page(SpGroupDeviceReq req) {
        QueryWrapper<SpGroupDevice> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getDeviceCode())) {
            queryWrapper.like("device_code", req.getDeviceCode());
        }
        if (StringUtils.isNotEmpty(req.getDeviceName())) {
            queryWrapper.like("device_name", req.getDeviceName());
        }
        if (StringUtils.isNotEmpty(req.getGroupName())) {
            queryWrapper.like("group_name", req.getGroupName());
        }
        IPage result = iSpGroupDeviceService.page(req, queryWrapper);
        return Result.success(result);
    }

    @ApiOperation("编组设备管理修改、新增")
    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(SpGroupDevice record) {
        iSpGroupDeviceService.saveOrUpdate(record);
        return Result.success();
    }

    @ApiOperation("删除编组设备信息")
    @PostMapping("/delete")
    @ResponseBody
    public Result delete(SpGroupDevice req) {
        iSpGroupDeviceService.removeById(req.getId());
        return Result.success();
    }
}