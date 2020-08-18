package com.hutao.mybatis.pool.database.service.impl;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.hutao.mybatis.pool.database.pojo.DbProperties;
import com.hutao.mybatis.pool.database.service.DbPoolService;


/**
 * @Description:数据库连接池实现
 * @author hutao
 * @mail hutao_2017@aliyun.com
 * @date 2020年07月09日
 */
public class DbPoolServiceImpl implements DbPoolService {
	
	/**
	 * 存放空闲连接的容器，除了可以使用并发队列，也可以使用线程安全的集合Vector
	 */
	private BlockingQueue<Connection> freeConnection = null;
	/**
	 * 存放活动连接的容器，除了可以使用并发队列，也可以使用线程安全的集合Vector
	 */
	private BlockingQueue<Connection> activeConnection = null;
	
	/**
	 * 存放映射的属性配置文件
	 */
	private DbProperties dDbProperties;

	
	public DbPoolServiceImpl(DbProperties dDbProperties) throws Exception {
		// 获取配置文件信息
		this.dDbProperties = dDbProperties;
		freeConnection =  new LinkedBlockingQueue<>(dDbProperties.getMaxFreeConnections());
		activeConnection = new LinkedBlockingQueue<>(dDbProperties.getMaxConnections());
		init();
	}

	/**
	 * @Description:初始化空闲线程池
	 * @author hutao
	 * @throws Exception 
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年07月08日
	 */
	private void init() throws Exception {
		System.out.println("初始化线程池开始，线程池配置属性："+dDbProperties);
		if (dDbProperties == null) {
			throw new  Exception("连接池配置属性对象不能为空");
		}
		//获取连接池配置文件中初始化连接数
		for (int i = 0; i < dDbProperties.getInitFreeConnections(); i++) {
			//创建Connection连接
			Connection newConnection = newConnection();
			if (newConnection != null) {
				//将创建的新连接放入到空闲池中
				freeConnection.add(newConnection);
			}
		}
		System.out.println("初始化线程池结束，初始化线程数："+dDbProperties.getInitFreeConnections());
	}

	/**
	 * @Description:
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年07月08日
	 */
	private synchronized Connection newConnection() {
		try {
			Class.forName(dDbProperties.getDriverName());
			return DriverManager.getConnection(dDbProperties.getUrl(), dDbProperties.getUserName(),dDbProperties.getPassWord());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @Description:判断连接是否可用，可用返回true
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年07月08日
	 */
	@Override
	public boolean isAvailable(Connection connection) {
		try {
			if (connection == null || connection.isClosed()) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
		
	}

	/**
	 * @Description:使用重复利用机制获取连接：如果总连接未超过最大连接，则从空闲连接池获取连接或者创建一个新的连接，如果超过最大连接，则等待一段时间以后，继续获取连接
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年07月08日
	 */
	@Override
	public synchronized Connection getConnection() {
		Connection connection = null;
		//空闲连接和活动连接的总数加起来 小于 最大配置连接
		System.out.println("当前空闲连接总数："+freeConnection.size()+" 当前活动连接总数"+activeConnection.size()+", 配置最大连接数："+ dDbProperties.getMaxConnections());
		if (freeConnection.size()+activeConnection.size() < dDbProperties.getMaxConnections()) {
			//空闲连接池，是否还有还有连接，有就取出来，没有就创建一个新的。
			if (freeConnection.size() > 0) {
				connection = freeConnection.poll();
				System.out.println("从空闲线程池取出线程："+connection+"当前空闲线程总数："+freeConnection.size());
			} else {
				connection = newConnection();
				System.out.println("空闲连接池没有连接，创建连接"+connection);
			}
			//拿到的连接可用，就添加活动连接池，否则就递归继续找下一个
			boolean available = isAvailable(connection);
			if (available) {
				activeConnection.add(connection);
			} else {
				connection = getConnection();
			}

		} else {
			System.out.println("当前连接数已达到最大连接数，等待"+dDbProperties.getRetryConnectionTimeOut()+"ms以后再试");
			try {
				wait(dDbProperties.getRetryConnectionTimeOut());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			connection = getConnection();
		}
		return connection;

	}

	/**
	 * @Description:使用可回收机制释放连接：如果连接可用，并且空闲连接池没有满，则把连接归还到空闲连接池，否则关闭连接
	 * @author hutao
	 * @mail hutao_2017@aliyun.com
	 * @date 2020年07月09日
	 */
	@Override
	public synchronized void releaseConnection(Connection connection) {
		try {
			if (isAvailable(connection) && freeConnection.size() < dDbProperties.getMaxFreeConnections()) {
				freeConnection.add(connection);
				System.out.println("空闲线程池未满，归还连接"+connection);
				
			} else {
				connection.close();
				System.out.println("空闲线程池已满，关闭连接"+connection);
			}
			activeConnection.remove(connection);
			notifyAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
