package net.uchoice.activiti.controller.management;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uchoice.common.persistence.Page;
import net.uchoice.common.utils.Digests;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 用户、组控制器 User: henry
 */
@Controller
@RequestMapping("/management/identity")
public class IdentityController {

	@Autowired
	private IdentityService identityService;

	private static final String[] INNER_GROUPS = { "super", "admin" };

	private static final String[] INNER_USERS = { "uchoice" };

	/**
	 * 组列表
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("group/list")
	public ModelAndView groupList(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mav = new ModelAndView(
				"/activiti/management/identity/groupList");
		Page<Group> page = new Page<Group>(request, response);
		GroupQuery groupQuery = identityService.createGroupQuery();
		page.setCount(groupQuery.count());
		List<Group> groupList = groupQuery.listPage(page.getFirstResult(),
				page.getMaxResults());
		page.setResult(groupList);
		mav.addObject("page", page);
		return mav;
	}

	/**
	 * 保存Group
	 *
	 * @return
	 */
	@RequestMapping(value = "group/save", method = RequestMethod.POST)
	public String saveGroup(@RequestParam("groupId") String groupId,
			@RequestParam("groupName") String groupName,
			@RequestParam("type") String type,
			RedirectAttributes redirectAttributes) {
		Group group = identityService.createGroupQuery().groupId(groupId)
				.singleResult();
		if (group == null) {
			group = identityService.newGroup(groupId);
		}
		group.setName(groupName);
		group.setType(type);
		identityService.saveGroup(group);
		redirectAttributes.addFlashAttribute("message", "成功添加组[" + groupName
				+ "]");
		return "redirect:/management/identity/group/list";
	}

	/**
	 * 删除Group
	 */
	@RequestMapping(value = "group/delete/{groupId}", method = RequestMethod.GET)
	public String deleteGroup(@PathVariable("groupId") String groupId,
			RedirectAttributes redirectAttributes) {
		if (ArrayUtils.contains(INNER_GROUPS, groupId)) {
			redirectAttributes.addFlashAttribute("errorMsg", "组[" + groupId
					+ "]属于系统内置组，不可删除!");
			return "redirect:/management/identity/group/list";
		}

		identityService.deleteGroup(groupId);
		redirectAttributes.addFlashAttribute("message", "成功删除组[" + groupId
				+ "]");
		return "redirect:/management/identity/group/list";
	}

	/**
	 * 用户列表
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("user/list")
	public ModelAndView userList(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mav = new ModelAndView(
				"/activiti/management/identity/userList");

		Page<User> page = new Page<User>(request, response);

		UserQuery userQuery = identityService.createUserQuery();
		page.setCount(userQuery.count());
		List<User> userList = userQuery.listPage(page.getFirstResult(),
				page.getMaxResults());

		// 查询每个人的分组，这样的写法比较耗费性能、时间，仅供读者参考
		Map<String, String[]> groupOfUsers = new HashMap<String, String[]>();
		String[] temp = null;
		for (User user : userList) {
			List<Group> groupList = identityService.createGroupQuery()
					.groupMember(user.getId()).list();
			temp = new String[] { "", "" };
			for (Group g : groupList) {
				temp[0] += g.getId() + ",";
				temp[1] += g.getName() + ",";
			}
			groupOfUsers.put(user.getId(), temp);
		}

		page.setResult(userList);
		mav.addObject("page", page);
		mav.addObject("groupOfUsers", groupOfUsers);

		// 读取所有组
		List<Group> groups = identityService.createGroupQuery().list();
		mav.addObject("allGroup", groups);

		return mav;
	}

	/**
	 * 保存User
	 *
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value = "user/save", method = RequestMethod.POST)
	public String saveUser(
			@RequestParam("userId") String userId,
			@RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "email", required = false) String email,
			RedirectAttributes redirectAttributes) {
		User user = identityService.createUserQuery().userId(userId)
				.singleResult();
		if (user == null) {
			user = identityService.newUser(userId);
		}
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		if (StringUtils.isNotBlank(password)) {
			user.setPassword(Digests.entryptPassword(password));
		}
		identityService.saveUser(user);
		redirectAttributes.addFlashAttribute("message", "成功添加用户[" + firstName
				+ lastName + "]");
		return "redirect:/management/identity/user/list";
	}

	/**
	 * 删除User
	 */
	@RequestMapping(value = "user/delete/{userId}", method = RequestMethod.GET)
	public String deleteUser(@PathVariable("userId") String userId,
			RedirectAttributes redirectAttributes) {
		if (ArrayUtils.contains(INNER_USERS, userId)) {
			redirectAttributes.addFlashAttribute("errorMsg", "用户[" + userId
					+ "]属于系统内置用户，不可删除!");
			return "redirect:/management/identity/user/list";
		}

		identityService.deleteUser(userId);
		redirectAttributes.addFlashAttribute("message", "成功删除用户[" + userId
				+ "]");
		return "redirect:/management/identity/user/list";
	}

	/**
	 * 为用户设置所属组
	 * 
	 * @param userId
	 * @param groupIds
	 * @return
	 */
	@RequestMapping(value = "group/set", method = RequestMethod.POST)
	public String groupForUser(@RequestParam("userId") String userId,
			@RequestParam("group") String[] groupIds,
			RedirectAttributes redirectAttributes) {

		if (ArrayUtils.contains(INNER_USERS, userId)) {
			redirectAttributes.addFlashAttribute("errorMsg", "用户[" + userId
					+ "]属于系统内置用户，不可更改!");
			return "redirect:/management/identity/user/list";
		}

		List<Group> groupInDb = identityService.createGroupQuery()
				.groupMember(userId).list();
		for (Group group : groupInDb) {
			identityService.deleteMembership(userId, group.getId());
		}
		for (String group : groupIds) {
			identityService.createMembership(userId, group);
		}
		return "redirect:/management/identity/user/list";
	}

}
