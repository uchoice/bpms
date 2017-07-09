/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.activiti.entity;

import java.util.Date;

import javax.validation.constraints.NotNull;

import net.uchoice.common.persistence.DataEntity;

import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 流程代办信息Entity
 * @author xbyang
 * @version 2017-06-27
 */
@Alias("ActDelegate")
public class ActDelegate extends DataEntity<ActDelegate> {
	
	private static final long serialVersionUID = 1L;
	private String owner;		// owner
	private String assign;		// assign
	private String processId;		// process_id
	private String processName;		// process_name
	private Date startDate;		// start_date
	private Date endDate;		// end_date
	private String canceled;		// canceled
	private Date cancelDate;		// cancel_date
	private Date nowDate;
	
	public ActDelegate() {
		super();
	}

	public ActDelegate(String id){
		super(id);
	}

	@Length(min=1, max=64, message="owner长度必须介于 1 和 64 之间")
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	@Length(min=1, max=64, message="assign长度必须介于 1 和 64 之间")
	public String getAssign() {
		return assign;
	}

	public void setAssign(String assign) {
		this.assign = assign;
	}
	
	@Length(min=1, max=64, message="process_id长度必须介于 1 和 64 之间")
	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	@Length(min=0, max=255, message="process_name长度必须介于 0 和 255 之间")
	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="start_date不能为空")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="end_date不能为空")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	@Length(min=0, max=1, message="canceled长度必须介于 0 和 1 之间")
	public String getCanceled() {
		return canceled;
	}

	public void setCanceled(String canceled) {
		this.canceled = canceled;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCancelDate() {
		return cancelDate;
	}

	public void setCancelDate(Date cancelDate) {
		this.cancelDate = cancelDate;
	}

	public Date getNowDate() {
		return nowDate;
	}

	public void setNowDate(Date nowDate) {
		this.nowDate = nowDate;
	}
	
	
	
}