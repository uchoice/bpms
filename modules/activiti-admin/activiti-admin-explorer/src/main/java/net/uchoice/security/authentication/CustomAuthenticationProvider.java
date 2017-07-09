package net.uchoice.security.authentication;

import java.util.Collection;

import net.uchoice.common.utils.Digests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private UserDetailsService userDetailService;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		String username = authentication.getName();
		String password = (String) authentication.getCredentials();
		UserDetails user = (UserDetails) userDetailService
				.loadUserByUsername(username);
		if (user == null) {
			throw new BadCredentialsException("该用户不存在");
		}

		// 加密过程在这里体现
		if (!Digests.validatePassword(password, user.getPassword())) {
			throw new BadCredentialsException("用户密码错误");
		}

		if (!user.isEnabled()) {
			throw new BadCredentialsException("用户未启用");
		}
		if (!user.isAccountNonLocked()) {
			throw new BadCredentialsException("用户被锁定");
		}
		if (!user.isAccountNonExpired()) {
			throw new BadCredentialsException("用户已过期");
		}

		Collection<? extends GrantedAuthority> authorities = user
				.getAuthorities();
		return new UsernamePasswordAuthenticationToken(user,
				user.getPassword(), authorities);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

}
