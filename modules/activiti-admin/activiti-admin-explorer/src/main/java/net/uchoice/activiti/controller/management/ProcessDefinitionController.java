package net.uchoice.activiti.controller.management;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.uchoice.activiti.cmd.JumpActivityCmd;
import net.uchoice.activiti.entity.ActForm;
import net.uchoice.activiti.service.ActFormService;
import net.uchoice.activiti.util.WorkflowUtils;
import net.uchoice.common.persistence.Page;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.converter.XMLStreamReaderUtil;
import org.activiti.bpmn.converter.util.InputStreamProvider;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.XmlStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

/**
 * 流程管理控制器
 *
 * @author HenryYan
 */
@Controller
@RequestMapping(value = "/management/process")
public class ProcessDefinitionController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private ManagementService managementService;
	@Autowired
	ActFormService actFormService;

	/**
	 * 流程定义列表
	 *
	 * @return
	 */
	@RequestMapping(value = "/defined/list")
	public ModelAndView definedList(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("activiti/management/definedProcessList");
		/*
		 * 保存两个对象，一个是ProcessDefinition（流程定义），一个是Deployment（流程部署）
		 */
		List<Object[]> objects = new ArrayList<Object[]>();

		Page<Object[]> page = new Page<Object[]>(request, response);

		ProcessDefinitionQuery processDefinitionQuery = repositoryService
				.createProcessDefinitionQuery().orderByDeploymentId().desc();
		page.setCount(processDefinitionQuery.count());
		List<ProcessDefinition> processDefinitionList = processDefinitionQuery
				.listPage(page.getFirstResult(), page.getMaxResults());
		for (ProcessDefinition processDefinition : processDefinitionList) {
			String deploymentId = processDefinition.getDeploymentId();
			Deployment deployment = repositoryService.createDeploymentQuery()
					.deploymentId(deploymentId).singleResult();
			objects.add(new Object[] { processDefinition, deployment });
		}
		page.setResult(objects);
		mav.addObject("page", page);

		return mav;
	}
	
	@RequestMapping(value = "/running/list")
	public ModelAndView runningList(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("activiti/management/runningProcessList");
		Page<ProcessInstance> page = new Page<ProcessInstance>(request,
				response);
		ProcessInstanceQuery processInstanceQuery = runtimeService
				.createProcessInstanceQuery().orderByProcessInstanceId().desc();
		page.setCount(processInstanceQuery.count());
		List<ProcessInstance> list = processInstanceQuery.listPage(
				page.getFirstResult(), page.getMaxResults());
		page.setResult(list);
		mav.addObject("page", page);
		return mav;
	}
	

	/**
	 * 挂起、激活流程实例
	 */
	@RequestMapping(value = "/update/instance/{processInstanceId}/{state}")
	public String updateInstanceState(@PathVariable("state") String state,
			@PathVariable("processInstanceId") String processInstanceId,
			RedirectAttributes redirectAttributes) {
		if (state.equals("active")) {
			redirectAttributes.addFlashAttribute("message", "已激活ID为["
					+ processInstanceId + "]的流程实例。");
			runtimeService.activateProcessInstanceById(processInstanceId);
		} else if (state.equals("suspend")) {
			runtimeService.suspendProcessInstanceById(processInstanceId);
			redirectAttributes.addFlashAttribute("message", "已挂起ID为["
					+ processInstanceId + "]的流程实例。");
		}
		return "redirect:/management/process/running/list";
	}

	@Value("${export.diagram.path}")
	private String exportDir;

	/**
	 * 部署全部流程
	 *
	 * @return
	 * @throws Exception
	 */
	/*@RequestMapping(value = "/redeploy/all")
	public String redeployAll() throws Exception {
		Collection<File> files = FileUtils.listFiles(new File(exportDir),
				new String[] { ".zip", ".bar" }, true);
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource = null;
		String path = null;
		for (File f : files) {
			path = f.getAbsolutePath();
			logger.debug("read workflow from: {}", path);
			resource = resourceLoader.getResource(path);
			InputStream inputStream = resource.getInputStream();
			if (inputStream == null) {
				logger.warn("ignore deploy workflow module: {}", path);
			} else {
				logger.debug("finded workflow module: {}, deploy it!", path);
				ZipInputStream zis = new ZipInputStream(inputStream);
				Deployment deployment = repositoryService.createDeployment()
						.addZipInputStream(zis).deploy();
				// export diagram
				List<ProcessDefinition> list = repositoryService
						.createProcessDefinitionQuery()
						.deploymentId(deployment.getId()).list();
				for (ProcessDefinition processDefinition : list) {
					WorkflowUtils.exportDiagramToFile(repositoryService,
							processDefinition, exportDir);
				}
			}
		}
		return "redirect:/management/process/defined/list";
	}*/

	/**
	 * 删除部署的流程，级联删除流程实例
	 *
	 * @param deploymentId
	 *            流程部署ID
	 */
	@RequestMapping(value = "/delete/deployment/{deploymentId}")
	public String delete(@PathVariable("deploymentId") String deploymentId,
			RedirectAttributes redirectAttributes) {
		repositoryService.deleteDeployment(deploymentId, true);
		redirectAttributes.addFlashAttribute("message", "已删除ID为["
				+ deploymentId + "]的部署流程。");
		return "redirect:/management/process/defined/list";
	}

	@RequestMapping(value = "/deploy")
	public String deploy(
			@RequestParam(value = "file", required = false) MultipartFile file,
			RedirectAttributes redirectAttributes) {

		String fileName = file.getOriginalFilename();

		try {
			Deployment deployment = null;

			String extension = FilenameUtils.getExtension(fileName);
			if (extension.equals("zip") || extension.equals("bar")) {
				ZipInputStream zip = new ZipInputStream(file.getInputStream());
				deployment = repositoryService.createDeployment()
						.addZipInputStream(zip).deploy();
			} else {
				BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(new InputStreamSource(file.getInputStream()), true, true);
				List<String> formKeys = WorkflowUtils.getFormKeys(bpmnModel);
				List<ActForm> forms = actFormService.findFormsByName(formKeys);
				if(forms.size() != formKeys.size()){
					Set<String> existsForms = Sets.newHashSet();
					StringBuilder notExists  = new StringBuilder();
					for(ActForm actForm:forms){
						existsForms.add(actForm.getName());
					}
					for(String fkey:formKeys){
						if(!existsForms.contains(fkey)){
							notExists.append(fkey).append(",");
						}
					}
					redirectAttributes.addFlashAttribute("message", "部署失败，流程中需要的表单"
							+ notExists.substring(0, notExists.length() - 1) + "不存在");
				} else {
					DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
							.addInputStream(fileName, file.getInputStream());
					for(ActForm f:forms){
						deploymentBuilder.addString(f.getName(), f.getContent());
					}
					deployment = deploymentBuilder.deploy();
					redirectAttributes.addFlashAttribute("message", "部署成功，部署ID="
							+ deployment.getId());
				}
			}

			redirectAttributes.addFlashAttribute("message", "部署"
					+ fileName + "成功");
		} catch (Exception e) {
			logger.error(
					"error on deploy process, because of file input stream", e);
			redirectAttributes.addFlashAttribute("message", "部署"
					+ fileName + "失败：" + e.getMessage());
		}

		return "redirect:/management/process/defined/list";
	}

	@RequestMapping(value = "/definition/{processDefinitionId}/convert-to-model")
	public String convertToModel(
			@PathVariable("processDefinitionId") String processDefinitionId)
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
		//String name = processDefinition.getResourceName();
		Model modelData = repositoryService.newModel();
		modelData.setKey(processDefinition.getKey());
		modelData.setName(processDefinition.getResourceName());
		modelData.setCategory(processDefinition.getDeploymentId());

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

		return "redirect:/management/model/list";
	}

	/**
	 * 挂起、激活流程实例
	 */
	@RequestMapping(value = "/update/definition/{processDefinitionId}/{state}")
	public String updateDefinitionState(@PathVariable("state") String state,
			@PathVariable("processDefinitionId") String processDefinitionId,
			RedirectAttributes redirectAttributes) {
		if (state.equals("active")) {
			redirectAttributes.addFlashAttribute("message", "已激活ID为["
					+ processDefinitionId + "]的流程定义。");
			repositoryService.activateProcessDefinitionById(
					processDefinitionId, true, null);
		} else if (state.equals("suspend")) {
			repositoryService.suspendProcessDefinitionById(processDefinitionId,
					true, null);
			redirectAttributes.addFlashAttribute("message", "已挂起ID为["
					+ processDefinitionId + "]的流程定义。");
		}
		return "redirect:/management/process/defined/list";
	}

	/**
	 * 导出图片文件到硬盘
	 *
	 * @return
	 */
	@RequestMapping(value = "export/diagrams")
	@ResponseBody
	public List<String> exportDiagrams() throws IOException {
		List<String> files = new ArrayList<String>();
		List<ProcessDefinition> list = repositoryService
				.createProcessDefinitionQuery().list();

		for (ProcessDefinition processDefinition : list) {
			files.add(WorkflowUtils.exportDiagramToFile(repositoryService,
					processDefinition, exportDir));
		}

		return files;
	}

	@RequestMapping(value = "/activity/jump")
	@ResponseBody
	public boolean jump(@RequestParam("executionId") String executionId,
			@RequestParam("activityId") String activityId) {
		Command<Object> cmd = new JumpActivityCmd(executionId, activityId);
		managementService.executeCommand(cmd);
		return true;
	}

	@RequestMapping(value = "/defined/{processDefinitionId}/bpmn/model")
	@ResponseBody
	public BpmnModel queryBpmnModel(
			@PathVariable("processDefinitionId") String processDefinitionId) {
		BpmnModel bpmnModel = repositoryService
				.getBpmnModel(processDefinitionId);
		return bpmnModel;
	}

}