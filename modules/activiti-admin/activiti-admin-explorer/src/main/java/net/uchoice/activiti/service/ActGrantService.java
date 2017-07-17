/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.service;

import java.util.Date;
import java.util.List;

import net.uchoice.activiti.dao.ActGrantDao;
import net.uchoice.activiti.entity.ActGrant;
import net.uchoice.common.persistence.Page;
import net.uchoice.common.service.CrudService;

import org.activiti.engine.identity.User;
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
public class ActGrantService extends CrudService<ActGrantDao, ActGrant> {

	public ActGrant get(String id) {
		return super.get(id);
	}
	
	public List<ActGrant> findList(ActGrant actGrant) {
		return super.findList(actGrant);
	}
	
	public Page<ActGrant> findPage(Page<ActGrant> page, ActGrant actGrant) {
		return super.findPage(page, actGrant);
	}
	
	@Transactional(readOnly = false)
	public void save(ActGrant actGrant) {
		User user = (User) AuthUtils.getAuthenticationObject().getPrincipal();
		actGrant.setOwner(user.getId());
		super.save(actGrant);
	}
	
	@Transactional(readOnly = false)
	public void delete(ActGrant actGrant) {
		actGrant.setRevokeDate(new Date());
		super.delete(actGrant);
	}
	
}