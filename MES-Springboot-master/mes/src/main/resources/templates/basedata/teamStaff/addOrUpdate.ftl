<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>班组员工管理</title>
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
                        <label for="js-staffCode" class="layui-form-label sp-required">员工编号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-staffCode" name="staffCode" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.staffCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-staffName" class="layui-form-label sp-required">员工姓名</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-staffName" name="staffName" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.staffName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-teamCode" class="layui-form-label">班组编号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-teamCode" name="teamCode" autocomplete="off"
                                   class="layui-input" value="${result.teamCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-teamName" class="layui-form-label">班组名称</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-teamName" name="teamName" autocomplete="off"
                                   class="layui-input" value="${result.teamName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-departmentName" class="layui-form-label">部门名称</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-departmentName" name="departmentName" autocomplete="off"
                                   class="layui-input" value="${result.departmentName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-position" class="layui-form-label">职位</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-position" name="position" autocomplete="off"
                                   class="layui-input" value="${result.position}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-phone" class="layui-form-label">联系电话</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-phone" name="phone" autocomplete="off"
                                   class="layui-input" value="${result.phone}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-email" class="layui-form-label">邮箱</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-email" name="email" autocomplete="off"
                                   class="layui-input" value="${result.email}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-status" class="layui-form-label sp-required">状态</label>
                        <div class="layui-input-block" id="js-status" style="width: 310px;">
                            <input type="radio" name="status" value="0" title="在职"
                                   <#if result.status == 0 || !(result??)>checked</#if>>
                            <input type="radio" name="status" value="1" title="离职"
                                   <#if result.status == 1>checked</#if>>
                            <input type="radio" name="status" value="2" title="休假"
                                   <#if result.status == 2>checked</#if>>
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
                url: "${request.contextPath}/basedata/teamStaff/add-or-update",
                data: data.field
            });
            return false;
        });
    });
</script>
</body>
</html>