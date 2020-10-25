package mvc;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;


/**
 * 这是升级出来的小总管---“大总管”
 * <p>
 * 这个小总管对象就是原来的Servlet
 * 是遵循原来Servlet对象生命周期机制 init    service      destroy
 */
public class DispatcherServlet extends HttpServlet {
    Handler handler = new Handler();

    //---------------------------------------------------------------
    //init方法  标识着这个小总管对象的创建
    public void init(ServletConfig config) {
        String packageNames = null;
        boolean flag = handler.loadPropertiesFile();
        //有可能文件是不存在---->文件中只有一个信息（告知需要扫描注解的包）
        if (!flag) {//根本就没有文件，必然要扫描注解
            packageNames = config.getInitParameter("scanPackage");
        }else {
            packageNames = handler.getScanPackageName();
        }
        handler.scanAnnotation(packageNames);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            //1.通过request获取请求的类名    urlName     request.getRequestURI()
            String uri = request.getRequestURI();
            //2.找1号小弟   帮助解析uri    得到一个请求名
            String requestContent = handler.parseURI(uri);

            String methodName = request.getParameter("method");
            if (methodName == null) {
                methodName = requestContent.substring(0, requestContent.indexOf("."));
            }
            //3.找2号小弟   帮助找到obj对象
            Object obj = handler.findObject(requestContent);
            //4.找3号小弟   通过obj对象找到对象里面的方法
            //通过request获取请求方法名  request.getParameter("method");
            Method method = handler.findMethod(obj, methodName);
            //5.找4号小弟   处理方法上面的参数DI
            //做某一个方法的解析  将方法执行所需的参数注入进去
            Object[] finalParamValue = handler.injectionParameters(method, request, response);
            //6.直接invoke执行执行方法
            Object methodResult =  method.invoke(obj, finalParamValue);
            //7.找5号小弟   处理方法执行完毕后的返回结果（响应  转发路径  重定向路径   返回对象JSON）
           handler.finalParse(obj,method,methodResult,request,response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

















    //=====================================================

    // 请求格式：ATMController.do?method=login
//    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        try {
//            //1.找到请求的类
//            String uri = request.getRequestURI();
//            //解析uri
//            //System.out.println(uri);//             /MyMVC/ATMController.do
//            String urlName = uri.substring(uri.lastIndexOf("/") + 1, uri.indexOf("."));
//            //System.out.println(uri);  //            ATMController
//            //发送请求  真实要找的类全名    对应关系（用户知道）
//
//            //2.通过缓存找真实类全名
//            String fullClassName = realClassName.get(urlName);
//
//            //System.out.println(fullClassName);
//            //3.找到请求的方法名
//            String methodName = request.getParameter("method");
//            //System.out.println(className+"------"+methodName);//    ATMController------login
//
//            //4.反射获取方法
//            //先找类   Class.forName(类全名)  对象.getClass()   类.class
//            Class cla = Class.forName(fullClassName);
//            //找方法
//            Method method = cla.getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
//            //5.反射让方法执行
//
//            Object obj = objectMap.get(urlName);
//            if (obj == null) {
//                obj = cla.newInstance();//
//                objectMap.put(urlName, obj);
//            }
//
//            //method.invoke(对象，参数);
//            String result = (String) method.invoke(obj,request, response);
//            //6.按照方法返回值“转发”处理
//            request.getRequestDispatcher(result).forward(request,response);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    //========================================================================

    //下面的方法是从Controller中提取出来的小总管
//    public void service(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
//        try {
//            //此时这个service方法相当于升级了
//            //他原来做的是自己的  具体登录控制
//            //现在这个方法升级成为一个“小总管”
//            //他自己不做具体的控制    负责分发给下面的两个方法
//            //--------------------------------------------------
//            //1.接收请求的名字（一个方法名）          约定优于配置的原则
//            String uri = request.getRequestURI();//统一资源标识符   项目/资源
//            //StringBuffer buffer = request.getRequestURL();//统一资源定位器   ip:port/项目/资源
//            uri = uri.substring(uri.lastIndexOf("/")+1);//请求名字----表示一个方法名
//            //System.out.println(uri);
//
//            //2.根据请求的方法名去下面找方法就可以了  反射通用
//            //先找类   3种方法    Class.forName("类全名");   对象.getClass();  类名.class;
//            Class cla = this.getClass();
//            //后找方法
//            Method method = cla.getDeclaredMethod(uri,HttpServletRequest.class,HttpServletResponse.class);
//
//            //3.让方法执行 invoke
//            String result = (String) method.invoke(this,request,response);
//            //4.方法最后进行转发
//            request.getRequestDispatcher(result).forward(request,response);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
