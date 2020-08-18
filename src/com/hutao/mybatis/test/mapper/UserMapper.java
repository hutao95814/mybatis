package com.hutao.mybatis.test.mapper;

import com.hutao.mybatis.annotation.Insert;
import com.hutao.mybatis.annotation.Param;
import com.hutao.mybatis.annotation.Select;
import com.hutao.mybatis.test.pojo.User;

/**
 * @Description: Mapper接口
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月11日
 */
public interface UserMapper {
	
	/**
	 * @Description:根据用户名查询用户  
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	@Select("select * from user where userName = #{userName}")
	public User selectUserByUserName(String userName);
	/**
	 * @Description:返回插入的用户 
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	@Insert("INSERT into user (userName,userAge,sex) VALUES(#{userName},#{userAge},#{sex})")
	public Integer insertUser(User user, @Param("userName") String userName , String sex);
}
