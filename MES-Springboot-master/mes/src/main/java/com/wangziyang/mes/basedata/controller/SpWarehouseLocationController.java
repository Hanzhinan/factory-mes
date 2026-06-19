package com.wangziyang.mes.basedata.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.basedata.entity.SpWarehouseLocation;
import com.wangziyang.mes.basedata.request.SpWarehouseLocationReq;
import com.wangziyang.mes.basedata.service.ISpWarehouseLocationService;
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
@RequestMapping("/basedata/warehouseLocation")
public class SpWarehouseLocationController extends BaseController {

    @Autowired
    private ISpWarehouseLocationService iSpWarehouseLocationService;

    @ApiOperation("库房库位管理界面UI")
    @GetMapping("/list-ui")
    public String listUI(Model model) {
        return "basedata/warehouseLocation/list";
    }

    @ApiOperation("库房库位管理修改界面")
    @GetMapping("/add-or-update-ui")
    public String addOrUpdateUI(Model model, SpWarehouseLocation record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SpWarehouseLocation spWarehouseLocation = iSpWarehouseLocationService.getById(record.getId());
            model.addAttribute("result", spWarehouseLocation);
        }
        return "basedata/warehouseLocation/addOrUpdate";
    }

    @ApiOperation("库房库位管理界面分页查询")
    @PostMapping("/page")
    @ResponseBody
    public Result page(SpWarehouseLocationReq req) {
        QueryWrapper<SpWarehouseLocation> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getWarehouseCode())) {
            queryWrapper.like("warehouse_code", req.getWarehouseCode());
        }
        if (StringUtils.isNotEmpty(req.getWarehouseName())) {
            queryWrapper.like("warehouse_name", req.getWarehouseName());
        }
        if (StringUtils.isNotEmpty(req.getLocationCode())) {
            queryWrapper.like("location_code", req.getLocationCode());
        }
        IPage result = iSpWarehouseLocationService.page(req, queryWrapper);
        return Result.success(result);
    }

    @ApiOperation("库房库位管理修改、新增")
    @PostMapping("/add-or-update")
    @ResponseBody
    public Result addOrUpdate(SpWarehouseLocation record) {
        iSpWarehouseLocationService.saveOrUpdate(record);
        return Result.success();
    }

    @ApiOperation("删除库房库位信息")
    @PostMapping("/delete")
    @ResponseBody
    public Result delete(SpWarehouseLocation req) {
        iSpWarehouseLocationService.removeById(req.getId());
        return Result.success();
    }
}