package com.hutao.mybatis.aop;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:Sql语句字符处理类
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月13日
 */
public class SqlStringUtils {
	
	/**
	 * @Description:提取占位符中的数据
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月13日
	 */
	public static List<String> getPlaceholder(String sql) {
		List<String> listParam = new ArrayList<>();
		Pattern p=Pattern.compile("#\\{(.*?)}");
		Matcher m=p.matcher(sql);
		while(m.find()){
			listParam.add(m.group(1));
		}
		return listParam;
	}
	
	/**
	 * @Description:将占位符的数据替换成问号的SQL语句
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月13日
	 */
	public static String getQuestionSql(String sql, List<String> parameterName) {
		for (int i = 0; i < parameterName.size(); i++) {
			String string = parameterName.get(i);
			sql = sql.replace("#{" + string + "}", "?");
		}
		return sql;
	}
}
