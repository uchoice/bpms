function graphTrace() {
	var url = ctxPath + "/diagram-viewer/index.html?processDefinitionId="
			+ $(this).attr("pdid") + "&processInstanceId="
			+ $(this).attr("pid");
	top.$.jBox("<iframe src='" + url + "' width='100%' height='100%' frameborder='0'/>", {
		title : "流程跟踪",
		width : 1200,
		height : 560,
		buttons : {
			'关闭' : true
		}
	});
}