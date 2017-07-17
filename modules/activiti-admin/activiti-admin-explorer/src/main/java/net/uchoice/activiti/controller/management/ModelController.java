package net.uchoice.activiti.controller.management;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.activiti.entity.ActForm;
import net.uchoice.activiti.service.ActFormService;
import net.uchoice.activiti.service.WorkflowService;
import net.uchoice.activiti.util.WorkflowUtils;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 流程模型控制器
 *
 * @author henryyan
 */
@Controller
@RequestMapping(value = "/management/model")
public class ModelController {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	ActFormService actFormService;

	@Autowired
	WorkflowService workflowService;

	/**
	 * 模型列表
	 */
	@RequestMapping(value = "list")
	public ModelAndView modelList() {
		ModelAndView mav = new ModelAndView("activiti/management/modelList");
		List<Model> list = repositoryService.createModelQuery().list();
		mav.addObject("list", list);
		return mav;
	}

	/**
	 * 创建模型
	 */
	@RequestMapping(value = "create", method = RequestMethod.POST)
	public void create(
			@RequestParam("name") String name,
			@RequestParam("tenant") String tenant,
			@RequestParam("key") String key,
			@RequestParam(value = "description", required = false) String description,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			String modelId = workflowService.createModel(name, tenant, key,
					description);
			response.sendRedirect(request.getContextPath()
					+ "/modeler.html?modelId=" + modelId);
		} catch (Exception e) {
			logger.error("create model failed!", e);
			try (OutputStream os = response.getOutputStream()) {
				os.write(("create model failed ! cause by :" + e.getMessage())
						.getBytes("UTF-8"));
				os.flush();
			} catch (Exception e1) {
				logger.error("write to client failed!", e);
			}
		}
	}

	/**
	 * 根据Model部署流程
	 */
	@RequestMapping(value = "deploy/{modelId}")
	public String deploy(@PathVariable("modelId") String modelId,
			RedirectAttributes redirectAttributes) {
		try {
			Model modelData = repositoryService.getModel(modelId);
			if(StringUtils.isEmpty(modelData.getTenantId())){
				redirectAttributes.addFlashAttribute("message", 
						"部署失败，该模型未输入流程全局表单，请输入流程全局表单后再试!");
			} else {
				ObjectNode modelNode = (ObjectNode) new ObjectMapper()
					.readTree(repositoryService.getModelEditorSource(modelData
						.getId()));
				BpmnModel model = new BpmnJsonConverter()
				.convertToBpmnModel(modelNode);
				byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
				List<String> formKeys = WorkflowUtils.getFormKeys(model);
				formKeys.add(modelData.getTenantId());
				
				List<ActForm> forms = actFormService.findFormsByName(formKeys);
				Set<String> notExistForms = WorkflowUtils.getNotExistForms(forms, formKeys);
				if (!notExistForms.isEmpty()) {
					redirectAttributes.addFlashAttribute("message", "部署" + modelId + "失败，流程中需要的表单"
							+ notExistForms + "不存在");
				} else {
					String processName = modelData.getName() + ".bpmn20.xml";
					DeploymentBuilder deploymentBuilder = repositoryService
							.createDeployment().name(modelData.getName())
							.addString(processName, new String(bpmnBytes));
					for (ActForm f : forms) {
						deploymentBuilder.addString(f.getName(), f.getContent());
					}
					deploymentBuilder.tenantId(modelData.getTenantId());
					Deployment deployment = deploymentBuilder.deploy();
					redirectAttributes.addFlashAttribute("message", "部署成功，部署ID="
							+ deployment.getId());
				}
			}
		} catch (Exception e) {
			logger.error("deploy by modelId failed，modelId={}", modelId, e);
			redirectAttributes.addFlashAttribute("message",
					"模型部署失败，" + e.getMessage());
		}
		return "redirect:/management/model/list";
	}

	/**
	 * 导出model对象为指定类型
	 *
	 * @param modelId
	 *            模型ID
	 * @param type
	 *            导出文件类型(bpmn\json)
	 */
	@RequestMapping(value = "export/{modelId}/{type}")
	public void export(@PathVariable("modelId") String modelId,
			@PathVariable("type") String type, HttpServletResponse response) {
		try {
			Model modelData = repositoryService.getModel(modelId);
			BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
			byte[] modelEditorSource = repositoryService
					.getModelEditorSource(modelData.getId());

			JsonNode editorNode = new ObjectMapper()
					.readTree(modelEditorSource);
			BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

			// 处理异常
			if (bpmnModel.getMainProcess() == null) {
				response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
				response.getOutputStream().println(
						"no main process, can't export for type: " + type);
				response.flushBuffer();
				return;
			}

			String filename = "";
			byte[] exportBytes = null;

			String mainProcessId = bpmnModel.getMainProcess().getId();

			if (type.equals("bpmn")) {

				BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
				exportBytes = xmlConverter.convertToXML(bpmnModel);

				filename = mainProcessId + ".bpmn20.xml";
			} else if (type.equals("json")) {

				exportBytes = modelEditorSource;
				filename = mainProcessId + ".json";

			}

			ByteArrayInputStream in = new ByteArrayInputStream(exportBytes);
			IOUtils.copy(in, response.getOutputStream());

			response.setHeader("Content-Disposition", "attachment; filename="
					+ filename);
			response.flushBuffer();
		} catch (Exception e) {
			logger.error("导出model的xml文件失败：modelId={}, type={}", modelId, type,
					e);
		}
	}

	@RequestMapping(value = "delete/{modelId}")
	public String delete(@PathVariable("modelId") String modelId) {
		repositoryService.deleteModel(modelId);
		return "redirect:/management/model/list";
	}

}
