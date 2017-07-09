package net.uchoice.activiti.controller.management;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.common.persistence.Page;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/management/history")
public class HistoryController {

	@Autowired
	private HistoryService historyService;

	/**
	 * 已结束的流程实例
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/process/list")
	public ModelAndView finishedProcessList(Model model,
			HttpServletRequest request, HttpServletResponse response,
			String finish, String startBy,String processInstanceId,
			String processDefinitionId) {
		ModelAndView mav = new ModelAndView("/activiti/management/processList");
		Page<HistoricProcessInstance> page = new Page<HistoricProcessInstance>(
				request, response);
		HistoricProcessInstanceQuery query = historyService
				.createHistoricProcessInstanceQuery();
		String orderby = page.getOrderBy();
		if(StringUtils.isNotBlank(orderby)){
			String[] order = StringUtils.split(orderby, " ");
			if(order.length == 2){
				if("startTime".equals(order[0])){
					query.orderByProcessInstanceStartTime();
				} else if("endTime".equals(order[0])){
					query.orderByProcessInstanceEndTime();
				} 
				if("asc".equalsIgnoreCase(order[1])){
					query.asc();
				} else if("desc".equalsIgnoreCase(order[1])){
					query.desc();
				}
			} else {
				query.orderByProcessInstanceEndTime().desc();
			}
		} else {
			query.orderByProcessInstanceEndTime().desc();
		}
		if ("1".equals(finish)) {
			query.finished();
		} else if ("0".equals(finish)) {
			query.unfinished();
		}
		if (StringUtils.isNotEmpty(startBy)) {
			query.startedBy(startBy);
		}
		if (StringUtils.isNotEmpty(processInstanceId)) {
			query.processInstanceId(processInstanceId);
		}
		if (StringUtils.isNotEmpty(processDefinitionId)) {
			query.processDefinitionId(processDefinitionId);
		}
		page.setCount(query.count());
		List<HistoricProcessInstance> list = query.listPage(
				page.getFirstResult(), page.getMaxResults());
		page.setResult(list);
		mav.addObject("page", page);
		mav.addObject("finish", finish);
		mav.addObject("startBy", startBy);
		return mav;
	}

	/**
	 * 所有任务列表
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/task/list")
	public ModelAndView finishedTasks(String finish, String owner,
			String assign, HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("/activiti/management/taskList");
		Page<HistoricTaskInstance> page = new Page<HistoricTaskInstance>(
				request, response);
		HistoricTaskInstanceQuery query = historyService
				.createHistoricTaskInstanceQuery();
		String orderby = page.getOrderBy();
		if(StringUtils.isNotBlank(orderby)){
			String[] order = StringUtils.split(orderby," ");
			if(order.length == 2){
				if("createTime".equals(order[0])){
					query.orderByTaskCreateTime();
				} else if("endTime".equals(order[0])){
					query.orderByHistoricTaskInstanceEndTime();
				} else if("dueDate".equals(order[0])){
					query.orderByTaskDueDate();
				}
				if("asc".equalsIgnoreCase(order[1])){
					query.asc();
				} else if("desc".equalsIgnoreCase(order[1])){
					query.desc();
				}
			} else {
				query.orderByTaskCreateTime().desc();
			}
		} else {
			query.orderByTaskCreateTime().desc();
		}
		if ("1".equals(finish)) { // 已办理
			query.finished();
		} else if ("0".equals(finish)) { // 未办理
			query.unfinished();
		}
		if (StringUtils.isNotEmpty(assign)) {
			query.taskAssignee(assign);
		}
		if (StringUtils.isNotEmpty(owner)) {
			query.taskOwner(owner);
		}
		page.setCount(query.count());
		List<HistoricTaskInstance> list = query.listPage(page.getFirstResult(),
				page.getMaxResults());
		page.setResult(list);
		mav.addObject("owner", owner);
		mav.addObject("assign", assign);
		mav.addObject("finish", finish);
		mav.addObject("page", page);
		return mav;
	}

}
