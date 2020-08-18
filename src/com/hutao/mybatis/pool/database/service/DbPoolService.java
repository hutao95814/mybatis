package com.hutao.mybatis.pool.database.service;

import java.sql.Connection;

/**
 * @Description:数据库连接池  
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月08日
 */
public interface DbPoolService {
	
	/**
	 * @Description:判断连接是否可用，可用返回true
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年7月08日
	 */
	public boolean isAvailable(Connection connection);

	/**
	 * @Description:使用重复利用机制获取连接
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年7月08日
	 */
	public Connection getConnection();

	/**
	 * @Description:使用可回收机制释放连接
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年7月09日
	 */
	public void releaseConnection(Connection connection);
}
