<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>班组管理</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form id="js-search-form" class="layui-form" lay-filter="js-q-form-filter">
            <div class="layui-form-item">
                <div class="layui-inline">
                    <label class="layui-form-label">班组代码</label>
                    <div class="layui-input-inline">
                        <input type="text" name="teamCode" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">班组名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="teamName" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <a class="layui-btn" lay-submit lay-filter="js-search-filter"><i class="layui-icon layui-icon-search layuiadmin-button-btn"></i></a>
                </div>
            </div>
        </form>

        <script type="text/html" id="js-toolbar">
            <a class="layui-btn layui-btn-sm" lay-event="add"><i class="layui-icon layui-icon-add-1"></i>新增班组</a>
        </script>

        <script type="text/html" id="js-table-operate">
            <a class="layui-btn layui-btn-xs" lay-event="edit"><i class="layui-icon layui-icon-edit"></i>编辑</a>
            <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete"><i class="layui-icon layui-icon-delete"></i>删除</a>
            <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="staff"><i class="layui-icon layui-icon-group"></i>班组员工</a>
        </script>

        <table class="layui-hide" id="js-table" lay-filter="js-table-filter"></table>
    </div>
</div>
<script>
    layui.use(['table', 'layer', 'form'], function () {
        var table = layui.table, layer = layui.layer, form = layui.form, $ = layui.$;

        var tableIns = table.render({
            elem: '#js-table',
            url: '${request.contextPath}/basedata/team/page',
            method: 'post',
            toolbar: '#js-toolbar',
            cols: [[
                {type: 'numbers'},
                {field: 'teamCode', title: '班组代码'},
                {field: 'teamName', title: '班组名称'},
                {field: 'teamDescr', title: '班组描述'},
                {field: 'status', title: '状态', templet: function(d){return d.status==1?'正常':'禁用';}},
                {title: '操作', toolbar: '#js-table-operate', width: 280}
            ]],
            page: true,
            id: 'teamTable',
            parseData: function(res) {
                return {
                    "code": res.code,
                    "msg": res.msg,
                    "count": res.data.total,
                    "data": res.data.records
                };
            }
        });

        table.on('toolbar(js-table-filter)', function (obj) {
            if (obj.event === 'add') {
                layer.open({
                    type: 2,
                    title: '新增班组',
                    area: ['600px', '400px'],
                    content: '${request.contextPath}/basedata/team/add-or-update-ui'
                });
            }
        });

        table.on('tool(js-table-filter)', function (obj) {
            var data = obj.data;
            if (obj.event === 'edit') {
                layer.open({
                    type: 2,
                    title: '编辑班组',
                    area: ['600px', '400px'],
                    content: '${request.contextPath}/basedata/team/add-or-update-ui?id=' + data.id
                });
            } else if (obj.event === 'delete') {
                layer.confirm('确定删除吗？', function (index) {
                    $.post('${request.contextPath}/basedata/team/delete', {id: data.id}, function () {
                        tableIns.reload();
                        layer.close(index);
                    });
                });
            } else if (obj.event === 'staff') {
                layer.open({
                    type: 2,
                    title: '班组员工管理 - ' + data.teamName,
                    area: ['900px', '600px'],
                    content: '${request.contextPath}/basedata/teamStaff/list-ui?teamId=' + data.id + '&teamName=' + encodeURIComponent(data.teamName)
                });
            }
        });

        form.on('submit(js-search-filter)', function (data) {
            tableIns.reload({where: data.field, page: {curr: 1}});
            return false;
        });
    });
</script>
</body>
</html>
