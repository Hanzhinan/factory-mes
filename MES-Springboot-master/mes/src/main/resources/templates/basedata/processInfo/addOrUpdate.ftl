<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>工序信息管理</title>
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
                        <label for="js-processCode" class="layui-form-label sp-required">工序编号</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-processCode" name="processCode" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.processCode}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-processName" class="layui-form-label sp-required">工序名称</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-processName" name="processName" lay-verify="required" autocomplete="off"
                                   class="layui-input" value="${result.processName}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-processDesc" class="layui-form-label">工序描述</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-processDesc" name="processDesc" autocomplete="off"
                                   class="layui-input" value="${result.processDesc}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-deviceId" class="layui-form-label">设备名称</label>
                        <div class="layui-input-inline" style="width: 310px;">
                            <select id="js-deviceId" name="deviceId" lay-search="">
                                <option value="">请选择设备</option>
                            </select>
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-standardTime" class="layui-form-label">标准工时</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-standardTime" name="standardTime" autocomplete="off"
                                   class="layui-input" value="${result.standardTime}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-sequence" class="layui-form-label">工序顺序</label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-sequence" name="sequence" autocomplete="off"
                                   class="layui-input" value="${result.sequence!"0"}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-status" class="layui-form-label sp-required">状态</label>
                        <div class="layui-input-block" id="js-status" style="width: 310px;">
                            <input type="radio" name="status" value="0" title="启用"
                                   <#if result.status == 0 || !(result??)>checked</#if>>
                            <input type="radio" name="status" value="1" title="停用"
                                   <#if result.status == 1>checked</#if>>
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

        function loadDeviceList() {
            $.ajax({
                type: "POST",
                url: "${request.contextPath}/basedata/groupDevice/page",
                data: {pageNo: 1, pageSize: 1000},
                success: function (res) {
                    if (res.code === 0 && res.data.records) {
                        var deviceList = res.data.records;
                        var $select = $('#js-deviceId');
                        $.each(deviceList, function (i, device) {
                            var selected = '';
                            if ('${result.deviceId}' && device.id === '${result.deviceId}') {
                                selected = 'selected';
                            }
                            $select.append('<option value="' + device.id + '" ' + selected + '>' + device.deviceName + '</option>');
                        });
                        form.render('select');
                    }
                }
            });
        }

        loadTeamList();
        loadDeviceList();

        form.on('submit(js-submit-filter)', function (data) {
            spUtil.submitForm({
                url: "${request.contextPath}/basedata/processInfo/add-or-update",
                data: data.field
            });
            return false;
        });
    });
</script>
</body>
</html>