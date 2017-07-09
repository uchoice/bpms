/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package net.uchoice.common.interceptor;

import net.uchoice.common.persistence.Page;
import net.uchoice.common.utils.Reflections;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.Properties;

/**
 * 数据库分页插件，只拦截查询语句.
 * 
 * @author poplar.yfyang / thinkgem
 * @version 2013-8-28
 */
@Intercepts({ @Signature(type = Executor.class, method = "query", args = {
		MappedStatement.class, Object.class, RowBounds.class,
		ResultHandler.class }) })
public class PaginationInterceptor implements Interceptor, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected static final String PAGE = "page";

	protected Log log = LogFactory.getLog(this.getClass());

	@Override
	public Object intercept(Invocation invocation) throws Throwable {

		final MappedStatement mappedStatement = (MappedStatement) invocation
				.getArgs()[0];
		// //拦截需要分页的SQL
		Object parameter = invocation.getArgs()[1];
		BoundSql boundSql = mappedStatement.getBoundSql(parameter);
		if (StringUtils.isBlank(boundSql.getSql())) {
			return null;
		}
		Object parameterObject = boundSql.getParameterObject();
		// 获取分页参数对象
		Page<Object> page = null;
		if (parameterObject != null) {
			page = convertParameter(parameterObject, page);
		}

		// 如果设置了分页对象，则进行分页
		if (page != null && page.getPageSize() != -1) {
			String originalSql = boundSql.getSql().trim();

			// 得到总记录数
			page.setCount(SQLHelper.getCount(originalSql, null,
					mappedStatement, parameterObject, boundSql, log));

			// 分页查询 本地化对象 修改数据库注意修改实现
			String pageSql = SQLHelper.generatePageSql(originalSql, page);
			invocation.getArgs()[2] = new RowBounds(RowBounds.NO_ROW_OFFSET,
					RowBounds.NO_ROW_LIMIT);
			BoundSql newBoundSql = new BoundSql(
					mappedStatement.getConfiguration(), pageSql,
					boundSql.getParameterMappings(),
					boundSql.getParameterObject());
			// 解决MyBatis 分页foreach 参数失效 start
			if (Reflections.getFieldValue(boundSql, "metaParameters") != null) {
				MetaObject mo = (MetaObject) Reflections.getFieldValue(
						boundSql, "metaParameters");
				Reflections.setFieldValue(newBoundSql, "metaParameters", mo);
			}
			// 解决MyBatis 分页foreach 参数失效 end
			MappedStatement newMs = copyFromMappedStatement(mappedStatement,
					new BoundSqlSqlSource(newBoundSql));

			invocation.getArgs()[0] = newMs;
		}
		// }
		return invocation.proceed();
	}

	/**
	 * 对参数进行转换和检查
	 * 
	 * @param parameterObject
	 *            参数对象
	 * @param page
	 *            分页对象
	 * @return 分页对象
	 * @throws NoSuchFieldException
	 *             无法找到参数
	 */
	@SuppressWarnings("unchecked")
	protected static Page<Object> convertParameter(Object parameterObject,
			Page<Object> page) {
		try {
			if (parameterObject instanceof Page) {
				return (Page<Object>) parameterObject;
			} else {
				return (Page<Object>) Reflections.getFieldValue(
						parameterObject, PAGE);
			}
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

	private MappedStatement copyFromMappedStatement(MappedStatement ms,
			SqlSource newSqlSource) {
		MappedStatement.Builder builder = new MappedStatement.Builder(
				ms.getConfiguration(), ms.getId(), newSqlSource,
				ms.getSqlCommandType());
		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		if (ms.getKeyProperties() != null) {
			for (String keyProperty : ms.getKeyProperties()) {
				builder.keyProperty(keyProperty);
			}
		}
		builder.timeout(ms.getTimeout());
		builder.parameterMap(ms.getParameterMap());
		builder.resultMaps(ms.getResultMaps());
		builder.cache(ms.getCache());
		builder.useCache(ms.isUseCache());
		return builder.build();
	}

	public static class BoundSqlSqlSource implements SqlSource {
		BoundSql boundSql;

		public BoundSqlSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}

		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}
}
