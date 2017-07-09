package net.uchoice.activiti.entity;

import net.uchoice.common.persistence.DataEntity;

import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

/**
 * 流程表单管理Entity
 * @author xbyang
 * @version 2017-06-14
 */
@Alias("ActForm")
public class ActForm extends DataEntity<ActForm> {
	
	private static final long serialVersionUID = 1L;
	private String name;		// 表单名称
	private String content;		// 表单内容
	
	public ActForm() {
		super();
	}

	public ActForm(String id){
		super(id);
	}

	@Length(min=1, max=40, message="表单名称长度必须介于 1 和 40 之间")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}