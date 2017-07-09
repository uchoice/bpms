package net.uchoice.thymeleaf.expression;

import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

public class CustomExpressionObjectDialect implements IExpressionObjectDialect {

	@Override
	public String getName() {
		return "th";
	}

	@Override
	public IExpressionObjectFactory getExpressionObjectFactory() {
		return new CustomExpressionObjectFactory();
	}

}
