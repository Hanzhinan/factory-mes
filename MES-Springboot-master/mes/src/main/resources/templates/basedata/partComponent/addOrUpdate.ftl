<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>零部件管理</title>
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
                        <label for="js-partCode" class="layui-form-label sp-required">零部件编号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-partCode" name="partCode" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.partCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-partName" class="layui-form-label sp-required">零部件名称</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-partName" name="partName" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.partName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-partType" class="layui-form-label">零部件类型</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-partType" name="partType" autocomplete="off"
                                   class="layui-input" value="${result.partType}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-materialCode" class="layui-form-label">物料编号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-materialCode" name="materialCode" autocomplete="off"
                                   class="layui-input" value="${result.materialCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-materialName" class="layui-form-label">物料名称</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-materialName" name="materialName" autocomplete="off"
                                   class="layui-input" value="${result.materialName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-specification" class="layui-form-label">规格</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-specification" name="specification" autocomplete="off"
                                   class="layui-input" value="${result.specification}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-unit" class="layui-form-label">单位</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-unit" name="unit" autocomplete="off"
                                   class="layui-input" value="${result.unit}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-version" class="layui-form-label">版本号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-version" name="version" autocomplete="off"
                                   class="layui-input" value="${result.version!"V1.0"}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-status" class="layui-form-label sp-required">状态</label>
                        <div class="layui-input-block" id="js-status" style="width: 310px;">
                            <input type="radio" name="status" value="0" title="有效"
                                   <#if result.status == 0 || !(result??)>checked</#if>>
                            <input type="radio" name="status" value="1" title="失效"
                                   <#if result.status == 1>checked</#if>>
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
                url: "${request.contextPath}/basedata/partComponent/add-or-update",
                data: data.field
            });
            return false;
        });
    });
</script>
</body>
</html>