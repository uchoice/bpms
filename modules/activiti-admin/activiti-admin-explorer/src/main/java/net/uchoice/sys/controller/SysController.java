package net.uchoice.sys.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.common.utils.CookieUtils;
import net.uchoice.sys.entity.Menu;
import net.uchoice.sys.utils.UserUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;


@Controller
public class SysController {

	
	@RequestMapping("/sys/login")
	public String login(HttpServletRequest request, HttpServletResponse response){
		return "sys/login";
	}
	
	/**
	 * 获取主题方案
	 */
	@RequestMapping(value = "/theme/{theme}")
	public String getThemeInCookie(@PathVariable String theme, HttpServletRequest request, HttpServletResponse response){
		if (StringUtils.isNotBlank(theme)){
			CookieUtils.setCookie(response, "theme", theme);
		}else{
			theme = CookieUtils.getCookie(request, "theme");
		}
		return "redirect:"+request.getParameter("url");
	}
	
	
	@RequestMapping("/sys/index")
	public String home(){
		return "sys/index";
	}
	
	@RequestMapping(value = "/sys/menu/tree")
	public String tree(Model model, @RequestParam(required = true) String parentId) {
		List<Menu> menus =  UserUtils.getMenus();
		List<Menu> results = buildTree(menus, new Menu(parentId));
		model.addAttribute("menulist", results);
		return "sys/menuTree";
	}
	
	private List<Menu> buildTree(List<Menu> menus, Menu parent){
		List<Menu> results = Lists.newArrayList();
		List<Menu> childs = null;
		for(Menu m:menus){
			if(parent.getId().equals(m.getParentId()) && "1".equals(m.getIsShow())){
				m.setParent(parent);
				childs = buildTree(menus, m);
				m.setChildren(childs);
				results.add(m);
			}
		}
		return results;
	}
	
}
