/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.entity;

import net.uchoice.common.persistence.DataEntity;

import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;


/**
 * 流程代理Entity
 * @author xbyang
 * @version 2017-07-14
 */
@Alias("ActDelegate")
public class ActDelegate extends DataEntity<ActDelegate> {
	
	private static final long serialVersionUID = 1L;
	private String taskId;		// 服务ID
	private String delegate;		// 代理人
	private String delegated;		// 被代理人
	
	public ActDelegate() {
		super();
	}

	public ActDelegate(String id){
		super(id);
	}

	@Length(min=1, max=64, message="服务ID长度必须介于 1 和 64 之间")
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	@Length(min=1, max=64, message="代理人长度必须介于 1 和 64 之间")
	public String getDelegate() {
		return delegate;
	}

	public void setDelegate(String delegate) {
		this.delegate = delegate;
	}
	
	@Length(min=1, max=64, message="被代理人长度必须介于 1 和 64 之间")
	public String getDelegated() {
		return delegated;
	}

	public void setDelegated(String delegated) {
		this.delegated = delegated;
	}
	
}