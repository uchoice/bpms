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
@Alias("ActGrant")
public class ActGrant extends DataEntity<ActGrant> {
	
	private static final long serialVersionUID = 1L;
	private String owner;		// 所有人
	private String grant;		// 被授予人
	private String processId;		// 流程ID
	private String processName;		// 流程名称
	private Date startDate;		// 开始日期
	private Date endDate;		// 结束日期
	private String revoke;		// 收回
	private Date revokeDate;		// 收回日期
	private Date nowDate;
	
	public ActGrant() {
		super();
	}

	public ActGrant(String id){
		super(id);
	}

	@Length(min=1, max=64, message="所有人长度必须介于 1 和 64 之间")
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	@Length(min=1, max=64, message="被授予人长度必须介于 1 和 64 之间")
	public String getGrant() {
		return grant;
	}

	public void setGrant(String grant) {
		this.grant = grant;
	}
	
	@Length(min=1, max=64, message="流程ID长度必须介于 1 和 64 之间")
	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	@Length(min=0, max=255, message="流程名称长度必须介于 0 和 255 之间")
	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="开始日期不能为空")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="结束日期不能为空")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	@Length(min=0, max=1, message="是否收回权限长度必须介于 0 和 1 之间")
	public String getRevoke() {
		return revoke;
	}

	public void setRevoke(String revoke) {
		this.revoke = revoke;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getRevokeDate() {
		return revokeDate;
	}

	public void setRevokeDate(Date revokeDate) {
		this.revokeDate = revokeDate;
	}

	public Date getNowDate() {
		return nowDate;
	}

	public void setNowDate(Date nowDate) {
		this.nowDate = nowDate;
	}
	
	
	
}