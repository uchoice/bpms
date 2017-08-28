package net.uchoice.thymeleaf.expression;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

public class CustomExpressionObjectFactory implements IExpressionObjectFactory {

	public static final String COOKIES_OBJECT_NAME = "cookies";

	public static final String DICTS_OBJECT_NAME = "dicts";
	
	public static final String USERS_OBJECT_NAME = "users";
	
	public static final String CACHE_OBJECT_NAME = "caches";

	public static final Set<String> ALL_EXPRESSION_OBJECT_NAMES;

	private static final Cookies COOKIES_EXPRESSION_OBJECT = new Cookies();
	private static final Dicts DICTS_EXPRESSION_OBJECT = new Dicts();
	private static final Users USERS_EXPRESSION_OBJECT = new Users();
	private static final Caches CACHE_EXPRESSION_OBJECT = new Caches();

	static {

		final Set<String> allExpressionObjectNames = new LinkedHashSet<String>();
		// allExpressionObjectNames.addAll(StandardExpressionObjectFactory.COOKIES_OBJECT_NAME);
		allExpressionObjectNames.add(COOKIES_OBJECT_NAME);
		allExpressionObjectNames.add(DICTS_OBJECT_NAME);
		allExpressionObjectNames.add(USERS_OBJECT_NAME);
		allExpressionObjectNames.add(CACHE_OBJECT_NAME);
		ALL_EXPRESSION_OBJECT_NAMES = Collections
				.unmodifiableSet(allExpressionObjectNames);

	}

	@Override
	public Set<String> getAllExpressionObjectNames() {
		return ALL_EXPRESSION_OBJECT_NAMES;
	}

	@Override
	public Object buildObject(IExpressionContext context,
			String expressionObjectName) {
		if (COOKIES_OBJECT_NAME.equals(expressionObjectName)) {
			return COOKIES_EXPRESSION_OBJECT;
		}
		if (DICTS_OBJECT_NAME.equals(expressionObjectName)) {
			return DICTS_EXPRESSION_OBJECT;
		}
		if (USERS_OBJECT_NAME.equals(expressionObjectName)) {
			return USERS_EXPRESSION_OBJECT;
		}
		if (CACHE_OBJECT_NAME.equals(expressionObjectName)) {
			return CACHE_EXPRESSION_OBJECT;
		}
		return null;
	}

	@Override
	public boolean isCacheable(String expressionObjectName) {
		return false;
	}

}
