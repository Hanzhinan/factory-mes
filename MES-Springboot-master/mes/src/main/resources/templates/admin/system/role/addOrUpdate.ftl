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
        var originalTreeData = null;
        var authorizedMenuIds = [];

        function convertTreeData(nodes) {
            for (var i = 0; i < nodes.length; i++) {
                nodes[i].title = nodes[i].name;
                nodes[i].spread = true;
                // 强制设置 checked: false，不依赖后端返回的值
                nodes[i].checked = false;
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
                        originalTreeData = result.data.tree;
                        authorizedMenuIds = result.data.authorizedIds || [];
                        var treeData = convertTreeData(result.data.tree);
                        console.log('加载的菜单树数据:', JSON.stringify(treeData, null, 2));
                        console.log('已授权菜单ID:', authorizedMenuIds);
                        
                        treeIns = tree.render({
                            elem: '#js-menu-tree',
                            data: treeData,
                            showCheckbox: true,
                            id: 'menuTree',
                            click: function (obj) {
                            }
                        });
                        
                        // 使用 tree.setChecked 方法精确控制勾选状态
                        // 只勾选叶子节点，避免父子联动问题
                        if (authorizedMenuIds.length > 0) {
                            console.log('设置已授权菜单:', authorizedMenuIds);
                            tree.setChecked('menuTree', authorizedMenuIds);
                        }
                    }
                }
            });
        }

        loadMenuTree();

        function findLeafNodes(nodes, leafIds) {
            for (var i = 0; i < nodes.length; i++) {
                if (!nodes[i].children || nodes[i].children.length === 0) {
                    leafIds.push(nodes[i].id);
                } else {
                    findLeafNodes(nodes[i].children, leafIds);
                }
            }
        }

        function findCheckedLeafIds(nodes, checkedIds) {
            var result = [];
            for (var i = 0; i < nodes.length; i++) {
                var node = nodes[i];
                var nodeId = node.id;
                // 如果是叶子节点且被勾选
                if ((!node.children || node.children.length === 0) && checkedIds.indexOf(nodeId) !== -1) {
                    result.push(nodeId);
                }
                // 递归处理子节点
                if (node.children && node.children.length > 0) {
                    var childResults = findCheckedLeafIds(node.children, checkedIds);
                    result = result.concat(childResults);
                }
            }
            return result;
        }

        form.on('submit(js-submit-filter)', function (data) {
            // 获取所有被勾选的节点
            var checkedData = tree.getChecked('menuTree');
            console.log('getChecked返回的数据:', JSON.stringify(checkedData, null, 2));
            
            // 只提取被勾选的叶子节点ID（从原始树数据中找）
            var leafIds = [];
            function extractLeafIds(nodes) {
                for (var i = 0; i < nodes.length; i++) {
                    // 只有叶子节点才保存
                    if (!nodes[i].children || nodes[i].children.length === 0) {
                        var nodeId = nodes[i].id || (nodes[i].data && nodes[i].data.id);
                        if (nodeId) {
                            leafIds.push(nodeId);
                        }
                    }
                    // 递归处理子节点
                    if (nodes[i].children && nodes[i].children.length > 0) {
                        extractLeafIds(nodes[i].children);
                    }
                }
            }
            extractLeafIds(checkedData);
            console.log('最终保存的叶子节点ID:', leafIds);

            spUtil.submitForm({
                url: "${request.contextPath}/admin/sys/role/add-or-update",
                data: data.field,
                success: function (res) {
                    var currentRoleId = res.data || roleId;
                    $.ajax({
                        type: "POST",
                        url: "${request.contextPath}/admin/sys/role/save-permissions?roleId=" + currentRoleId,
                        data: JSON.stringify(leafIds),
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
