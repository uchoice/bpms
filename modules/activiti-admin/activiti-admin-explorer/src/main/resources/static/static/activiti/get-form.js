/**
 * 流程管理Javascript
 * 
 * @author HenryYan
 */
$(function() {

	$("a.startup-process").click(startProcess);
	
	$("a.handle").click(handleProcess);
	
	$("a.tenant").click(tenantForm);
	
});

function handleProcess(){
	var $ele = $(this);
	// 当前节点的中文名称
	var tname = $(this).attr('tname');
	// 任务ID
	var taskId = $(this).attr('tid');
	
    top.$.jBox.open("iframe:" + ctxPath + "/workflow/task/" + taskId + "/form", "办理任务 -> " + tname, 700, 500, {
		ajaxData:{},buttons:{"办理":"ok", "关闭":true}, submit:function(v, h, f){
			if(v == "ok"){
				var form = $(h.find("iframe")[0].contentWindow.document).find("form");
				if(form.valid()){
					top.$.jBox.tip("任务处理中，请稍候...", 'loading',{persistent: true});
					$.ajax({
					    url : form.attr("action"),
					    data : form.serialize(),
					    type : 'POST',
					    dataType : 'json',
					    success : function(res){
					    	if(res && res != null){
					    		if(res.status == 1){
					    			top.$.jBox.tip(res.result, 'info'); 
					    		} else {
					    			top.$.jBox.tip(res.result, 'error'); 
					    		}
					    	} else {
					    		top.$.jBox.tip(res, 'error'); 
					    	}
					    	window.location.reload();
					    },
					    error : function(error){
					    	top.$.jBox.tip("服务器内部错误！" + error.responseText, 'error'); 
					    }
					});
				}
				return true;
			}
		}, loaded:function(h){
			$(".jbox-content", top.document).css("overflow-y","hidden");
		}
	});
}

function startProcess(){
	var pId = $(this).attr("pid");
	var pname = $(this).attr("pname");
	top.$.jBox.open("iframe:" + ctxPath + "/workflow/process/"+ pId + "/start-form", "启动流程 -> " + pname, 700, 500, {
		ajaxData:{},buttons:{"启动":"ok", "关闭":true}, submit:function(v, h, f){
			if(v == "ok"){
				var form = $(h.find("iframe")[0].contentWindow.document).find("form");
				if(form.valid()){
					top.$.jBox.tip("流程启动中，请稍候...", 'loading',{persistent: true});
					$.ajax({
					    url : form.attr("action"),
					    data : form.serialize(),
					    type : 'POST',
					    dataType : 'json',
					    success : function(res){
					    	if(res && res != null){
					    		if(res.status == 1){
					    			top.$.jBox.tip(res.result, 'info'); 
					    		} else {
					    			top.$.jBox.tip(res.result, 'error'); 
					    		}
					    	} else {
					    		top.$.jBox.tip(res, 'error'); 
					    	}
					    },
					    error : function(error){
					    	top.$.jBox.tip("服务器内部错误！" + error.responseText, 'error'); 
					    }
					});
				}
				return true;
			}
		}, loaded:function(h){
			$(".jbox-content", top.document).css("overflow-y","hidden");
		}
	});
}

function tenantForm(){
	var $ele = $(this);
	// 流程名称
	var pname = $(this).attr('pname');
	// 流程ID
	var pId = $(this).attr('pid');
	
    top.$.jBox.open("iframe:" + ctxPath + "/workflow/process/" + pId + "/tenant-form", "流程总览---" + pname, 700, 500, {
		buttons:{"关闭":true}, loaded:function(h){
			$(".jbox-content", top.document).css("overflow-y","hidden");
		}
	});
}