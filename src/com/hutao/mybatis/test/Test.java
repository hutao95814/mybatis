package com.hutao.mybatis.test;

import com.hutao.mybatis.sqlSession.SqlSession;
import com.hutao.mybatis.test.mapper.UserMapper;
import com.hutao.mybatis.test.pojo.User;

public class Test {
	public static void main(String[] args) {
		UserMapper userMapper = SqlSession.getMapper(UserMapper.class);
		
		//public Integer insertUser(User user, @Param("userName") String userName , String sex);
		User user = new User();//传入包装对象
		user.setUserAge(24);
		String userName = "胡涛";//传入有注解的
		String sex = "男";//传入没有注解的
		System.out.println("数据更新总数："+userMapper.insertUser(user, userName,sex));
		
		User zhagnhuan = new User();//只传入包装对象
		zhagnhuan.setUserName("张欢");
		zhagnhuan.setUserAge(22);
		zhagnhuan.setSex("女");
		System.out.println("数据更新总数："+userMapper.insertUser(zhagnhuan, null,null));
		
		System.out.println("查询结果："+userMapper.selectUserByUserName("张欢"));
		
	}
}
