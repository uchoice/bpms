/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.activiti.entity.ActDelegate;
import net.uchoice.activiti.service.ActDelegateService;
import net.uchoice.common.persistence.Page;
import net.uchoice.common.web.BaseController;

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
@RequestMapping(value = "/activiti/actDelegate")
public class ActDelegateController extends BaseController {

	@Autowired
	private ActDelegateService actDelegateService;

	@ModelAttribute
	public ActDelegate get(@RequestParam(required = false) String id) {
		ActDelegate entity = null;
		if (StringUtils.isNotBlank(id)) {
			entity = actDelegateService.get(id);
		}
		if (entity == null) {
			entity = new ActDelegate();
		}
		return entity;
	}

	@RequestMapping(value = { "list", "" })
	public String list(ActDelegate actDelegate, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		Page<ActDelegate> page = actDelegateService.findPage(
				new Page<ActDelegate>(request, response), actDelegate);
		model.addAttribute("page", page);
		model.addAttribute("actDelegate", actDelegate);
		return "activiti/actDelegateList";
	}

	@RequestMapping(value = "form")
	public String form(ActDelegate actDelegate, Model model) {
		model.addAttribute("actDelegate", actDelegate);
		return "activiti/actDelegateForm";
	}

	@RequestMapping(value = "save")
	public String save(ActDelegate actDelegate, Model model,
			RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, actDelegate)) {
			return form(actDelegate, model);
		}
		actDelegateService.save(actDelegate);
		addMessage(redirectAttributes, "保存流程代办信息成功");
		return "redirect:/activiti/actDelegate/?repage";
	}

	@RequestMapping(value = "delete")
	public String delete(ActDelegate actDelegate,
			RedirectAttributes redirectAttributes) {
		actDelegate.setCancelDate(new Date());
		actDelegateService.delete(actDelegate);
		addMessage(redirectAttributes, "取消流程代办成功");
		return "redirect:/activiti/actDelegate/?repage";
	}

}