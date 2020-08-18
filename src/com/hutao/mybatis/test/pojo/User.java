package com.hutao.mybatis.test.pojo;

public class User {
	
	public String userName;
	
	public int userAge;

	public String sex;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getUserAge() {
		return userAge;
	}

	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	@Override
	public String toString() {
		return "User [userName=" + userName + ", userAge=" + userAge + ", sex=" + sex + "]";
	}
	
}
