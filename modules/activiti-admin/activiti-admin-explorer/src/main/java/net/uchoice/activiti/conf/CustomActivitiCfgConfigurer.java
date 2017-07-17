package net.uchoice.activiti.conf;

import net.uchoice.activiti.behavior.DelegateUserTaskActivityBehaviorFactory;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CustomActivitiCfgConfigurer implements ProcessEngineConfigurationConfigurer {
	@Autowired
	private Environment env;
	
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
    	String font = env.getProperty("activiti.png.font");
    	if(StringUtils.isNotBlank(font)){
    		processEngineConfiguration.setActivityFontName(font);
    		processEngineConfiguration.setLabelFontName(font);
    	}
    	processEngineConfiguration.setActivityBehaviorFactory(new DelegateUserTaskActivityBehaviorFactory());
    }
    
}
