<#include "/default/inc/layout.ftl"/>
<@layout "编辑文章">

<form id="submitForm" class="form" action="${base}/post/submit" method="post" enctype="multipart/form-data">
    <input type="hidden" name="status" value="${view.status!0}"/>
    <input type="hidden" name="editor" value="${editor!'tinymce'}"/>
    <div class="row">
        <div class="col-xs-12 col-md-8">
            <div id="message"></div>
            <#if view??>
                <input type="hidden" name="id" value="${view.id}"/>
                <input type="hidden" name="authorId" value="${view.authorId}"/>
            </#if>
            <input type="hidden" id="thumbnail" name="thumbnail" value="${view.thumbnail}"/>

            <div class="form-group">
                <select class="form-control" name="channelId" required>
                    <option value="">请选择栏目</option>
                    <#list channels as row>
                        <option value="${row.id}" <#if (view.channelId == row.id)> selected </#if>>${row.name}</option>
                    </#list>
                </select>
            </div>
            <div class="form-group">
                <input type="text" class="form-control" name="title" maxlength="128" value="${view.title}" placeholder="请输入标题" required>
            </div>

            <div class="form-group">
                <#include "/default/channel/editor/${editor}.ftl"/>
            </div>
        </div>
        <div class="col-xs-12 col-md-4">


            <div class="panel panel-default corner-radius help-box">
                <div class="panel-heading">
                    <h3 class="panel-title">标签(用逗号或空格分隔)</h3>
                </div>
                <div class="panel-body">
                    <input type="text" id="tags" name="tags" class="form-control" value="${view.tags}" placeholder="添加相关标签，逗号分隔 (最多4个)">
                </div>
            </div>

<!--            <div class="panel panel-default corner-radius help-box">
                <div class="thumbnail-box">
                    <div class="convent_choice" id="thumbnail_image" >
                        <a href="<#if view.thumbnail?? && view.thumbnail?length gt 0> <@resource src=view.thumbnail/>"> </#if>1</a>
                        <a href="/storage/thumbnails/_signature/11E8CHORL75B519RHFC1D53SIN.jpg">1443</a>
                        <a href="/storage/admin/王铭晨账户操作明细.xlsx" download="王铭晨账户操作明细.xlsx">test</a>
                        <div class="upload-btn">
                            <label>
                                <span>点击选择一张图片</span>
                                <input id="upload_btn" type="file" name="file" title="点击添加图片">
                            </label>
                        </div>
                    </div>
                </div>
            </div>
-->


                <div class="file-loading">
                    <input id="fileinput" class="file" type="file" data-preview-file-type="any" data-theme="fas">
                </div>



        </div>
    </div>
    <div class="col-xs-12 col-md-12">
            <div class="form-group">
                <div class="text-center">
                    <button type="button" data-status="0" class="btn btn-primary" event="post_submit" style="padding-left: 30px; padding-right: 30px;">发布</button>
                </div>
            </div>
    </div>
</form>
<!-- /form-actions -->
<script type="text/javascript">
seajs.use('post', function (post) {
	post.init();
});

var thumbnail = $("#thumbnail").val();
var filename = thumbnail.split("/").pop().split(".")[0];
var postfix = thumbnail.split("/").pop().split(".")[1];

var initialPreview = [];
var initialPreviewConfig = [];

if("" !== thumbnail){
    initialPreview.push(thumbnail);
    initialPreviewConfig.push({
        caption: filename+"."+postfix, type: postfix, key: thumbnail
    })
}

$("#fileinput").fileinput({
    language:'en',                                          // 多语言设置，需要引入local中相应的js，例如locales/zh.js
    uploadUrl: "/post/upload",
    deleteUrl: "/post/deleteFile",
    showClose: false,
    initialPreview: initialPreview,
    initialPreviewAsData:true,
    initialPreviewConfig: initialPreviewConfig,
    previewFileIconSettings: {
        'doc': '<i class="fa fa-file-word-o text-primary"></i>',
        'docx': '<i class="fa fa-file-word-o text-primary"></i>',
        'xls': '<i class="fa fa-file-excel-o text-success"></i>',
        'xlsx': '<i class="fa fa-file-excel-o text-success"></i>',
        'ppt': '<i class="fa fa-file-powerpoint-o text-danger"></i>',
        'jpg': '<i class="fa fa-file-photo-o text-warning"></i>',
        'pdf': '<i class="fa fa-file-pdf-o text-danger"></i>',
        'zip': '<i class="fa fa-file-archive-o text-muted"></i>',
    }
});


$("#fileinput").on("fileuploaded", function(event, data, previewId, index) {
    var path = data.response.path;
    setTimeout("closeUpladLayer()",2000)
    $("#thumbnail").val(path);
});
// 上传失败回调
$('#fileinput').on('fileerror', function(event, data, msg) {
    tokenTimeOut(data);
});

//新上传移除
$('#fileinput').on('filesuccessremove', function(event, key) {
    console.log('remove file Key = ' + $("#thumbnail").val());
    $.ajax({
        type: "POST",
        url: "/post/deleteFile",
        contentType: "application/x-www-form-urlencoded",
        data:{"key": $("#thumbnail").val()},
        dataType: "json",
        success:function (message) {
            $("#thumbnail").val(message.path);
        },
        error:function (message) {
            alert("提交失败"+JSON.stringify(message));
        }
    });
});

//已有文件删除
$('#fileinput').on('filedeleted', function(event, key) {
    console.log('delete file Key = ' + $("#thumbnail").val());
    $.ajax({
        type: "POST",
        url: "/post/deleteFile",
        contentType: "application/x-www-form-urlencoded",
        data:{"key": $("#thumbnail").val()},
        dataType: "json",
        success:function (message) {
            $("#thumbnail").val(message.path);
        },
        error:function (message) {

        }
    });
});


</script>
</@layout>
