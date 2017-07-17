/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.uchoice.activiti.entity.ActDelegate;
import net.uchoice.common.persistence.CrudDao;

/**
 * 流程代理DAO接口
 * @author xbyang
 * @version 2017-07-14
 */
@Mapper
public interface ActDelegateDao extends CrudDao<ActDelegate> {
	
	
	int batchInsert(List<ActDelegate> delegates);
	
	List<ActDelegate> findListByTaksIds(@Param("taskIds") List<String> taskIds, @Param("delegate") String delegate);
	
}