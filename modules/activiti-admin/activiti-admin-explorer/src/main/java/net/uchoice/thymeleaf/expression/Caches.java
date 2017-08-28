package net.uchoice.thymeleaf.expression;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.uchoice.common.utils.SpringContextHolder;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;

import com.google.common.collect.Maps;

/**
 * 流程定义缓存
 *
 * @author henryyan
 */
public class Caches {

	private Map<String, ProcessDefinition> processDefinitions = Maps.newHashMap();

	private Map<String, List<ActivityImpl>> activities = Maps.newHashMap();

	private Map<String, ActivityImpl> singleActivity = Maps.newHashMap();

	private RepositoryService repositoryService;

	public ProcessDefinition get(String processDefinitionId) {
		if(repositoryService == null){
			repositoryService = SpringContextHolder.getBean(RepositoryService.class);
		}
		ProcessDefinition processDefinition = processDefinitions.get(processDefinitionId);
		if (processDefinition == null) {
			processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
					.getDeployedProcessDefinition(processDefinitionId);
			if (processDefinition != null) {
				put(processDefinitionId, processDefinition);
			}
		}
		return processDefinition;
	}

	public void put(String processDefinitionId,
			ProcessDefinition processDefinition) {
		processDefinitions.put(processDefinitionId, processDefinition);
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) processDefinition;
		activities.put(processDefinitionId, pde.getActivities());
		for (ActivityImpl activityImpl : pde.getActivities()) {
			singleActivity.put(
					processDefinitionId + "_" + activityImpl.getId(),
					activityImpl);
		}
	}

	public ActivityImpl getActivity(String processDefinitionId,
			String activityId) {
		ProcessDefinition processDefinition = get(processDefinitionId);
		if (processDefinition != null) {
			ActivityImpl activityImpl = singleActivity.get(processDefinitionId
					+ "_" + activityId);
			if (activityImpl != null) {
				return activityImpl;
			}
		}
		return null;
	}

	public String getActivityName(String processDefinitionId, String activityId) {
		ActivityImpl activity = getActivity(processDefinitionId, activityId);
		if (activity != null) {
			return Objects.toString(activity.getProperty("name"), "");
		}
		return null;
	}

}
