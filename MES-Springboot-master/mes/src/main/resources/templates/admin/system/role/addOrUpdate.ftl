<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>添加/编辑角色</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form class="layui-form splayui-form">
            <div class="layui-row">
                <div class="layui-col-xs6 layui-col-sm6 layui-col-md6">
                    <div class="layui-form-item">
                        <label for="js-name" class="layui-form-label sp-required">角色名称
                        </label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-name" name="name" lay-verify="required" autocomplete="off" class="layui-input" value="${result.name}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-code" class="layui-form-label sp-required">角色编码
                        </label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-code" name="code" lay-verify="required" autocomplete="off" class="layui-input" value="${result.code}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-descr" class="layui-form-label sp-required">描述
                        </label>
                        <div class="layui-input-inline">
                            <input type="text" id="js-descr" name="descr" lay-verify="required" autocomplete="off" class="layui-input" value="${result.descr}">
                        </div>
                    </div>
                    <div class="layui-form-item">
                        <label for="js-is-deleted" class="layui-form-label sp-required">状态
                        </label>
                        <div class="layui-input-block" id="js-is-deleted" style="width: 310px;">
                            <input type="radio" name="deleted" value="0" title="正常" <#if result.deleted == "0" || !(result??)>checked</#if>>
                            <input type="radio" name="deleted" value="1" title="已删除" <#if result.deleted == "1">checked</#if>>
                            <input type="radio" name="deleted" value="2" title="已禁用" <#if result.deleted == "2">checked</#if>>
                        </div>
                    </div>
                </div>

                <div class="layui-col-xs6 layui-col-sm6 layui-col-md6">
                    <div class="layui-form-item">
                        <label class="layui-form-label sp-required">菜单授权
                        </label>
                        <div class="layui-input-block" style="width: 310px;">
                            <div id="js-menu-tree" style="max-height: 400px; overflow-y: auto;"></div>
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
    layui.use(['form', 'util', 'tree'], function () {
        var form = layui.form,
            util = layui.util,
            tree = layui.tree;

        var roleId = '${result.id}';
        var treeIns;

        function convertTreeData(nodes) {
            for (var i = 0; i < nodes.length; i++) {
                nodes[i].title = nodes[i].name;
                if (nodes[i].children && nodes[i].children.length > 0) {
                    convertTreeData(nodes[i].children);
                }
            }
            return nodes;
        }

        function loadMenuTree() {
            $.ajax({
                type: "GET",
                url: "${request.contextPath}/admin/sys/role/menu-tree",
                data: {roleId: roleId},
                success: function (result) {
                    if (result.code === 0) {
                        var treeData = convertTreeData(result.data);
                        treeIns = tree.render({
                            elem: '#js-menu-tree',
                            data: treeData,
                            showCheckbox: true,
                            id: 'menuTree',
                            click: function (obj) {
                            }
                        });
                    }
                }
            });
        }

        loadMenuTree();

        form.on('submit(js-submit-filter)', function (data) {
            var menuIds = tree.getChecked('menuTree');
            var checkedIds = [];
            function collectIds(nodes) {
                for (var i = 0; i < nodes.length; i++) {
                    checkedIds.push(nodes[i].id);
                    if (nodes[i].children && nodes[i].children.length > 0) {
                        collectIds(nodes[i].children);
                    }
                }
            }
            collectIds(menuIds);

            spUtil.submitForm({
                url: "${request.contextPath}/admin/sys/role/add-or-update",
                data: data.field,
                success: function (res) {
                    var currentRoleId = res.data || roleId;
                    $.ajax({
                        type: "POST",
                        url: "${request.contextPath}/admin/sys/role/save-permissions?roleId=" + currentRoleId,
                        data: JSON.stringify(checkedIds),
                        contentType: "application/json",
                        dataType: "json",
                        success: function (permResult) {
                            if (permResult.code === 0) {
                                layer.msg('保存成功', {icon: 1});
                                setTimeout(function () {
                                    parent.layer.closeAll();
                                }, 1000);
                            } else {
                                layer.alert(permResult.msg, {icon: 2});
                            }
                        },
                        error: function () {
                            layer.alert('保存权限失败', {icon: 2});
                        }
                    });
                }
            });

            return false;
        });
    });
</script>
</body>
</html>
