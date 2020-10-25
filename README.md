# MyMVC
如何使用这个mvc包
1.导包
2.配置web.xml核心配置文件
	拦截:		*.do	
	servlet-class:	包名.DispatcherServlet
	如果需要扫描注解还不要配置文件	init-param	scanPackage  包名
3.正常去写自己的Controller类
	没有继承关系
	方法名字随意	不用重写
	方法参数是为了接收请求信息的
		通常domain对象	零散值（@RequestParam）
	方法不需要抛出异常
	方法中如果需要携带一些信息
		需要ModelAndView来存储（相当于request.setAttribute()）
		如果想要存入session  先存入ModelAndView  在类上@SessionAttributes("key")
	方法的返回值ModelAndView类型	mv.setPathName();	mv.addObject("key",obj)
		String类型（路径）	对象/String不是路径（@ResponseBody）
	方法的上面添加注解（@RequestMapping("xxx.do")）





//============================================================
MVC分层架构思想（I/O  ATM小系统  DAO  Service  Swing展示  浏览器语言）

V：View视图层（浏览器  看得见的）	（HTML+CSS+JavaScript）+JSP+JSON+AJAX
	视图层的所有资源也是服务端给予的响应
C：Controller控制层（控制响应信息）	Servlet请求找的那个真实资源
	Servlet是有规则	继承  重写  方法参数  方法异常
	控制层做的具体事情	1.接收请求数据	2.找寻业务方法	3.转发重定向/响应信息
M：Model模型层（数据模型  数据处理Service  数据读写DAO  数据存储domain）
	DAO负责执行JDBC读写数据库--------代码冗余  性能问题-------连接池+ORM解决
	存入domain对象中
DB:DataBase


-----------------------------------------------------------------------------------------------------------
看起来Servlet所处在的控制层是现在代码最麻烦的地方
1.控制层Servlet有规则		继承	重写	方法带参数
2.Servlet有规则		需要告知Tomcat容器	帮我们进行管理
	配置xml文件（基于注解来使用）
3.从Servlet类的设计思想看-----------控制层
	发送一个请求（一个功能点）	直接对应一个Servlet类（对象  方法只有一个）

问题抽取
	1.类本身有各种约束	耦合度
	2.类写完了后配置麻烦	配置8行xml
	3.一个功能点	就对应一个类	8行xml

封装意义：
	1.减少web.xml配置
	2.减少Servlet类的个数

		domain类的个数-----表来决定		属性对应的get/set方法
		DAO类的个数--------domain表		DAO方法	通常都是增删改查。。。
		Service类个数-------DAO		Service方法   根据上面的功能需求提供的逻辑支持
		Servlet类的个数-----？？？
			如果能让Servlet类中也不仅有一个方法
			类的个数自然就少了---类对应的配置也就少了
		让Servlet类的个数与Service一致

================================================

一个请求（功能点）-----------类Servlet
升级成“小总管”
一个请求（功能点）-----------类Servlet中的某一个具体方法

核心在于一个类中只有一个方法做事
类太过于浪费了
一个类中不止一个方法

 String uri = request.getRequestURI();//统一资源标识符   项目/资源
 StringBuffer buffer = request.getRequestURL();//统一资源定位器   ip:port/项目/资源

小小的总结：
	1.将原来的几个Controller的方法封装到了一个Controller类中
		ATMController类中
		login	query
	2.从类的个数来讲减少了
		原来是一个请求对应一个类
		现在是一个请求对应一个方法
	3.类的个数减少随之带来xml配置减少
		配置只减少了一半	url还在 	class变成一个了
==========================================================
ATMController(类的个数原来非常多  现在比较多----Service个数对应)
	login(){原来具体的控制  取值  找业务  响应}
	query(){原来具体的控制  取值 找业务  响应}

小总管单独提取出来
	service方法（）{}从原来做具体控制  升级为一个“小总管”  分发请求

发送请求.do
找到核心入口----“小总管”DispatcherServlet
	init----加载读取自己的配置文件ApplicationContext.Properties	（类名字和真实类对应关系）
	service---接收请求的uri和method参数----分别找handler中不同的小弟做事

现在的Controller
不需要继承
不需要重写方法	名字随意了
参数不需要request	response	随意了	普通String(需要注解)

	转发			重定向
1.	url  看到的是a		url改变  看到的是b
2.	转发在服务器内部		重定向是从服务器返回浏览器  浏览器发送新请求  出去服务器外部
3.	方法不一样forward(r,r)	redirect()
4.	转发可以获取之前req信息	重定向不会

----------------------------------------
1.浏览器发送请求	.html	.jsp	AJAX异步----Servlet
	类型.do
2.通过参考web.xml找到一个核心入口
	DispatcherServlet	"大总管"（小总管升级）
	分发请求
	大总管会参考自己的配置文件ApplicationContext.properties
		----->找类   找方法   执行
3.将这些具体的找类  反射找方法等  做一个封装
	Handler类（大总管的小弟）
4.我们现在的Controller类（Servlet类）
	没有那么多的规则
	随意写一个类   xxxController   不需要继承
	类中的方法随意  名字随意  参数随意（String  int User  HashMap  request）
	类写完了以后还需要做一个配置	类名=包名.类名（类全名）
5.现在方法响应返回值也可以了
	void	表示不需要框架帮忙做响应
	String	如果没有Response注解 	表示转发/重定向	如果有注解表示响应信息
	ModelAndView	mv.setPathName();	mv.addObject("key",value);
	List<domain>	Map<String,domain>	domain	必须添加注解@ResponseBody




























