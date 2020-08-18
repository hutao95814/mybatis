package com.hutao.mybatis.pool.database.pojo;

/**
 * @Description:数据库连接池属性信息
 * @author hutao
 * @mail hutao_2017@aliyun.com
 * @date 2020年07月08日
 */
public class DbProperties {

	/* 链接属性 */
	private String driverName;

	private String url;

	private String userName;

	private String passWord;

	private String poolName;

	/**
	 * 空闲池，最小连接数
	 */
	private int minFreeConnections;

	/**
	 * 空闲池，最大连接数
	 */
	private int maxFreeConnections;

	/**
	 * 初始连接数
	 */
	private int initFreeConnections;

	/**
	 * 重试获得连接的频率  毫秒
	 */
	private long retryConnectionTimeOut;

	/**
	 * 最大允许的连接数
	 */
	private int maxConnections;

	/**
	 * 连接超时时间
	 */
	private long connectionTimeOut;

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public int getMinFreeConnections() {
		return minFreeConnections;
	}

	public void setMinFreeConnections(int minFreeConnections) {
		this.minFreeConnections = minFreeConnections;
	}

	public int getMaxFreeConnections() {
		return maxFreeConnections;
	}

	public void setMaxFreeConnections(int maxFreeConnections) {
		this.maxFreeConnections = maxFreeConnections;
	}

	public int getInitFreeConnections() {
		return initFreeConnections;
	}

	public void setInitFreeConnections(int initFreeConnections) {
		this.initFreeConnections = initFreeConnections;
	}

	public long getRetryConnectionTimeOut() {
		return retryConnectionTimeOut;
	}

	public void setRetryConnectionTimeOut(long retryConnectionTimeOut) {
		this.retryConnectionTimeOut = retryConnectionTimeOut;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public long getConnectionTimeOut() {
		return connectionTimeOut;
	}

	public void setConnectionTimeOut(long connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}

	@Override
	public String toString() {
		return "DbProperties [driverName=" + driverName + ", url=" + url + ", userName=" + userName + ", passWord="
				+ passWord + ", poolName=" + poolName + ", minFreeConnections=" + minFreeConnections
				+ ", maxFreeConnections=" + maxFreeConnections + ", initFreeConnections=" + initFreeConnections
				+ ", retryConnectionTimeOut=" + retryConnectionTimeOut + ", maxConnections=" + maxConnections
				+ ", connectionTimeOut=" + connectionTimeOut + "]";
	}
}
