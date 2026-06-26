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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/basedata/warehouseLocation")
public class SpWarehouseLocationController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SpWarehouseLocationController.class);

    @Autowired
    private ISpWarehouseLocationService iSpWarehouseLocationService;

    @ApiOperation("库房库位管理界面UI")
    @GetMapping("/list-ui")
    @RequiresPermissions("basedata:warehouseLocation:view")
    public String listUI(Model model) {
        return "basedata/warehouseLocation/list";
    }

    @ApiOperation("库房库位管理修改界面")
    @GetMapping("/add-or-update-ui")
    @RequiresPermissions("basedata:warehouseLocation:add")
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
    @RequiresPermissions("basedata:warehouseLocation:view")
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
    @RequiresPermissions("basedata:warehouseLocation:add")
    public Result addOrUpdate(SpWarehouseLocation record) {
        iSpWarehouseLocationService.saveOrUpdate(record);
        return Result.success();
    }

    @ApiOperation("删除库房库位信息")
    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("basedata:warehouseLocation:delete")
    public Result delete(SpWarehouseLocation req) {
        iSpWarehouseLocationService.removeById(req.getId());
        return Result.success();
    }

    @ApiOperation("获取所有库位数据（供3D渲染使用）")
    @GetMapping("/list-all")
    @ResponseBody
    public Result listAll() {
        logger.info("========== 开始查询所有库位数据 ==========");
        QueryWrapper<SpWarehouseLocation> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("warehouse_code", "area_code", "shelf_row", "shelf_column", "shelf_layer");
        java.util.List<SpWarehouseLocation> result = iSpWarehouseLocationService.list(queryWrapper);
        logger.info("查询完成，共 {} 条库位数据", result.size());
        if (result.size() > 0) {
            logger.info("第一条数据: warehouseCode={}, areaCode={}, shelfId={}, status={}", 
                result.get(0).getWarehouseCode(), 
                result.get(0).getAreaCode(), 
                result.get(0).getShelfId(), 
                result.get(0).getStatus());
        }
        return Result.success(result);
    }

    @ApiOperation("获取库位数据按库区分组（供3D渲染使用）")
    @GetMapping("/list-by-area")
    @ResponseBody
    public Result listByArea() {
        QueryWrapper<SpWarehouseLocation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0);
        queryWrapper.orderByAsc("warehouse_code", "area_code", "shelf_row", "shelf_column", "shelf_layer");
        return Result.success(iSpWarehouseLocationService.list(queryWrapper));
    }
}