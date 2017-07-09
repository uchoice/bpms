package net.uchoice.thymeleaf.conf;

import net.uchoice.thymeleaf.expression.CustomExpressionObjectDialect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.IDialect;

@Configuration
public class ThymeleafConfig {

	@Bean
	public IDialect customDialect() {
		return new CustomExpressionObjectDialect();
	}
	
	
	

}
