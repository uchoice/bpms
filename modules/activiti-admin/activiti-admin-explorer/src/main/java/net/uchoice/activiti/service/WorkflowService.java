package net.uchoice.activiti.service;

import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLStreamException;

public interface WorkflowService {
	/**
	 * 签收任务
	 * @param taskId
	 */
	public void claimTask(String taskId);
	
	
	/**
	 * 将流程定义转化为模型
	 * @param processDefinitionId
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws XMLStreamException
	 * @return 模型ID
	 */
	public String processDefinitionToModel(String processDefinitionId) throws UnsupportedEncodingException,XMLStreamException;
	
	/**
	 * 创建模型
	 * @param name 名称
	 * @param tenant 全局表单
	 * @param key key
	 * @param description 描述
	 * @throws UnsupportedEncodingException
	 * @return 模型ID
	 */
	public String createModel(String name,String tenant,String key,String description) throws UnsupportedEncodingException;
	
}
