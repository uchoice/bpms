/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.activiti.entity.ActGrant;
import net.uchoice.activiti.service.ActGrantService;
import net.uchoice.activiti.service.WorkflowService;
import net.uchoice.common.persistence.Page;
import net.uchoice.common.web.BaseController;

import org.activiti.engine.RepositoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 流程代办信息Controller
 * 
 * @author xbyang
 * @version 2017-06-27
 */
@Controller
@RequestMapping(value = "/activiti/actGrant")
public class ActGrantController extends BaseController {

	@Autowired
	private ActGrantService actGrantService;
	@Autowired
	private WorkflowService workflowService;
	@Autowired
	private RepositoryService repositoryService; 

	@ModelAttribute
	public ActGrant get(@RequestParam(required = false) String id) {
		ActGrant entity = null;
		if (StringUtils.isNotBlank(id)) {
			entity = actGrantService.get(id);
		}
		if (entity == null) {
			entity = new ActGrant();
		}
		return entity;
	}

	@RequestMapping(value = { "list", "" })
	public String list(ActGrant actGrant, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		Page<ActGrant> page = actGrantService.findPage(
				new Page<ActGrant>(request, response), actGrant);
		model.addAttribute("page", page);
		model.addAttribute("actGrant", actGrant);
		model.addAttribute("processes", repositoryService.createProcessDefinitionQuery().list());
		return "activiti/actGrantList";
	}

	@RequestMapping(value = "form")
	public String form(ActGrant actGrant, Model model) {
		model.addAttribute("actGrant", actGrant);
		model.addAttribute("processes", repositoryService.createProcessDefinitionQuery().list());
		return "activiti/actGrantForm";
	}
	
	@RequestMapping(value = "save")
	public String save(ActGrant actGrant, Model model,
			RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, actGrant)) {
			return form(actGrant, model);
		}
		actGrantService.save(actGrant);
		addMessage(redirectAttributes, "保存流程代办信息成功");
		return "redirect:/activiti/actGrant/?repage";
	}

	@RequestMapping(value = "delete")
	public String delete(ActGrant actGrant,
			RedirectAttributes redirectAttributes) {
		actGrantService.delete(actGrant);
		addMessage(redirectAttributes, "取消流程代办成功");
		return "redirect:/activiti/actGrant/?repage";
	}

}