/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.uchoice.activiti.cmd;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tom Baeyens
 */
public class GetRenderedTenantFormCmd implements Command<Object>, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String processInstanceId;

	public GetRenderedTenantFormCmd(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public Object execute(CommandContext commandContext) {
		HistoricProcessInstanceEntity historicProcessInstanceEntity = commandContext
				.getHistoricProcessInstanceEntityManager()
				.findHistoricProcessInstance(processInstanceId);

		if (historicProcessInstanceEntity == null) {
			throw new ActivitiObjectNotFoundException("ProcessInstance '"
					+ processInstanceId + "' not found",
					HistoricProcessInstanceEntity.class);
		}

		if (StringUtils.isEmpty(historicProcessInstanceEntity
				.getProcessDefinitionId())) {
			throw new ActivitiException(
					"processDefinitionId for ProcessInstance '"
							+ processInstanceId + "' not found");
		}

		ProcessDefinitionEntity processDefinitionEntity = commandContext
				.getProcessDefinitionEntityManager().findProcessDefinitionById(
						historicProcessInstanceEntity.getProcessDefinitionId());

		if (processDefinitionEntity == null) {
			throw new ActivitiObjectNotFoundException("ProcessDefinition '"
					+ historicProcessInstanceEntity.getProcessDefinitionId()
					+ "' not found", ProcessDefinitionEntity.class);
		}

		if (StringUtils.isEmpty(processDefinitionEntity.getTenantId())) {
			throw new ActivitiException("TenantId for ProcessDefinition '"
					+ processDefinitionEntity.getProcessDefinition()
					+ "' not found");
		}

		return renderTenantForm(processDefinitionEntity.getDeploymentId(),
				processDefinitionEntity.getTenantId());
	}
	/**
	 * 获取解析后的tenantForm内容
	 * @param deploymentId
	 * @param tenantId
	 * @return
	 */
	private Object renderTenantForm(String deploymentId, String tenantId) {
		String formTemplateString = getFormTemplateString(deploymentId,
				tenantId);
		ScriptingEngines scriptingEngines = Context
				.getProcessEngineConfiguration().getScriptingEngines();
		return scriptingEngines.evaluate(formTemplateString,
				ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE,
				new ProcessInstanceVariableScope(processInstanceId));
	}
	/**
	 * 获取全局表单模板
	 * @param deploymentId 部署ID
	 * @param tenant 表单名称
	 * @return
	 */
	private String getFormTemplateString(String deploymentId, String tenant) {

		ResourceEntity resourceStream = Context
				.getCommandContext()
				.getResourceEntityManager()
				.findResourceByDeploymentIdAndResourceName(deploymentId, tenant);

		if (resourceStream == null) {
			throw new ActivitiObjectNotFoundException("Tenant with key '"
					+ tenant + "' does not exist", String.class);
		}

		byte[] resourceBytes = resourceStream.getBytes();
		String encoding = "UTF-8";
		String formTemplateString = "";
		try {
			formTemplateString = new String(resourceBytes, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new ActivitiException("Unsupported encoding of :" + encoding,
					e);
		}
		return formTemplateString;
	}
}
