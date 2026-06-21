<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>班组员工管理</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <div style="margin-bottom:10px;">
            <span class="layui-badge layui-bg-blue">班组：${teamName!}</span>
            <a class="layui-btn layui-btn-sm" id="js-add-staff"><i class="layui-icon layui-icon-add-1"></i>添加员工</a>
        </div>

        <script type="text/html" id="js-table-operate">
            <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete"><i class="layui-icon layui-icon-delete"></i>移除</a>
        </script>

        <table class="layui-hide" id="js-table" lay-filter="js-table-filter"></table>
    </div>
</div>

<div id="userSelectModal" style="display:none; padding:15px;">
    <div class="layui-form-item">
        <div class="layui-inline">
            <input type="text" id="userSearchKey" placeholder="输入关键字搜索" autocomplete="off" class="layui-input">
        </div>
        <button class="layui-btn" id="userSearchBtn">搜索</button>
    </div>
    <div id="userTree" style="max-height:350px; overflow:auto;"></div>
</div>

<script>
    layui.use(['table', 'layer', 'tree'], function () {
        var table = layui.table, layer = layui.layer, tree = layui.tree, $ = layui.$;
        var teamId = '${RequestParameters.teamId!}';

        var tableIns = table.render({
            elem: '#js-table',
            url: '${request.contextPath}/basedata/teamStaff/page',
            method: 'post',
            where: {teamId: teamId},
            cols: [[
                {type: 'numbers'},
                {field: 'userName', title: '用户编码', templet: function(d){return d.userName || d.userId;}},
                {field: 'status', title: '状态', templet: function(d){return d.status==1?'正常':'禁用';}},
                {title: '操作', toolbar: '#js-table-operate', width: 100}
            ]],
            page: true,
            parseData: function(res) {
                return {
                    "code": res.code,
                    "msg": res.msg,
                    "count": res.data.total,
                    "data": res.data.records
                };
            }
        });

        $('#js-add-staff').on('click', function () {
            layer.open({
                type: 1,
                title: '用户选择',
                area: ['400px', '500px'],
                content: $('#userSelectModal'),
                btn: ['确定', '关闭'],
                success: function () {
                    loadUserTree();
                },
                yes: function (index) {
                    var checked = tree.getChecked('userTree');
                    var userIds = [];
                    collectChecked(checked, userIds);
                    if (userIds.length === 0) {
                        layer.msg('请选择用户');
                        return;
                    }
                    var successCount = 0;
                    var total = userIds.length;
                    userIds.forEach(function(uid) {
                        $.post('${request.contextPath}/basedata/teamStaff/add', {teamId: teamId, userId: uid}, function(res) {
                            successCount++;
                            if (successCount >= total) {
                                layer.msg('添加完成', {icon: 1});
                                tableIns.reload();
                                layer.close(index);
                            }
                        });
                    });
                }
            });
        });

        function loadUserTree() {
            $.get('${request.contextPath}/basedata/teamStaff/user-tree', function (res) {
                if (res.code === 0) {
                    var data = convertTreeData(res.data);
                    tree.render({
                        elem: '#userTree',
                        data: data,
                        showCheckbox: true,
                        id: 'userTree'
                    });
                }
            });
        }

        function convertTreeData(nodes) {
            for (var i = 0; i < nodes.length; i++) {
                nodes[i].title = nodes[i].name;
                if (nodes[i].children && nodes[i].children.length > 0) {
                    convertTreeData(nodes[i].children);
                }
            }
            return nodes;
        }

        function collectChecked(nodes, result) {
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].isUser) {
                    result.push(nodes[i].id);
                }
                if (nodes[i].children && nodes[i].children.length > 0) {
                    collectChecked(nodes[i].children, result);
                }
            }
        }

        table.on('tool(js-table-filter)', function (obj) {
            var data = obj.data;
            if (obj.event === 'delete') {
                layer.confirm('确定移除该员工吗？', function (index) {
                    $.post('${request.contextPath}/basedata/teamStaff/delete', {id: data.id}, function () {
                        tableIns.reload();
                        layer.close(index);
                    });
                });
            }
        });
    });
</script>
</body>
</html>
