package net.uchoice.activiti.controller.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.activiti.cmd.JumpActivityCmd;
import net.uchoice.activiti.entity.ActForm;
import net.uchoice.activiti.service.ActFormService;
import net.uchoice.activiti.service.WorkflowService;
import net.uchoice.activiti.util.WorkflowUtils;
import net.uchoice.common.persistence.Page;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
	private WorkflowService workflowService;
	@Autowired
	private ActFormService actFormService;
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
			@RequestParam(value = "tenant" , required = true) String tenant,
			@RequestParam(value = "file", required = true) MultipartFile file,
			RedirectAttributes redirectAttributes) {
		try{
			String fileName = file.getOriginalFilename();
			Deployment deployment = null;
			String extension = fileName.substring(fileName.indexOf('.') + 1);
			if (extension.equals("zip") || extension.equals("bar")) {
				deployment = repositoryService.createDeployment().tenantId(tenant)
						.addZipInputStream(new ZipInputStream(file.getInputStream())).deploy();
				return "部署" + fileName + "成功";
			} else if (extension.equals("bpmn") || extension.equals("bpmn20.xml")) {
				BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(
						new InputStreamSource(file.getInputStream()), true, true);
				List<String> formKeys = WorkflowUtils.getFormKeys(bpmnModel);
				formKeys.add(tenant);
				List<ActForm> forms = actFormService.findFormsByName(formKeys);
				Set<String> notExistForms = WorkflowUtils.getNotExistForms(forms, formKeys);
				if (!notExistForms.isEmpty()) {
					redirectAttributes.addFlashAttribute("message", "部署" + fileName + "失败，流程中需要的表单"
							+ notExistForms + "不存在");
				} else {
					DeploymentBuilder deploymentBuilder = repositoryService
							.createDeployment().addInputStream(fileName,
									file.getInputStream());
					for (ActForm f : forms) {
						deploymentBuilder.addString(f.getName(), f.getContent());
					}
					deploymentBuilder.tenantId(tenant);
					deployment = deploymentBuilder.deploy();
					redirectAttributes.addFlashAttribute("message", "部署成功，部署ID=" + deployment.getId());
				}
			} else {
				redirectAttributes.addFlashAttribute("message", "部署" + fileName + "失败，上传文件类型错误");
			}
		}catch(Exception e){
			String fileName = file.getOriginalFilename();
			logger.error("failed to deploy {}", fileName , e);
			redirectAttributes.addFlashAttribute("message", "部署" + fileName + "失败：" + e.getMessage());
		}
		return "redirect:/management/process/defined/list";
	}

	@RequestMapping(value = "/definition/{processDefinitionId}/convert-to-model")
	public String convertToModel(
			@PathVariable("processDefinitionId") String processDefinitionId,
			RedirectAttributes redirectAttributes) {
		try{
			String modelId = workflowService.processDefinitionToModel(processDefinitionId);
			redirectAttributes.addFlashAttribute("message", "转化为模型成功，ID：" + modelId);
			return "redirect:/management/model/list";
		}catch(Exception e){
			logger.error("failed to convert model to model", e);
			redirectAttributes.addFlashAttribute("message", "转化为模型失败：" + e.getMessage());
			return "redirect:/management/process/defined/list";
		}
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