package com.hutao.mybatis.pool.database.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.ResourceBundle;

import com.hutao.mybatis.pool.database.pojo.DbProperties;
import com.hutao.mybatis.pool.database.service.DbPoolService;
import com.hutao.mybatis.pool.database.service.impl.DbPoolServiceImpl;

/**
 * @Description:数据库连接池管理
 * @author hutao
 * @mail hutao_2017@aliyun.com
 * @date 2020年07月08日
 */
public class DbPoolManager {
	
	private static String sourcePath = "com/hutao/resources/database";
	
	/**
	 * 数据库连接池配置属性
	 */
	private static DbProperties properties = null;
	
	/**
	 * 数据库连接池接口
	 */
	private static DbPoolService connectionPool = null;
	
	
	/**
	 * 双重检查机制静态加载连接池
	 */
	static {
		try {
			if(properties == null) {
				synchronized(DbPoolManager.class) {
					if(properties == null) {
						properites2Object();
						connectionPool = new DbPoolServiceImpl(properties);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @Description:数据库连接池database配置文件映射到java对象
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年07月08日
	 */
	private static void properites2Object() throws NoSuchFieldException, IllegalAccessException {
		properties = new DbProperties();
		ResourceBundle resourceBundle = ResourceBundle.getBundle(sourcePath);
		//获取资源文件中所有的key
		Enumeration<String> keys = resourceBundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			//反射获取类中的属性字段
			Field field= DbProperties.class.getDeclaredField(key);
			//属性字段的类型
			Type genericType = field.getGenericType();
			//属性设置可访问
			field.setAccessible(true);
			//根据key读取对应的value值
			String value = resourceBundle.getString(key);
			if("int".equals(genericType.getTypeName())) {
				//反射给属性赋值
				field.set(properties, Integer.parseInt(value));
			}else if("long".equals(genericType.getTypeName())) {
				field.set(properties, Long.parseLong(value));
			}else if("java.lang.String".equals(genericType.getTypeName())) {
				field.set(properties,value);
			}
		}
	}
	
	/**
	 * @Description:获取连接
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年07月09日
	 */
	public static Connection getConnection() {
		return connectionPool.getConnection();
	}

	/**
	 * @Description:释放连接
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年07月09日
	 */
	public static void releaseConnection(Connection connection) {
		connectionPool.releaseConnection(connection);
	}
}
