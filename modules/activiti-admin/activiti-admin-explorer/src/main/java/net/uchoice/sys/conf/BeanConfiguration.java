package net.uchoice.sys.conf;

import javax.validation.Validator;

import net.uchoice.common.interceptor.PaginationInterceptor;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;


@Configuration
public class BeanConfiguration {

	@Bean
	public Validator validator(){
		return new LocalValidatorFactoryBean();
	}
	@Bean
	public Interceptor paginationInterceptor(){
		return new PaginationInterceptor();
	}
	
}
