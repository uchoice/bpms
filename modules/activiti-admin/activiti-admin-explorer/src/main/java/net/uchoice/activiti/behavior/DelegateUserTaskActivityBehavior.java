package net.uchoice.activiti.behavior;

import java.util.Date;
import java.util.List;
import java.util.Set;

import net.uchoice.activiti.entity.ActDelegate;
import net.uchoice.activiti.entity.ActGrant;
import net.uchoice.activiti.service.ActDelegateService;
import net.uchoice.activiti.service.ActGrantService;
import net.uchoice.common.utils.SpringContextHolder;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DelegateUserTaskActivityBehavior extends UserTaskActivityBehavior {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DelegateUserTaskActivityBehavior(String userTaskId,
			TaskDefinition taskDefinition) {
		super(userTaskId, taskDefinition);
	}

	@Override
	protected void handleAssignments(Expression assigneeExpression,
			Expression ownerExpression,
			Set<Expression> candidateUserExpressions,
			Set<Expression> candidateGroupExpressions, TaskEntity task,
			ActivityExecution execution) {
		super.handleAssignments(assigneeExpression, ownerExpression,
				candidateUserExpressions, candidateGroupExpressions, task,
				execution);
		List<ActGrant> actGrants = getEffectiveGrants(task
				.getProcessDefinitionId());
		if (!actGrants.isEmpty()) {
			if(StringUtils.isNotEmpty(task.getAssignee())){
				delegateAssignTask(actGrants, task);
			} else {
				delegateCandidateTaks(actGrants, task);
			}
		}
	}
	/**
	 * 处理直接分配到人的代理任务
	 * @param actGrants 当前流程的所有授权信息
	 * @param task 当前任务
	 */
	private void delegateAssignTask(List<ActGrant> actGrants,TaskEntity task){
		//说明是定义里面直接分配的
		if(taskDefinition.getAssigneeExpression() != null){
			for (ActGrant actGrant : actGrants) {
				if (actGrant.getOwner().equals(task.getAssignee())) {
					ActDelegate actDelegate = new ActDelegate();
					actDelegate.setDelegate(actGrant.getGrant());
					actDelegate.setDelegated(actGrant.getOwner());
					actDelegate.setTaskId(task.getId());
					SpringContextHolder.getBean(ActDelegateService.class).batchInsert(
							Lists.newArrayList(actDelegate));
					task.setAssignee(actGrant.getGrant());
					task.setOwner(actGrant.getOwner());
					return;
				}
			}
		}
	}

	/**
	 * 处理未直接分配的代理任务
	 * @param actGrants 当前流程的所有授权信息
	 * @param task 当前任务
	 */
	private void delegateCandidateTaks(List<ActGrant> actGrants,TaskEntity task){
		List<ActDelegate> delegates = Lists.newArrayList();
		Set<String> users = getAllCandidateUser(task);
		Set<String> newUsers = Sets.newHashSet();
		ActDelegate actDelegate = null;
		for (ActGrant actGrant : actGrants) {
			if (!users.contains(actGrant.getGrant())
					&& users.contains(actGrant.getOwner())
					&& !newUsers.contains(actGrant.getGrant())) {
				newUsers.add(actGrant.getGrant());
				actDelegate = new ActDelegate();
				actDelegate.setTaskId(task.getId());
				actDelegate.setDelegate(actGrant.getGrant());
				actDelegate.setDelegated(actGrant.getOwner());
				delegates.add(actDelegate);
			}
		}
		if(!newUsers.isEmpty()){
			SpringContextHolder.getBean(ActDelegateService.class).batchInsert(
					delegates);
			task.addCandidateUsers(newUsers);
		}
	}
	
	/**
	 * 查询流程processDefinitionId在当前有效的代办授权
	 * 
	 * @param processDefinitionId
	 *            流程定义ID
	 * @return
	 */
	private List<ActGrant> getEffectiveGrants(String processDefinitionId) {
		ActGrant actGrant = new ActGrant();
		actGrant.setProcessId(processDefinitionId);
		actGrant.setNowDate(new Date());
		actGrant.setRevoke("0");
		return SpringContextHolder.getBean(ActGrantService.class).findList(
				actGrant);
	}

	/**
	 * 获得所有有权处理的用户
	 * 
	 * @param task
	 * @return
	 */
	private Set<String> getAllCandidateUser(TaskEntity task) {
		Set<String> users = Sets.newHashSet();
		IdentityService identityService = SpringContextHolder
				.getBean(IdentityService.class);
		for (IdentityLink link : task.getCandidates()) {
			if (StringUtils.isNotEmpty(link.getUserId())) {
				users.add(link.getUserId());
			} else if (StringUtils.isNotEmpty(link.getGroupId())) {
				List<User> list = identityService.createUserQuery()
						.memberOfGroup(link.getGroupId()).list();
				for (User u : list) {
					users.add(u.getId());
				}
			}
		}
		return users;
	}

}
