/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import net.uchoice.activiti.entity.ActDelegate;
import net.uchoice.common.persistence.CrudDao;

/**
 * 流程代办信息DAO接口
 * @author xbyang
 * @version 2017-06-27
 */
@Mapper
public interface ActDelegateDao extends CrudDao<ActDelegate> {

	List<ActDelegate> findEffectiveList(ActDelegate actDelegate);
	
}