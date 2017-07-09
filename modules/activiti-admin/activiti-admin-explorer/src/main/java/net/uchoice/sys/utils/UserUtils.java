/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.sys.utils;

import java.util.List;

import net.uchoice.common.utils.SpringContextHolder;
import net.uchoice.sys.dao.MenuDao;
import net.uchoice.sys.entity.Menu;

/**
 * 用户工具类
 * 
 * @author ThinkGem
 * @version 2013-12-05
 */
public class UserUtils {


	public static List<Menu> getMenus() {
		MenuDao menuDao = SpringContextHolder.getBean(MenuDao.class);
		return menuDao.findAllList(new Menu());
	}

	public static List<Menu> getMenusByParent(String parentId) {
		MenuDao menuDao = SpringContextHolder.getBean(MenuDao.class);
		Menu parent = new Menu(parentId);
		Menu m = new Menu();
		m.setParent(parent);
		return menuDao.findList(m);
	}

}
