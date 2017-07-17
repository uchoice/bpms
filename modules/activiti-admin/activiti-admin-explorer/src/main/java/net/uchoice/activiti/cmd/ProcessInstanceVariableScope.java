package net.uchoice.activiti.cmd;

import java.util.List;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;

import com.google.common.collect.Lists;

public class ProcessInstanceVariableScope extends ExecutionEntity {

	protected String processInstanceId;

	public ProcessInstanceVariableScope(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected List<VariableInstanceEntity> loadVariableInstances() {
		CommandContext commandContext = Context.getCommandContext();
		HistoricVariableInstanceQueryImpl query = new HistoricVariableInstanceQueryImpl();
		query.processInstanceId(processInstanceId);
		List<HistoricVariableInstance> historicVariableInstances = commandContext
				.getHistoricVariableInstanceEntityManager()
				.findHistoricVariableInstancesByQueryCriteria(query,
						new Page(0, Integer.MAX_VALUE));
		return historicVariableInstancesToVariableInstances(historicVariableInstances);
	}

	private List<VariableInstanceEntity> historicVariableInstancesToVariableInstances(
			List<HistoricVariableInstance> historicVars) {
		List<VariableInstanceEntity> variableInstances = Lists.newArrayList();
		for (HistoricVariableInstance historicVar : historicVars) {
			variableInstances
					.add(historicVariableInstanceToVariableInstance(historicVar));
		}
		return variableInstances;
	}

	private VariableInstanceEntity historicVariableInstanceToVariableInstance(
			HistoricVariableInstance historicVariableInstance) {
		HistoricVariableInstanceEntity historicVariableInstanceEntity = (HistoricVariableInstanceEntity) historicVariableInstance;
		VariableInstanceEntity variableInstance = VariableInstanceEntity
				.create(historicVariableInstanceEntity.getName(),
						historicVariableInstanceEntity.getVariableType(),
						historicVariableInstanceEntity.getValue());
		variableInstance.setExecutionId(historicVariableInstanceEntity
				.getExecutionId());
		variableInstance.setProcessInstanceId(historicVariableInstanceEntity
				.getProcessInstanceId());
		return variableInstance;
	}

}
