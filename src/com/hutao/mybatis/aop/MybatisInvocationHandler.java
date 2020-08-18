package com.hutao.mybatis.aop;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hutao.mybatis.annotation.Delete;
import com.hutao.mybatis.annotation.Insert;
import com.hutao.mybatis.annotation.Param;
import com.hutao.mybatis.annotation.Select;
import com.hutao.mybatis.annotation.Update;
import com.hutao.mybatis.pool.database.manager.DbPoolManager;

/**
 * @Description:使用JDK动态代理方式实现APO
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月11日
 */
public class MybatisInvocationHandler implements InvocationHandler {

	/**
	 * 需要被代理的对象
	 */
	private Object object;

	public MybatisInvocationHandler(Object object) {
		this.object = object;
	}

	/**
	 * @Description:执行代理
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月11日
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("需要被代理的接口的方法：" + object+method.getName());
		Insert insert = method.getDeclaredAnnotation(Insert.class);
		if (insert != null) {
			return excuteInsert(insert, proxy, method, args);
		}
		Delete delete = method.getDeclaredAnnotation(Delete.class);
		if (delete != null) {
			return excuteDelete(delete, proxy, method, args);
		}
		Select select = method.getDeclaredAnnotation(Select.class);
		if (select != null) {
			return excuteSelect(select, proxy, method, args);
		}
		Update update = method.getDeclaredAnnotation(Update.class);
		if (update != null) {
			return excuteUpdate(update, proxy, method, args);
		}
		return null;
	}

	/**
	 * @Description:Insert操作
	 * @author hutao
	 * @throws SQLException 
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	private int excuteInsert(Insert insert, Object proxy, Method method, Object[] args) throws SQLException {
		String insertSql = insert.value();
		System.out.println("需要执行的SQL语句：" + insertSql);
		List<String> placeholders = SqlStringUtils.getPlaceholder(insertSql);
		System.out.println("提取占位符的变量：" + placeholders);
		String questionSql = SqlStringUtils.getQuestionSql(insertSql, placeholders);
		System.out.println("占位符变量替换为问号：" + questionSql);
		Map<String, Object> methodParam = getMethodParam(method, args);
		System.out.println("执行SQL语句所需要的参数：" + methodParam);
		return excuteUpdateSql(questionSql,placeholders, methodParam);
	}


	/**
	 * @Description: Delete操作
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	private Object excuteDelete(Delete delete, Object proxy, Method method, Object[] args) {

		return args;
	}

	/**
	 * @Description:Select操作
	 * @author hutao
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InstantiationException 
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	private Object excuteSelect(Select select, Object proxy, Method method, Object[] args) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		String insertSql = select.value();
		System.out.println("需要执行的SQL语句：" + insertSql);
		List<String> placeholders = SqlStringUtils.getPlaceholder(insertSql);
		System.out.println("提取占位符的变量：" + placeholders);
		String questionSql = SqlStringUtils.getQuestionSql(insertSql, placeholders);
		System.out.println("占位符变量替换为问号：" + questionSql);
		Map<String, Object> methodParam = getMethodParam(method, args);
		System.out.println("执行SQL语句所需要的参数：" + methodParam);
		return excuteSelectSql(method,questionSql,placeholders, methodParam);
	}

	/**
	 * @Description:Update操作
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	private Object excuteUpdate(Update update, Object proxy, Method method, Object[] args) {

		return args;
	}

	/**
	 * @Description:执行SQL语句：select语句
	 * @author hutao
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InstantiationException 
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月14日
	 */
	private Object excuteSelectSql(Method method,String questionSql, List<String> placeholders,Map<String, Object> methodParam) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		Connection connection = DbPoolManager.getConnection();
		PreparedStatement ps = connection.prepareStatement(questionSql);
		for (int i = 1; i <= placeholders.size(); i++) {
			ps.setObject(i, methodParam.get(placeholders.get(i-1)));
		}
		ResultSet executeQuery = ps.executeQuery();
		DbPoolManager.releaseConnection(connection);
		if (!executeQuery.next()) {
			return null;
		}
		executeQuery.previous();
		Class<?> returnType = method.getReturnType();
		Object object = returnType.newInstance();
		while (executeQuery.next()) {
			// 获取当前所有的属性
			Field[] declaredFields = returnType.getDeclaredFields();
			for (Field field : declaredFields) {
				String fieldName = field.getName();
				Object fieldValue = executeQuery.getObject(fieldName);
				field.setAccessible(true);
				field.set(object, fieldValue);
			}
		
		}
		return object;
	}
	
	/**
	 * @Description:执行SQL语句：update语句，select语句，delete语句
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月14日
	 */
	private int excuteUpdateSql(String questionSql, List<String> placeholders,Map<String, Object> methodParam) throws SQLException {
		Connection connection = DbPoolManager.getConnection();
		PreparedStatement ps = connection.prepareStatement(questionSql);
		for (int i = 1; i <= placeholders.size(); i++) {
			ps.setObject(i, methodParam.get(placeholders.get(i-1)));
		}
		int executeUpdate = ps.executeUpdate();
		DbPoolManager.releaseConnection(connection);
		return executeUpdate;
	}
	
	/**
	 * @Description:获取方法参数:本例只举出普通字符串数据，普通包装类，字符串带参数注解，
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月13日
	 */
	private Map<String, Object> getMethodParam(Method method, Object[] args) {
		Map<String, Object> paramsMap = new ConcurrentHashMap<>();
		// 获取方法上的参数
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Object paramValue = args[i];
			if(paramValue!=null) {
				Class<? extends Object> class1 = paramValue.getClass();
				Parameter parameter = parameters[i];
				//参数带注解的
				Param param = parameter.getDeclaredAnnotation(Param.class);
				if(param!=null && paramValue!=null) {
					String paramName = param.value();
					paramsMap.put(paramName, paramValue);
				}else if("java.lang.String".equals(class1.getTypeName()) && args[i]!=null) {
					//不带参数注解，并且为String类型的,这里只做演示，其余的数据类型不在编写
					paramsMap.put(parameters[i].getName(),args[i]);
				}else {
					//不带参数注解，并且为包装类型的
					Field[] declaredFields = class1.getDeclaredFields();
					for (Field field : declaredFields) {
						try {
							Object fieldValue = field.get(paramValue);
							if(field.getName()!=null && fieldValue!=null) {
								paramsMap.put(field.getName(),fieldValue);
							}
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
						
					}
				}
			}
		}
		return paramsMap;
	}
}
