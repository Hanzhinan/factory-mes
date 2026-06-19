<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>库房库位管理</title>
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
                    <label class="layui-form-label">库房编号</label>
                    <div class="layui-input-inline">
                        <input type="text" name="warehouseCode" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">库房名称</label>
                    <div class="layui-input-inline">
                        <input type="text" name="warehouseName" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <label class="layui-form-label">库位编号</label>
                    <div class="layui-input-inline">
                        <input type="text" name="locationCode" autocomplete="off" class="layui-input">
                    </div>
                </div>
                <div class="layui-inline">
                    <a class="layui-btn" lay-submit lay-filter="js-search-filter"><i
                                class="layui-icon layui-icon-search layuiadmin-button-btn"></i></a>
                </div>
            </div>
        </form>

        <table class="layui-hide" id="js-record-table" lay-filter="js-record-table-filter"></table>
    </div>
</div>

<script type="text/html" id="js-record-table-toolbar-top">
    <div class="layui-btn-container">
        <button class="layui-btn layui-btn-danger layui-btn-sm" lay-event="deleteBatch"><i
                    class="layui-icon">&#xe640;</i>批量删除
        </button>
        <button class="layui-btn layui-btn-sm" lay-event="add"><i class="layui-icon">&#xe61f;</i>添加</button>
    </div>
</script>

<script type="text/html" id="js-record-table-toolbar-right">
    <a class="layui-btn layui-btn-xs" lay-event="edit"><i class="layui-icon layui-icon-edit"></i>编辑</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="delete"><i class="layui-icon layui-icon-delete"></i>删除</a>
</script>

<script>
    layui.use(['form', 'table', 'spLayer', 'spTable'], function () {
        var form = layui.form,
            table = layui.table,
            spLayer = layui.spLayer,
            spTable = layui.spTable;

        var tableIns = spTable.render({
            url: '${request.contextPath}/basedata/warehouseLocation/page',
            cols: [
                [{
                    type: 'checkbox'
                }, {
                    field: 'warehouseCode', title: '库房编号'
                }, {
                    field: 'warehouseName', title: '库房名称'
                }, {
                    field: 'warehouseType', title: '库房类型'
                }, {
                    field: 'locationCode', title: '库位编号'
                }, {
                    field: 'locationName', title: '库位名称'
                }, {
                    field: 'xCoordinate', title: 'X坐标'
                }, {
                    field: 'yCoordinate', title: 'Y坐标'
                }, {
                    field: 'zCoordinate', title: 'Z坐标'
                }, {
                    field: 'capacity', title: '容量'
                }, {
                    field: 'currentQty', title: '当前数量'
                }, {
                    field: 'status', title: '状态', templet: function (d) {
                        var statusMap = {0: '可用', 1: '占用', 2: '禁用'};
                        return statusMap[d.status] || '未知';
                    }
                }, {
                    fixed: 'right',
                    field: 'operate',
                    title: '操作',
                    toolbar: '#js-record-table-toolbar-right',
                    unresize: true,
                    width: 150
                }]
            ],
            done: function (res, curr, count) {
            }
        });

        $(function () {
            form.render();
        });

        form.on('submit(js-search-filter)', function (data) {
            tableIns.reload({
                where: data.field,
                page: {
                    curr: 1
                }
            });
            return false;
        });

        table.on('toolbar(js-record-table-filter)', function (obj) {
            var checkStatus = table.checkStatus(obj.config.id);

            if (obj.event === 'deleteBatch') {
                var checkStatus = table.checkStatus('record-table'),
                    data = checkStatus.data;
                if (data.length > 0) {
                    layer.confirm('确认要删除吗？', function (index) {
                    });
                } else {
                    layer.msg("请先选择需要删除的数据！");
                }
            }

            if (obj.event === 'add') {
                var index = spLayer.open({
                    title: '添加库房库位',
                    area: ['90%', '90%'],
                    content: '${request.contextPath}/basedata/warehouseLocation/add-or-update-ui'
                });
            }
        });

        table.on('tool(js-record-table-filter)', function (obj) {
            var data = obj.data;

            if (obj.event === 'edit') {
                spLayer.open({
                    title: '编辑库房库位',
                    area: ['90%', '90%'],
                    spWhere: {id: data.id},
                    content: '${request.contextPath}/basedata/warehouseLocation/add-or-update-ui'
                });
            }

            if (obj.event === 'delete') {
                layer.confirm('确认要删除吗？', function (index) {
                    spUtil.ajax({
                        url: '${request.contextPath}/basedata/warehouseLocation/delete',
                        async: false,
                        type: 'POST',
                        showLoading: true,
                        serializable: false,
                        data: {
                            id: data.id
                        },
                        success: function (data) {
                            tableIns.reload();
                            layer.close(index);
                        },
                        error: function () {
                        }
                    });
                });
            }
        });
    });
</script>
</body>
</html>