package net.uchoice.activiti.controller.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.common.entity.JsonResult;
import net.uchoice.common.persistence.Page;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.extras.springsecurity4.auth.AuthUtils;

@Controller
@RequestMapping("/workflow/process")
public class ProcessController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private FormService formService;

	@Autowired
	private IdentityService identityService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private RuntimeService runtimeService;

	/**
	 * 动态form流程列表
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = { "/useable/list" })
	public ModelAndView processDefinitionList(Model model,
			HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView(
				"/activiti/workflow/useableProcessList");
		Page<ProcessDefinition> page = new Page<ProcessDefinition>(request,
				response);
		ProcessDefinitionQuery query = repositoryService
				.createProcessDefinitionQuery().active().orderByDeploymentId()
				.desc();
		page.setCount(query.count());
		List<ProcessDefinition> list = query.listPage(page.getFirstResult(),
				page.getMaxResults());
		page.setResult(list);
		mav.addObject("page", page);
		return mav;
	}

	/**
	 * 初始化启动流程，读取启动流程的表单内容来渲染start form
	 */
	@RequestMapping(value = "/{processDefinitionId}/start-form")
	public String findStartForm(
			@PathVariable("processDefinitionId") String processDefinitionId,
			String pName, Model model) throws Exception {
		// 根据流程定义ID读取外置表单
		Object startForm = formService
				.getRenderedStartForm(processDefinitionId);
		model.addAttribute("form", startForm);
		model.addAttribute("processDefinitionId", processDefinitionId);
		return "/activiti/workflow/startForm";
	}

	/**
	 * 读取启动流程的表单字段
	 */
	@RequestMapping(value = "/start/{processDefinitionId}")
	@ResponseBody
	public JsonResult submitStartFormAndStartProcessInstance(
			@PathVariable("processDefinitionId") String processDefinitionId,
			HttpServletRequest request) {
		Map<String, String> formProperties = new HashMap<String, String>();

		// 从request中读取参数然后转换
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
		for (Entry<String, String[]> entry : entrySet) {
			String key = entry.getKey();

			// fp_的意思是form paremeter
			if (StringUtils.defaultString(key).startsWith("fp_")) {
				formProperties.put(key.split("_")[1], entry.getValue()[0]);
			}
		}

		logger.debug("start form parameters: {}", formProperties);
		User user = (User) AuthUtils.getAuthenticationObject().getPrincipal();
		try {
			identityService.setAuthenticatedUserId(user.getId());
			ProcessInstance processInstance = formService.submitStartFormData(
					processDefinitionId, formProperties);
			ProcessDefinition definition = repositoryService
					.createProcessDefinitionQuery()
					.processDefinitionId(processDefinitionId).singleResult();
			if (definition != null) {
				runtimeService.setProcessInstanceName(processInstance.getId(),
						"[" + user.getId() + "] " + definition.getName());
			}
			logger.debug("start a processinstance: {}", processInstance);
		} finally {
			identityService.setAuthenticatedUserId(null);
		}
		return JsonResult.success("启动成功");
	}

	/**
	 * 历史流程实例
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/history/list")
	public ModelAndView running(Model model, HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mav = new ModelAndView(
				"/activiti/workflow/historyProcessList");
		Page<HistoricProcessInstance> page = new Page<HistoricProcessInstance>(
				request, response);
		User user = (User) AuthUtils.getAuthenticationObject().getPrincipal();
		HistoricProcessInstanceQuery query = historyService
				.createHistoricProcessInstanceQuery().startedBy(user.getId())
				.orderByProcessInstanceEndTime().desc();
		page.setCount(query.count());
		List<HistoricProcessInstance> list = query.listPage(
				page.getFirstResult(), page.getMaxResults());
		page.setResult(list);
		mav.addObject("page", page);
		return mav;
	}

	/**
	 * 已结束的流程实例
	 *
	 * @param model
	 * @return
	 */
	/*
	 * @RequestMapping(value = "/finished/list") public ModelAndView
	 * finished(Model model, HttpServletRequest request, HttpServletResponse
	 * response) { ModelAndView mav = new
	 * ModelAndView("/activiti/workflow/finishedProcessList");
	 * Page<HistoricProcessInstance> page = new Page<HistoricProcessInstance>(
	 * request, response); User user = (User)
	 * AuthUtils.getAuthenticationObject().getPrincipal();
	 * HistoricProcessInstanceQuery query = historyService
	 * .createHistoricProcessInstanceQuery() .involvedUser(user.getId())
	 * .orderByProcessInstanceEndTime().desc(); page.setCount(query.count());
	 * List<HistoricProcessInstance> list = query.listPage(
	 * page.getFirstResult(), page.getMaxResults()); page.setResult(list);
	 * mav.addObject("page", page); return mav; }
	 */

}
