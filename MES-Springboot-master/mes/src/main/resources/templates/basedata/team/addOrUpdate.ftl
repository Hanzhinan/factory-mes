<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>班组信息</title>
    <#include "${request.contextPath}/common/common.ftl">
</head>
<body>
<div class="splayui-container">
    <div class="splayui-main">
        <form id="js-form" class="layui-form" lay-filter="js-form-filter">
            <input type="hidden" name="id" value="${result.id!}">
            <div class="layui-form-item">
                <label class="layui-form-label">班组代码<span style="color:red">*</span></label>
                <div class="layui-input-block">
                    <input type="text" name="teamCode" lay-verify="required" value="${result.teamCode!}" autocomplete="off" class="layui-input">
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">班组名称<span style="color:red">*</span></label>
                <div class="layui-input-block">
                    <input type="text" name="teamName" lay-verify="required" value="${result.teamName!}" autocomplete="off" class="layui-input">
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">班组描述</label>
                <div class="layui-input-block">
                    <input type="text" name="teamDescr" value="${result.teamDescr!}" autocomplete="off" class="layui-input">
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">状态</label>
                <div class="layui-input-block">
                    <input type="radio" name="status" value="1" title="正常" <#if (result.status!1)==1>checked</#if>>
                    <input type="radio" name="status" value="0" title="禁用" <#if (result.status!1)==0>checked</#if>>
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">备注信息</label>
                <div class="layui-input-block">
                    <textarea name="remark" class="layui-textarea">${result.remark!}</textarea>
                </div>
            </div>
            <div class="layui-form-item">
                <div class="layui-input-block">
                    <button class="layui-btn" lay-submit lay-filter="js-submit-filter">保存</button>
                    <button type="reset" class="layui-btn layui-btn-primary">重置</button>
                </div>
            </div>
        </form>
    </div>
</div>
<script>
    layui.use(['form', 'layer'], function () {
        var form = layui.form, layer = layui.layer, $ = layui.$;
        form.render();

        form.on('submit(js-submit-filter)', function (data) {
            $.post('${request.contextPath}/basedata/team/add-or-update', data.field, function (res) {
                if (res.code === 0) {
                    layer.msg('保存成功', {icon: 1, time: 1000}, function () {
                        var index = parent.layer.getFrameIndex(window.name);
                        parent.layer.close(index);
                        parent.layui.table.reload('teamTable');
                    });
                } else {
                    layer.msg(res.msg || '保存失败', {icon: 2});
                }
            });
            return false;
        });
    });
</script>
</body>
</html>
