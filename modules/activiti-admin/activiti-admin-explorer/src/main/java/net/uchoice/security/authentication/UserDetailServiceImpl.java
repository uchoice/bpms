package net.uchoice.security.authentication;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	private IdentityService identityService;

	@Override
	public UserDetails loadUserByUsername(String name)
			throws UsernameNotFoundException {
		List<User> users = identityService.createUserQuery().userId(name)
				.list();
		if (users == null || users.isEmpty()) {
			throw new UsernameNotFoundException("该用户不存在");
		}
		List<Group> groups = identityService.createGroupQuery()
				.groupMember(name).list();
		List<GrantedAuthority> authorities = Lists.newArrayList();
		for (Group g : groups) {
			authorities.add(new SimpleGrantedAuthority(g.getName()));
		}
		return new CustomUserDetails((UserEntity) users.get(0), authorities);
	}

}
