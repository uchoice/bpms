package net.uchoice.security.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthenticationProvider authenticationProvider;

	@Autowired
	private UserDetailsService userDetailService;

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/static/**", "/**/favicon.ico");
	}

	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
				.antMatchers("/theme/*", "/error")
				.permitAll()
				.anyRequest()
				.authenticated()
				.and().formLogin()
				.loginPage("/sys/login").defaultSuccessUrl("/sys/index")
				.permitAll().and().logout().permitAll()
				.and().headers().frameOptions().disable();
		/*
		 * .rememberMe() .rememberMeServices( new
		 * TokenBasedRememberMeServices("rememberMe",
		 * userDetailService)).key("rememberMe") .tokenValiditySeconds(60 * 60 *
		 * 24 * 15).and()
		 */
	}

	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
		auth.authenticationProvider(authenticationProvider);
	}

}
