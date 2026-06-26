package com.wangziyang.mes.basedata.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangziyang.mes.basedata.entity.SpPartComponent;
import com.wangziyang.mes.basedata.request.SpPartComponentReq;
import com.wangziyang.mes.basedata.service.ISpPartComponentService;
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
@RequestMapping("/basedata/partComponent")
public class SpPartComponentController extends BaseController {

    @Autowired
    private ISpPartComponentService iSpPartComponentService;

    @ApiOperation("零部件管理界面UI")
    @GetMapping("/list-ui")
    @RequiresPermissions("basedata:partComponent:view")
    public String listUI(Model model) {
        return "basedata/partComponent/list";
    }

    @ApiOperation("零部件管理修改界面")
    @GetMapping("/add-or-update-ui")
    @RequiresPermissions("basedata:partComponent:add")
    public String addOrUpdateUI(Model model, SpPartComponent record) {
        if (StringUtils.isNotEmpty(record.getId())) {
            SpPartComponent spPartComponent = iSpPartComponentService.getById(record.getId());
            model.addAttribute("result", spPartComponent);
        }
        return "basedata/partComponent/addOrUpdate";
    }

    @ApiOperation("零部件管理界面分页查询")
    @PostMapping("/page")
    @ResponseBody
    @RequiresPermissions("basedata:partComponent:view")
    public Result page(SpPartComponentReq req) {
        QueryWrapper<SpPartComponent> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(req.getPartCode())) {
            queryWrapper.like("part_code", req.getPartCode());
        }
        if (StringUtils.isNotEmpty(req.getPartName())) {
            queryWrapper.like("part_name", req.getPartName());
        }
        if (StringUtils.isNotEmpty(req.getMaterialName())) {
            queryWrapper.like("material_name", req.getMaterialName());
        }
        IPage result = iSpPartComponentService.page(req, queryWrapper);
        return Result.success(result);
    }

    @ApiOperation("零部件管理修改、新增")
    @PostMapping("/add-or-update")
    @ResponseBody
    @RequiresPermissions("basedata:partComponent:add")
    public Result addOrUpdate(SpPartComponent record) {
        iSpPartComponentService.saveOrUpdate(record);
        return Result.success();
    }

    @ApiOperation("删除零部件信息")
    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("basedata:partComponent:delete")
    public Result delete(SpPartComponent req) {
        iSpPartComponentService.removeById(req.getId());
        return Result.success();
    }
}