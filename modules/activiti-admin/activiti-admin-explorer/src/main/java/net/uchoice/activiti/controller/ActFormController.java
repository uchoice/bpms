package net.uchoice.activiti.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.activiti.entity.ActForm;
import net.uchoice.activiti.service.ActFormService;
import net.uchoice.common.persistence.Page;
import net.uchoice.common.web.BaseController;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

/**
 * 流程表单管理Controller
 * 
 * @author xbyang
 * @version 2017-06-14
 */
@Controller
@RequestMapping(value = "/activiti/actForm")
public class ActFormController extends BaseController {

	@Autowired
	private ActFormService actFormService;

	@ModelAttribute
	public ActForm get(@RequestParam(required = false) String id) {
		ActForm entity = null;
		if (StringUtils.isNotBlank(id)) {
			entity = actFormService.get(id);
		}
		if (entity == null) {
			entity = new ActForm();
		}
		return entity;
	}

	@RequestMapping(value = { "list", "" })
	public String list(ActForm actForm, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		Page<ActForm> page = actFormService.findPage(new Page<ActForm>(request,
				response), actForm);
		model.addAttribute("page", page);
		return "activiti/actFormList";
	}

	@RequestMapping(value = "form")
	public String form(ActForm actForm, Model model) {
		model.addAttribute("actForm", actForm);
		return "activiti/actFormForm";
	}
	
	@RequestMapping(value = "preview")
	public String preview(ActForm actForm, Model model) {
		model.addAttribute("actForm", actForm);
		return "activiti/actFormPreview";
	}
	

	@RequestMapping(value = "save")
	public String save(ActForm actForm, Model model,
			RedirectAttributes redirectAttributes) {
		actForm.setContent(HtmlUtils.htmlUnescape(actForm.getContent()));
		if (!beanValidator(model, actForm)) {
			return form(actForm, model);
		}
		try{
			actFormService.save(actForm);
		} catch(DuplicateKeyException e){
			addMessage(redirectAttributes, "保存表单失败，表单"+actForm.getName()+"已存在");
			return form(actForm, model);
		}
		addMessage(redirectAttributes, "保存流程表单成功");
		return "redirect:/activiti/actForm/?repage";
	}

	@RequestMapping(value = "delete")
	public String delete(ActForm actForm,
			RedirectAttributes redirectAttributes) {
		actFormService.delete(actForm);
		addMessage(redirectAttributes, "删除流程表单成功");
		return "redirect:/activiti/actForm/?repage";
	}

}