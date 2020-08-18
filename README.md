据库连接池参考：[如何自己手写一套数据库连接池?](https://blog.csdn.net/m0_37892044/article/details/107239656)
完整代码结构如下
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200717150015908.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
# 1.原生mybatis
## 1.1.原生mybatis的使用
定义一个mapper
```java
public interface UserMapper {
	@Select("select * from user where userName =  #{userName }")
	public queryUserByUserName(@Param("userName") String userName);
}
```
编写一个mapper配置工具类
此处略，详情参考[单独使用MyBatis时，mybatis对数据库如何进行管理](https://blog.csdn.net/m0_37892044/article/details/106590047)
使用mybatis

```java
UserMapper userMapper= MybatisConfig.openSqlSession(true).getMapper(UserMapper.class);
User user = userMapper.queryUserByUserName("hutao");
```
## 1.2.原生mybatis使用的疑问
### 1.2.1.疑问1：怎么去调用没有实现类的接口
**疑问1：**

UserMapper是一个接口，压根没有实现类，为什么我们调用接口的方法，能够执行？

### 1.2.2.疑问2：怎么找到SQL
**疑问2：**

如果接口的方法能够以某种特殊方式来被执行，那么执行接口的方法的时候，是怎么找到我们需要执行的SQL语句的？

### 1.2.3.疑问3：SQL的参数怎么绑定
**疑问3：**

当我们的SQL语句需要传入参数时，我们的参数是怎么绑定到SQL语句中的？

### 1.2.4.疑问4：怎么执行SQL
这个最好解决了。这里就不啰嗦了
### 1.2.5.疑问5：怎么返回SQL的执行结果
**疑问5：**

我们的查询结果怎么被映射到java对象中去的？

如果我们解决了如上5个问题，那么mybatis的核心我们也就理解了。
其余的一些问题，在不本文讨论范文之内。
## 1.3.手写mybtis原理示意图
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200717153259752.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
**流程说明：**
1、拦截Mapper接口
当执行如下代码时候
```java
userMapper.insertUser(user, userName,sex);
```
我们需要拦截到这个如下这个接口

```java
@Insert("INSERT into user (userName,userAge,sex) VALUES(#{userName},#{userAge},#{sex})")
public Integer insertUser(User user, @Param("userName") String userName , String sex);
```
2、解析这个接口的@Insert注解，我们就可以拿到

```java
INSERT into user (userName,userAge,sex) VALUES(#{userName},#{userAge},#{sex}
```
并且把Mybatis的SQL转化成JDBC执行的SQL

```java
INSERT into user (userName,userAge,sex) VALUES(?,?,?);
```

3、解析insertUser这个方法的入参，我们就可以拿到入参

```java
User user,
@Param("userName") String userName 
String sex
```
4、从方法的入参中，找到我们执行SQL需要的绑定参数
执行上述的SQL中，我们需要在方法的入参中，
```java
User user,
@Param("userName") String userName 
String sex
```
找到这三个变量
```java
#{userName},#{userAge},#{sex}
```
5、有了JDBC可以执行SQL，以及JDBC需要的绑定参数，就可以通过JDBC操作数据库了。
# 2.代理设计模式
想要调用没有实现类的接口，有很多种方式，这里我们用代理模式思路来实现。

## 2.1.什么是代理模式
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713123239559.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
```java
1、通过代理控制对象的访问，可以详细访问某个对象的方法；
2、它可以在这个方法调用前处理，或调用后处理。 
3、它是Spring AOP面向切面编程的核心。
```
代理设计模式分为静态代理和动态代理。
## 2.2.静态代理
在程序运行前就已经存在代理类的字节码文件（class文件），代理类和委托类的关系在运行前就确定了。也就是说，使用静态代理方式，需要我们自己去实现代理类的代码。(这种方式代码冗余量和维护工作量大的一比，谁爱用，谁用，反正我不用)。
## 2.3.动态代理
动态代理最常见的有JDK接口动态代理，CGLB生成子类代理。
**CGLB生成子类代理**

```java
利用asm开源包，对代理对象类的class文件加载进来，通过修改其字节码生成子类来处理。 
```
**JDK接口动态代理**
```java
根据类加载器和接口创建代理类（此代理类是接口的实现类）
```
# 3.代理Mapper接口解决疑问1
有了代理设计设计模式的概念以后，我们使用JDK接口动态代理的方法，来代理Mapper接口。这样就能执行Mapper接口中定义的方法了，也就解决了我们的第一个问题。
**疑问1：**
```java
UserMapper是一个接口，压根没有实现类，为什么我们调用接口的方法，能够执行？
```
**解决思路**
```java
面向接口生成代理。
```

代码结构如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713131114189.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
### 3.1 .调用代理对象
```java
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

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("使用JDK动态代理拦截Mapper接口");
		System.out.println("需要被代理的接口："+object);
		System.out.println("需要执行的方法："+method.getName());
		return null;
	}
}
```
### 3.2 .动态生成代理对象

```java
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

```

### 3.3 .定义Mapper接口
```java
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
	 * @date 2020年7月13日
	 */
	public User selectUserByUserName(String userName);
}
```
### 3.4 .代理效果演示
通过JDK的动态代理方式，我们就解决了我们的疑问1。
```java
public class Test {
	public static void main(String[] args) {
		UserMapper userMapper = SqlSession.getMapper(UserMapper.class);
	}
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713143818472.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
# 3.注解+反射解决疑问2
本例以插入操作为例
**疑问2：**

```java
如果接口的方法能够以某种特殊方式来被执行，那么执行接口的方法的时候，是怎么找到我们需要执行的SQL语句的？
```
## 3.1.定义注解
### 3.1.1.@Delete
```java
/**
 * @Description:手写mybatis删除注解
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月12日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delete {
	String value();
}
```
### 3.1.2.@Insert
```java
/**
 * @Description:手写mybatis插入注解
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月12日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Insert {
	String value();
}
```
### 3.1.3.@Select
```java
/**
 * @Description:手写mybatis查询注解
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月12日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Select {
	String value();
}
```
### 3.1.4.@Update
```java
/**
 * @Description:手写mybatis更新注解
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月12日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Update {
	String value();
}
```

### 3.1.5.@Param
```java
/**
 * @Description:参数注解
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月12日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
	String value();
}
```
## 3.2.反射获取自定义注解

```java
	Insert insert = method.getDeclaredAnnotation(Insert.class);
	Delete delete = method.getDeclaredAnnotation(Delete.class);
	Select select = method.getDeclaredAnnotation(Select.class);
	Update update = method.getDeclaredAnnotation(Update.class);
```
## 3.3.执行代理，代理获取SQL

```java
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
		System.out.println("使用JDK动态代理拦截Mapper接口");
		System.out.println("需要被代理的接口："+object);
		System.out.println("需要执行的方法："+method.getName());
		
		Insert insert = method.getDeclaredAnnotation(Insert.class);
		if(insert!=null) {
			return excuteInsert(insert,proxy,method,args);
		}
		Delete delete = method.getDeclaredAnnotation(Delete.class);
		if(delete!=null) {
			return excuteDelete(delete,proxy,method,args);
		}
		Select select = method.getDeclaredAnnotation(Select.class);
		if(select!=null) {
			return excuteSelect(select,proxy,method,args);
		}
		Update update = method.getDeclaredAnnotation(Update.class);
		if(update!=null) {
			return excuteUpdate(update,proxy,method,args);
		}
		return null;
	}
	
	/**
	 * @Description:Insert操作  
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	private Object excuteInsert(Insert insert, Object proxy, Method method, Object[] args) {
		String insertSql = insert.value();
		System.out.println("需要执行的SQL语句："+insertSql);
		return null;
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
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	private Object excuteSelect(Select select, Object proxy, Method method, Object[] args) {
		
		return args;
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
```
## 3.4.代码演示效果
如下演示所示，此时我们通过代理，明确知道我我们要执行哪一个方法，以及执行这个方法所需要执行的SQL语句。
```java
public class Test {
	public static void main(String[] args) {
		UserMapper userMapper = SqlSession.getMapper(UserMapper.class);
		User user = new User();
		String userName = "";
		System.out.println(userMapper.insertUser(user, userName));
	}
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713151631798.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
# 4.反射+参数截取解决疑问3
**疑问3：**
```java
当我们的SQL语句需要传入参数时，我们的参数是怎么绑定到SQL语句中的？
```
上述案例中，我们是直接编写好了SQL语句，实际场景中，我的SQL语句是需要传入参数的，实际情况我们拿到的SQL应该是下面这样
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713160037157.png)
这个时候，如果我们能把

```java
INSERT into user (userName,userAge,sex) VALUES(#{userName},#{userAge},#{sex})
```
上面这个语句变成如下语句，那么我们就能解决疑问3了

```java
INSERT into user (userName,userAge,sex) VALUES('胡涛',24,'男')
```

设置编译器使用反射时，保持参数名不变，不然，获取的参数名称变成
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713155556384.png)
这里以STS为例，其他的编译器自行百度
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713155448107.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713155713653.png)
## 4.1.提取的Mapper接口的入参
这里我们为了方便演示，定义了三种入参
带注解参数的字符串：@Param("userName") String userName
不带注解参数的字符串：String sex
不带注解参数的包装对象:User user

```java
	User user = new User();//传入包装对象
	user.setUserAge(24);
	//user.setUserName("胡涛");
	String userName = "胡涛";//传入有注解的
	String sex = "男";//传入没有直接的
	//User user, @Param(value="userName") String userName, String sex
	System.out.println(userMapper.insertUser(user, userName,sex));
```

```java
	@Insert("INSERT into user (userName,userAge,sex) VALUES(#{userName},#{userName},#{sex})")
	public User insertUser(User user, @Param("userName") String userName , String sex);
```
```java
/**
	 * @Description:获取方法参数:本例只举出普通字符串数据，普通包装类，字符串带参数注解，
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
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
```
演示效果
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713190017118.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
## 4.2.mybatis的占位符替换成JDBC的占位符
相信我们学习JDBC的时候，应该还记得，JDBC提供了两种方式来操作数据可，一个是带问号的的占位符，一个是直接拼接的SQL。推荐使用占位方式，因为能有防止SQL注入，具体细节，这里不做说明。

```java
//拼接方式
String sql = "select * from user where userName = 'hutao'";
Statement statement = connection.createStatement();
statement.execute(selectsql);
//---------------------------------------------------------------
//占位方式
String sql = "select * from user where userName = ?";
PreparedStatement ps = connection.prepareStatement(sql);
ps.setObject(1, "hutao");
ResultSet executeQuery = ps.executeQuery();
```
编写一个工具类，将@Insert/@Select的sql语句中的占位符的数据提取出来，将Mybatis的占位符替换成JDBC的占位符。
```java
/**
 * @Description:Sql语句字符处理类
 * @author hutao
 * @mail:hutao_2017@aliyun.com
 * @date 2020年7月13日
 */
public class SqlStringUtils {
	
	/**
	 * @Description:提取mybatis占位符中的字段
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
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
	 * @Description:将Mybatis的占位符替换成JDBC的占位符
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	public static String getQuestionSql(String sql, List<String> parameterName) {
		for (int i = 0; i < parameterName.size(); i++) {
			String string = parameterName.get(i);
			sql = sql.replace("#{" + string + "}", "?");
		}
		return sql;
	}
	public static void main(String[] args) {
		String sql = "INSERT into user (userName,userAge,sex) VALUES(#{userName},#{userAge},#{sex})";
		List<String> replacePlaceholder = getPlaceholder(sql);
		System.out.println(replacePlaceholder);
		//[userName, userAge, sex]
	}
}
```

```java
	/**
	 * @Description:Insert操作
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月12日
	 */
	private Object excuteInsert(Insert insert, Object proxy, Method method, Object[] args) {
		String insertSql = insert.value();
		System.out.println("需要执行的SQL语句：" + insertSql);
		List<String> placeholders = SqlStringUtils.getPlaceholder(insertSql);
		System.out.println("提取占位符的变量：" + placeholders);
		String questionSql = SqlStringUtils.getQuestionSql(insertSql, placeholders);
		System.out.println("占位符数据替换为问号：" + questionSql);
		Map<String, Object> methodParam = getMethodParam(method, args);
		System.out.println("执行SQL语句所需要的参数：" + methodParam);
		return null;
	}
```
**效果演示：**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200713192240518.png)

# 5.JDBC操作数据库解决疑问4
## 5.1.整合之前手写的数据库连接池
这里不在重复说明JDBC操作数据库。有兴趣的可以看下这篇文章：[手写数据库连接池](https://blog.csdn.net/m0_37892044/article/details/107239656)，在这里，我们需要用到这里面的代码。将我们之前编写的连接池整合到mybatis中。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200714160852705.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
整合完毕以后的完整代码结构如下
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200714161030388.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
## 5.2.执行插入、修改、删除，返回影响条数

封装一个专门用来修改数据的方法来执行新增，删除，修改。这里我们使用占位符的PreparedStatement。
```java
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
	 * @Description:执行SQL语句：update语句，select语句，delete语句
	 * @author hutao
	 * @mail:hutao_2017@aliyun.com
	 * @date 2020年7月14日
	 */
	private int excuteUpdateSql(String questionSql, List<String> placeholders,Map<String, Object> methodParam) throws SQLException {
		//使用我们之前手写的连接池
		Connection connection = DbPoolManager.getConnection();
		PreparedStatement ps = connection.prepareStatement(questionSql);
		//注意从1开始，不是从0开始
		for (int i = 1; i <= placeholders.size(); i++) {
			ps.setObject(i, methodParam.get(placeholders.get(i-1)));
		}
		int executeUpdate = ps.executeUpdate();
		DbPoolManager.releaseConnection(connection);
		return executeUpdate;
	}
```
## 5.3.执行查询，利用反射解析结果集解决疑问5
```java
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
```
# 6.最终结果演示
1.在插入胡涛这条SQL语句中，我们的入参有包装对象，有不带注解的普通字符串，有带注解的字符串。（其余的入参，自由发挥去完善代码，这里就不做介绍了）
2.在插入张欢这条SQL语句中，我们的入参只有包装对象。
3.查询张欢这条SQL语句中，入参为不带入参的字符串。
```java
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
```
演示截图
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200714163003898.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200714163237644.png)
从上面的结果中，我们可以看出，第一次执行数据库的时候，初始化了我们的数据库连接池，之后的操作数据库，从连接池获取连接。用完之后归还连接。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200714164307903.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzM3ODkyMDQ0,size_16,color_FFFFFF,t_70)
至此整个手写mybatis的框架的核心内容完毕。当然这个只是写了一些比较核心的内容，距离真正的投入使用，还早着。手写这一套框架，只是为了更加理解mybatis的工作原理。感谢各位的支持，喜欢就点赞吧。
# 7.完整代码包
链接：https://pan.baidu.com/s/12riVN5R5a2AcUDaDBKeU9Q 
提取码：uihv
