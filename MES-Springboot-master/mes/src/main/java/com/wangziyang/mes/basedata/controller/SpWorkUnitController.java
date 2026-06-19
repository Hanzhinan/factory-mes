package com.wangziyang.mes.basedata.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.basedata.entity.SpWorkUnit;
import com.wangziyang.mes.basedata.request.SpWorkUnitReq;
import com.wangziyang.mes.basedata.service.ISpWorkUnitService;
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
@RequestMapping("/basedata/workUnit")
public class SpWorkUnitController extends BaseController {

    @Autowired
    private ISpWorkUnitService iSpWorkUnitService;

    @ApiOperation("加工单元管理界面UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "basedata/workUnit/list";
    }

    @ApiOperation("加工单元管理修改界面")
    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, SpWorkUnit record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SpWorkUnit spWorkUnit = iSpWorkUnitService.getById(record.getId());
            model.addAttribute("result", spWorkUnit);
        }
        return "basedata/workUnit/addOrUpdate";
    }

    @ApiOperation("加工单元管理界面分页查询")
    @PostMapping("/page")
    @ResponseBody
    public Result page(SpWorkUnitReq req) {
        QueryWrapper<SpWorkUnit> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getUnitCode())) {
            queryWrapper.like("unit_code", req.getUnitCode());
        }
        if (StringUtils.isNotEmpty(req.getUnitName())) {
            queryWrapper.like("unit_name", req.getUnitName());
        }
        IPage result = iSpWorkUnitService.page(req, queryWrapper);
        return Result.success(result);
    }

    @ApiOperation("加工单元管理修改、新增")
    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(SpWorkUnit record) {
        iSpWorkUnitService.saveOrUpdate(record);
        return Result.success();
    }

    @ApiOperation("删除加工单元信息")
    @PostMapping("/delete")
    @ResponseBody
    public Result delete(SpWorkUnit req) {
        iSpWorkUnitService.removeById(req.getId());
        return Result.success();
    }
}