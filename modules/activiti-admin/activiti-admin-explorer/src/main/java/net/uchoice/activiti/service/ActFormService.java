package net.uchoice.activiti.service;

import java.util.Collections;
import java.util.List;

import net.uchoice.activiti.dao.ActFormDao;
import net.uchoice.activiti.entity.ActForm;
import net.uchoice.common.persistence.Page;
import net.uchoice.common.service.CrudService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程表单管理Service
 * @author xbyang
 * @version 2017-06-14
 */
@Service
@Transactional(readOnly = true)
public class ActFormService extends CrudService<ActFormDao, ActForm> {

	
	public ActForm get(String id) {
		return super.get(id);
	}
	
	public List<ActForm> findList(ActForm actAdminForm) {
		return super.findList(actAdminForm);
	}
	
	public Page<ActForm> findPage(Page<ActForm> page, ActForm actAdminForm) {
		return super.findPage(page, actAdminForm);
	}
	
	public List<ActForm> findFormsByName(List<String> names){
		if(names == null || names.isEmpty()){
			return Collections.emptyList();
		}
		return dao.findFormsByName(names);
	}
	
	@Transactional(readOnly = false)
	public void save(ActForm actAdminForm) {
		super.save(actAdminForm);
	}
	
	@Transactional(readOnly = false)
	public void delete(ActForm actAdminForm) {
		super.delete(actAdminForm);
	}
	
}