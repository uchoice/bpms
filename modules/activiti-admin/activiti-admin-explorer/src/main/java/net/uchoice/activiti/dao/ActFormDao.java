package net.uchoice.activiti.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import net.uchoice.activiti.entity.ActForm;
import net.uchoice.common.persistence.CrudDao;

/**
 * 流程表单管理DAO接口
 * @author xbyang
 * @version 2017-06-14
 */
@Mapper
public interface ActFormDao extends CrudDao<ActForm> {
	
	List<ActForm> findFormsByName(List<String> names);
	
}