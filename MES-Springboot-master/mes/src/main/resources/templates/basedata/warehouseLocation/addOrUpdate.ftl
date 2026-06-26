<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>库房库位管理</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form class="layui-form splayui-form" lay-filter="formTest">
            <div class="layui-row">
                <div class="layui-col-xs6 layui-col-sm6 layui-col-md10">
                    <div class="layui-form-item">
                        <label for="js-warehouseCode" class="layui-form-label sp-required">库房编号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-warehouseCode" name="warehouseCode" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.warehouseCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-warehouseName" class="layui-form-label sp-required">库房名称</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-warehouseName" name="warehouseName" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.warehouseName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-warehouseType" class="layui-form-label">库房类型</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-warehouseType" name="warehouseType" autocomplete="off"
                                   class="layui-input" value="${result.warehouseType}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-locationCode" class="layui-form-label sp-required">库位编号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-locationCode" name="locationCode" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.locationCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-locationName" class="layui-form-label">库位名称</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-locationName" name="locationName" autocomplete="off"
                                   class="layui-input" value="${result.locationName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-xCoordinate" class="layui-form-label">X坐标</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-xCoordinate" name="xCoordinate" autocomplete="off"
                                   class="layui-input" value="${result.xCoordinate}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-yCoordinate" class="layui-form-label">Y坐标</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-yCoordinate" name="yCoordinate" autocomplete="off"
                                   class="layui-input" value="${result.yCoordinate}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-zCoordinate" class="layui-form-label">Z坐标</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-zCoordinate" name="zCoordinate" autocomplete="off"
                                   class="layui-input" value="${result.zCoordinate}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-capacity" class="layui-form-label">容量</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-capacity" name="capacity" autocomplete="off"
                                   class="layui-input" value="${result.capacity}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-currentQty" class="layui-form-label">当前数量</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-currentQty" name="currentQty" autocomplete="off"
                                   class="layui-input" value="${result.currentQty}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-status" class="layui-form-label sp-required">状态</label>
                        <div class="layui-input-block" id="js-status" style="width: 310px;">
                            <input type="radio" name="status" value="0" title="可用"
                                   <#if result.status == 0 || !(result??)>checked</#if>>
                            <input type="radio" name="status" value="1" title="占用"
                                   <#if result.status == 1>checked</#if>>
                            <input type="radio" name="status" value="2" title="禁用"
                                   <#if result.status == 2>checked</#if>>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-areaCode" class="layui-form-label">库区编码</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-areaCode" name="areaCode" autocomplete="off"
                                   class="layui-input" value="${result.areaCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-shelfId" class="layui-form-label">货架ID</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-shelfId" name="shelfId" autocomplete="off"
                                   class="layui-input" value="${result.shelfId}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-shelfRow" class="layui-form-label">货架行号</label>
                        <div class="layui-input-inline">
                            <input type="number" id="js-shelfRow" name="shelfRow" autocomplete="off"
                                   class="layui-input" value="${result.shelfRow}">
                        </div>
                        <label for="js-shelfColumn" class="layui-form-label">货架列号</label>
                        <div class="layui-input-inline">
                            <input type="number" id="js-shelfColumn" name="shelfColumn" autocomplete="off"
                                   class="layui-input" value="${result.shelfColumn}">
                        </div>
                        <label for="js-shelfLayer" class="layui-form-label">货架层号</label>
                        <div class="layui-input-inline">
                            <input type="number" id="js-shelfLayer" name="shelfLayer" autocomplete="off"
                                   class="layui-input" value="${result.shelfLayer}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-color" class="layui-form-label">3D显示颜色</label>
                        <div class="layui-input-inline">
                            <select id="js-color" name="color">
                                <option value="#90EE90" <#if result.color == "#90EE90">selected</#if>>绿色-可用</option>
                                <option value="#FF6347" <#if result.color == "#FF6347">selected</#if>>红色-占用</option>
                                <option value="#FFD700" <#if result.color == "#FFD700">selected</#if>>金色-特殊</option>
                                <option value="#87CEEB" <#if result.color == "#87CEEB">selected</#if>>蓝色-待处理</option>
                                <option value="#D3D3D3" <#if result.color == "#D3D3D3">selected</#if>>灰色-禁用</option>
                            </select>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-modelType" class="layui-form-label">模型类型</label>
                        <div class="layui-input-inline">
                            <select id="js-modelType" name="modelType">
                                <option value="SHELF" <#if result.modelType == "SHELF">selected</#if>>货架</option>
                                <option value="RACK" <#if result.modelType == "RACK">selected</#if>>立库</option>
                                <option value="LOCATION" <#if result.modelType == "LOCATION">selected</#if>>库位</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="layui-form-item layui-hide">
                    <div class="layui-input-block">
                        <input id="js-id" name="id" value="${result.id}"/>
                        <button id="js-submit" class="layui-btn" lay-submit lay-filter="js-submit-filter">确定</button>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>
<script>
    layui.use(['form', 'util'], function () {
        var form = layui.form,
            util = layui.util;

        form.on('submit(js-submit-filter)', function (data) {
            spUtil.submitForm({
                url: "${request.contextPath}/basedata/warehouseLocation/add-or-update",
                data: data.field
            });
            return false;
        });
    });
</script>
</body>
</html>