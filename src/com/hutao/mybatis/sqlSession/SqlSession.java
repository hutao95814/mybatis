package com.hutao.mybatis.sqlSession;

import java.lang.reflect.Proxy;

import com.hutao.mybatis.aop.MybatisInvocationHandler;

/**
 * @Description:SqlSession会话  
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月11日
 */
public class SqlSession {

	/**
	 * @Description: 返回指定的Mapper代理對象
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月13日
	 */
	public static <T> T getMapper(Class<?> classz) {
		return (T) Proxy.newProxyInstance(
				classz.getClassLoader(), 
				new Class[] { classz },
				new MybatisInvocationHandler(classz
			)
		);
	}
}