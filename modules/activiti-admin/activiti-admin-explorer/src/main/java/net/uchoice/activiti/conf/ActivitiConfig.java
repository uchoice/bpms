package net.uchoice.activiti.conf;

import net.uchoice.activiti.servlet.JsonpCallbackFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiConfig {

	
	@Bean
	public JsonpCallbackFilter filter(){
	    return new JsonpCallbackFilter();
	}
	
}
