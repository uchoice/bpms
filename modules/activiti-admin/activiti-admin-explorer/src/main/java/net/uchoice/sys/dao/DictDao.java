/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.sys.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import net.uchoice.common.persistence.CrudDao;
import net.uchoice.sys.entity.Dict;

/**
 * 字典DAO接口
 * 
 * @author ThinkGem
 * @version 2014-05-16
 */
@Mapper
public interface DictDao extends CrudDao<Dict> {

	public List<String> findTypeList(Dict dict);

}
