package net.uchoice.security.authentication;

import java.util.Collection;
import java.util.List;

import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails extends UserEntity implements UserDetails {

	private List<GrantedAuthority> authorities;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomUserDetails(UserEntity user, List<GrantedAuthority> authorities) {
		super();
		BeanUtils.copyProperties(user, this);
		this.authorities = authorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getUsername() {
		return getId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
