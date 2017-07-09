package net.uchoice.thymeleaf.expression;

import java.util.List;

import net.uchoice.sys.entity.Menu;
import net.uchoice.sys.utils.UserUtils;


public final class Users {
	
	public List<Menu> menus(){
		return UserUtils.getMenus();
	}
	
	public static List<Menu> getMenusByParent(String parentId) {
		return UserUtils.getMenusByParent(parentId);
	}

}
