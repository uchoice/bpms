/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.service;

import java.util.Collections;
import java.util.List;

import net.uchoice.activiti.dao.ActDelegateDao;
import net.uchoice.activiti.entity.ActDelegate;
import net.uchoice.common.persistence.Page;
import net.uchoice.common.service.CrudService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程代理Service
 * 
 * @author xbyang
 * @version 2017-07-14
 */
@Service
@Transactional(readOnly = true)
public class ActDelegateService extends
		CrudService<ActDelegateDao, ActDelegate> {

	public ActDelegate get(String id) {
		return super.get(id);
	}

	public List<ActDelegate> findList(ActDelegate actDelegate) {
		return super.findList(actDelegate);
	}
	
	public int batchInsert(List<ActDelegate> delegates){
		return dao.batchInsert(delegates);
	}

	public Page<ActDelegate> findPage(Page<ActDelegate> page,
			ActDelegate actDelegate) {
		return super.findPage(page, actDelegate);
	}
	
	public List<ActDelegate> findListByTaksIds(List<String> taskIds, String delegate) {
		if(taskIds == null || taskIds.isEmpty()){
			return Collections.emptyList();
		}
		return dao.findListByTaksIds(taskIds, delegate);
	}

	@Transactional(readOnly = false)
	public void save(ActDelegate actDelegate) {
		super.save(actDelegate);
	}

	@Transactional(readOnly = false)
	public void delete(ActDelegate actDelegate) {
		super.delete(actDelegate);
	}

}