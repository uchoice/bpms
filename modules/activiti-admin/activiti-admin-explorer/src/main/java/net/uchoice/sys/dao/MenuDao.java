/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.sys.dao;

import org.apache.ibatis.annotations.Mapper;

import net.uchoice.common.persistence.CrudDao;
import net.uchoice.sys.entity.Menu;

/**
 * 菜单DAO接口
 * @author ThinkGem
 * @version 2014-05-16
 */
@Mapper
public interface MenuDao extends CrudDao<Menu> {

	
}
