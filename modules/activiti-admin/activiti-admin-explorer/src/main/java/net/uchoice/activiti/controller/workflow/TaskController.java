package net.uchoice.activiti.controller.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.activiti.entity.ActDelegate;
import net.uchoice.activiti.service.ActDelegateService;
import net.uchoice.activiti.service.ActGrantService;
import net.uchoice.activiti.service.WorkflowService;
import net.uchoice.common.entity.JsonResult;
import net.uchoice.common.persistence.Page;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.extras.springsecurity4.auth.AuthUtils;

import com.google.common.collect.Maps;

@Controller
@RequestMapping("/workflow/task")
public class TaskController {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskController.class);

	@Autowired
	private TaskService taskService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private FormService formService;

	@Autowired
	private IdentityService identityService;

	@Autowired
	private ActGrantService actGrantService;

	@Autowired
	private ActDelegateService actDelegateService;

	@Autowired
	private WorkflowService workflowService;
	/**
	 * 所有任务列表
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/all/list")
	public ModelAndView finishedTasks(String finish, String delegate,
			HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/activiti/workflow/allTaskList");
		Page<HistoricTaskInstance> page = new Page<HistoricTaskInstance>(
				request, response);
		User user = (User) AuthUtils.getAuthenticationObject().getPrincipal();
		HistoricTaskInstanceQuery query = historyService
				.createHistoricTaskInstanceQuery()
				.taskInvolvedUser(user.getId()).orderByTaskCreateTime().desc();
		if ("1".equals(finish)) { // 已办理
			query.finished();
		} else if ("0".equals(finish)) { // 未办理
			query.unfinished();
		}
		if ("1".equals(delegate)) { // 我代办的
			query.taskAssignee(user.getId()).taskOwnerLike("%");
		} else if ("2".equals(delegate)) { // 被代办的
			query.taskOwner(user.getId());
		} else { // 其他
			query.taskAssignee(user.getId());
		}
		page.setCount(query.count());
		List<HistoricTaskInstance> list = query.listPage(page.getFirstResult(),
				page.getMaxResults());
		page.setResult(list);
		mav.addObject("delegate", delegate);
		mav.addObject("finish", finish);
		mav.addObject("page", page);
		return mav;
	}

	/**
	 * 待办任务列表
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/todo/list")
	public ModelAndView taskList(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/activiti/workflow/todoTaskList");
		User user = (User) AuthUtils.getAuthenticationObject().getPrincipal();

		Page<Task> page = new Page<Task>(request, response);
		TaskQuery taskQuery = taskService.createTaskQuery().active()
				.taskCandidateOrAssigned(user.getId());
		taskQuery.orderByTaskCreateTime().desc();
		page.setCount(taskQuery.count());
		List<Task> tasks = taskQuery.listPage(page.getFirstResult(),
				page.getMaxResults());
		List<ActDelegate> delegates = actDelegateService.findListByTaksIds(
				tasks.stream().map(o -> {
					return o.getId();
				}).collect(Collectors.toList()), user.getId());
		Map<String, ActDelegate> delegateMap = Maps.newHashMap();
		for (ActDelegate actDelegate : delegates) {
			delegateMap.put(actDelegate.getTaskId(), actDelegate);
		}
		mav.addObject("delegateMap", delegateMap);
		page.setResult(tasks);
		mav.addObject("page", page);
		return mav;
	}

	/**
	 * 读取Task的表单
	 */
	@RequestMapping(value = "/{taskId}/form")
	public String findTaskForm(@PathVariable("taskId") String taskId,
			Model model) throws Exception {
		Object renderedTaskForm = formService.getRenderedTaskForm(taskId);
		model.addAttribute("form", renderedTaskForm);
		model.addAttribute("taskId", taskId);
		return "/activiti/workflow/handleForm";
	}

	/**
	 * 签收任务
	 */
	@RequestMapping(value = "/claim/{taskId}")
	public String claim(@PathVariable("taskId") String taskId,
			RedirectAttributes redirectAttributes) {
		workflowService.claimTask(taskId);
		redirectAttributes.addFlashAttribute("message", "任务已签收");
		return "redirect:/workflow/task/todo/list";
	}

	/**
	 * 办理任务，提交task的并保存form
	 */
	@RequestMapping(value = "/complete/{taskId}")
	@ResponseBody
	public JsonResult completeTask(@PathVariable("taskId") String taskId,
			HttpServletRequest request) {
		Map<String, String> formProperties = new HashMap<String, String>();
		// 从request中读取参数然后转换
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
		for (Entry<String, String[]> entry : entrySet) {
			String key = entry.getKey();
			/*
			 * 参数结构：fq_reason，用_分割 fp的意思是form paremeter 最后一个是属性名称
			 */
			if (StringUtils.defaultString(key).startsWith("fp_")) {
				String[] paramSplit = key.split("_");
				formProperties.put(paramSplit[1], entry.getValue()[0]);
			}
		}

		logger.debug("start form parameters: {}", formProperties);

		User user = (User) AuthUtils.getAuthenticationObject().getPrincipal();
		try {
			identityService.setAuthenticatedUserId(user.getId());
			formService.submitTaskFormData(taskId, formProperties);
		} finally {
			identityService.setAuthenticatedUserId(null);
		}
		return JsonResult.success("任务已办理");
	}

}
