<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>编组设备管理</title>
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
                        <label for="js-deviceCode" class="layui-form-label sp-required">设备编号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-deviceCode" name="deviceCode" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.deviceCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-deviceName" class="layui-form-label sp-required">设备名称</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-deviceName" name="deviceName" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.deviceName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-deviceModel" class="layui-form-label">设备型号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-deviceModel" name="deviceModel" autocomplete="off"
                                   class="layui-input" value="${result.deviceModel}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-deviceType" class="layui-form-label">设备类型</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-deviceType" name="deviceType" autocomplete="off"
                                   class="layui-input" value="${result.deviceType}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-groupCode" class="layui-form-label">编组编号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-groupCode" name="groupCode" autocomplete="off"
                                   class="layui-input" value="${result.groupCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-groupName" class="layui-form-label">编组名称</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-groupName" name="groupName" autocomplete="off"
                                   class="layui-input" value="${result.groupName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-teamId" class="layui-form-label">关联班组</label>
                        <div class="layui-input-inline" style="width: 310px;">
                            <select id="js-teamId" name="teamId" lay-search="">
                                <option value="">请选择班组</option>
                            </select>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-location" class="layui-form-label">位置</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-location" name="location" autocomplete="off"
                                   class="layui-input" value="${result.location}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-capacity" class="layui-form-label">产能</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-capacity" name="capacity" autocomplete="off"
                                   class="layui-input" value="${result.capacity}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-status" class="layui-form-label sp-required">状态</label>
                        <div class="layui-input-block" id="js-status" style="width: 310px;">
                            <input type="radio" name="status" value="0" title="运行"
                                   <#if result.status == 0 || !(result??)>checked</#if>>
                            <input type="radio" name="status" value="1" title="停机"
                                   <#if result.status == 1>checked</#if>>
                            <input type="radio" name="status" value="2" title="维护"
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

        function loadTeamList() {
            $.ajax({
                type: "POST",
                url: "${request.contextPath}/basedata/team/page",
                data: {pageNo: 1, pageSize: 1000},
                success: function (res) {
                    if (res.code === 0 && res.data.records) {
                        var teamList = res.data.records;
                        var $select = $('#js-teamId');
                        $.each(teamList, function (i, team) {
                            var selected = '';
                            if ('${result.teamId}' && team.id === '${result.teamId}') {
                                selected = 'selected';
                            }
                            $select.append('<option value="' + team.id + '" ' + selected + '>' + team.teamName + '</option>');
                        });
                        form.render('select');
                    }
                }
            });
        }

        loadTeamList();

        form.on('submit(js-submit-filter)', function (data) {
            spUtil.submitForm({
                url: "${request.contextPath}/basedata/groupDevice/add-or-update",
                data: data.field
            });
            return false;
        });
    });
</script>
</body>
</html>