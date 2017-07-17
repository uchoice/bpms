/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.dao;

import net.uchoice.activiti.entity.ActGrant;
import net.uchoice.common.persistence.CrudDao;

import org.apache.ibatis.annotations.Mapper;

/**
 * 流程代办信息DAO接口
 * @author xbyang
 * @version 2017-06-27
 */
@Mapper
public interface ActGrantDao extends CrudDao<ActGrant> {

	
}