/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.service;

import java.util.Date;
import java.util.List;

import net.uchoice.activiti.dao.ActDelegateDao;
import net.uchoice.activiti.entity.ActDelegate;
import net.uchoice.common.persistence.Page;
import net.uchoice.common.service.CrudService;

import org.activiti.engine.identity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.extras.springsecurity4.auth.AuthUtils;

/**
 * 流程代办信息Service
 * @author xbyang
 * @version 2017-06-27
 */
@Service
@Transactional(readOnly = true)
public class ActDelegateService extends CrudService<ActDelegateDao, ActDelegate> {

	public ActDelegate get(String id) {
		return super.get(id);
	}
	
	public List<ActDelegate> findEffectiveList(ActDelegate actDelegate) {
		if(StringUtils.isBlank(actDelegate.getAssign())){
			User user = (User) AuthUtils.getAuthenticationObject().getPrincipal();
			actDelegate.setAssign(user.getId());
		}
		if(actDelegate.getNowDate() == null){
			actDelegate.setNowDate(new Date());
		}
		return dao.findEffectiveList(actDelegate);
	}
	
	public List<ActDelegate> findList(ActDelegate actDelegate) {
		return super.findList(actDelegate);
	}
	
	public Page<ActDelegate> findPage(Page<ActDelegate> page, ActDelegate actDelegate) {
		return super.findPage(page, actDelegate);
	}
	
	@Transactional(readOnly = false)
	public void save(ActDelegate actDelegate) {
		User user = (User) AuthUtils.getAuthenticationObject().getPrincipal();
		actDelegate.setOwner(user.getId());
		super.save(actDelegate);
	}
	
	@Transactional(readOnly = false)
	public void delete(ActDelegate actDelegate) {
		super.delete(actDelegate);
	}
	
}