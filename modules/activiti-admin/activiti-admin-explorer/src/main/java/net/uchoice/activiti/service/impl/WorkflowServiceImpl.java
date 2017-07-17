package net.uchoice.activiti.service.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.uchoice.activiti.dao.ActDelegateDao;
import net.uchoice.activiti.entity.ActDelegate;
import net.uchoice.activiti.service.ActFormService;
import net.uchoice.activiti.service.WorkflowService;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.extras.springsecurity4.auth.AuthUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

@Service
@Transactional(rollbackFor = Exception.class)
public class WorkflowServiceImpl implements WorkflowService {

	private static final Logger logger = LoggerFactory
			.getLogger(WorkflowServiceImpl.class);

	@Autowired
	private TaskService taskService;
	@Autowired
	private ActDelegateDao actDelegateDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private ManagementService managementService;
	@Autowired
	private ActFormService actFormService;

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void claimTask(String taskId) {
		User user = (User) AuthUtils.getAuthenticationObject().getPrincipal();
		List<ActDelegate> actDelegates = actDelegateDao.findListByTaksIds(
				Lists.newArrayList(taskId), user.getId());
		if (actDelegates.isEmpty()) {
			taskService.claim(taskId, user.getId());
		} else {
			ActDelegate actDelegate = actDelegates.get(0);
			taskService.setOwner(taskId, actDelegate.getDelegated());
			taskService.claim(taskId, user.getId());
			actDelegateDao.delete(actDelegate);
		}
	}

	@Override
	public String processDefinitionToModel(String processDefinitionId)
			throws UnsupportedEncodingException, XMLStreamException {
		ProcessDefinition processDefinition = repositoryService
				.createProcessDefinitionQuery()
				.processDefinitionId(processDefinitionId).singleResult();
		InputStream bpmnStream = repositoryService.getResourceAsStream(
				processDefinition.getDeploymentId(),
				processDefinition.getResourceName());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
		XMLStreamReader xtr = xif.createXMLStreamReader(in);
		BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

		BpmnJsonConverter converter = new BpmnJsonConverter();
		com.fasterxml.jackson.databind.node.ObjectNode modelNode = converter
				.convertToJson(bpmnModel);
		// String name = processDefinition.getResourceName();
		Model modelData = repositoryService.newModel();
		modelData.setKey(processDefinition.getKey());
		modelData.setName(processDefinition.getResourceName());
		modelData.setCategory(processDefinition.getDeploymentId());
		modelData.setTenantId(processDefinition.getTenantId());
		ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
		modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME,
				processDefinition.getName());
		modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
		modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION,
				processDefinition.getDescription());
		modelData.setMetaInfo(modelObjectNode.toString());
		repositoryService.saveModel(modelData);
		repositoryService.addModelEditorSource(modelData.getId(), modelNode
				.toString().getBytes("utf-8"));
		return modelData.getId();
	}

	@Override
	public String createModel(String name, String tenant, String key,
			String description) throws UnsupportedEncodingException {
		ObjectNode editorNode = objectMapper.createObjectNode();
		editorNode.put("id", "canvas");
		editorNode.put("resourceId", "canvas");
		ObjectNode stencilSetNode = objectMapper.createObjectNode();
		stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
		editorNode.replace("stencilset", stencilSetNode);
		Model modelData = repositoryService.newModel();

		ObjectNode modelObjectNode = objectMapper.createObjectNode();
		modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
		modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
		description = StringUtils.defaultString(description);
		modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION,
				description);
		modelData.setMetaInfo(modelObjectNode.toString());
		modelData.setName(name);
		modelData.setKey(StringUtils.defaultString(key));
		modelData.setTenantId(tenant);
		repositoryService.saveModel(modelData);
		repositoryService.addModelEditorSource(modelData.getId(), editorNode
				.toString().getBytes("utf-8"));
		return modelData.getId();
	}

}
