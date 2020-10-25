package mvc;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

/**
 * 这是一个新的类
 * 这个类没有其他特殊含义
 * 只是将那些小弟方法单独拆分出来
 * 让原来那个DispatcherServlet类更整洁一点
 */
public class Handler {
    //属性------用来存储  请求名==真实类全名的对应关系 读取文件的缓存机制
    private static Map<String, String> realClassName = new HashMap<>();//类名+类全名
    //属性------用来存储Controller类的对象    因为当前对象是单例的  这个集合只要不new新的肯定单例
    private Map<String, Object> objectMap = new HashMap<>();//类名+对象 对象延迟机制
    //属性------用来存储某一个Controller对象和他里面的全部方法
    private Map<Object, Map<String, Method>> objectMethodMap = new HashMap<>();
    //属性------用来存放 请求方法名+类名对应关系
    private Map<String, String> methodRealClassNameMap = new HashMap<>();

    String getScanPackageName(){
        return this.realClassName.get("scanPackage");
    }

    //---------------------------------------------------------------
    //0号小弟  读取配置文件的方法
    boolean loadPropertiesFile() {
        boolean flag = true;    //默认文件是存在的
        try {
            //读取专属配置文件----放入缓存realClassName中
            Properties properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("ApplicationContext.properties"));
            Enumeration en = properties.propertyNames();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String value = properties.getProperty(key);
                realClassName.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            flag = false;
        }
        return flag;
    }

    //0号小弟  负责在init方法执行的时候 加载类中的注解  请求--类--方法   对应关系
    void scanAnnotation(String packageNames){
        if (packageNames == null) {
            return;
        }
        //包名存在  a,b,c  || a
        String[] packages = packageNames.split(",");
        //解析数组中的每一个包名字  当然也有可能就一个名字
        for (String packageName : packages) {
            //循环每一次获取到某一个包名 String字符串   controller
            //包名  找到真实的类  扫描类上面的注解
            //包.类   文件夹\文件Test.class    文件拿过来  加载入内存  扫描内存中那个类上的注解
            //类加载器ClassLoader读取包名对应的路径 获取到一个文件所在的URL
            String path = packageName.replace(".", "\\");
            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url == null) {
                continue;
            }
            //根据获取的url定位一个真实类文件路径
            String packagePath = url.getPath();
            //根据packagePath创建一个File对象  用file对象来操作真实硬盘上的那个文件
            File packageFile = new File(packagePath);//file对象与硬盘上的一个文件产生一个一个映射关系
            //上面那个packageFile代表的是controller文件夹的真身  要的是里面的所有子文件对象
            //packageFile.listFiles只要名字以class结尾的文件
            File[] files = packageFile.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isFile() && file.getName().endsWith("class")) {
                        return true;
                    }
                    return false;
                }
            });
            //============================
            //我们获取到了一个files的数组  数组中存储的每一个file对象是我们找到的controller类
            for (File file : files) {
                //每次循环得到一个file类---->反射加载类   Class.forName   类名.class  对象.getClass
                //利用刚才的包名和此时file对象的文件名  拼接类全名
                String simpleName = file.getName();//获取了文件名
                String fullName = packageName + "." +simpleName.substring(0,simpleName.indexOf("."));
                //得到类全名可以反射
                try {
                    Class cla = Class.forName(fullName);
                    //找到cla上面的注解
                    RequestMapping requestMapping = (RequestMapping) cla.getAnnotation(RequestMapping.class);
                    //判断类上的注解是否存在
                    if (requestMapping != null) {   //类上面有注解
                        //获取注解里面的value值 请求名字
                        String name = requestMapping.value();
                        realClassName.put(name, fullName);
                    }else { //用户访问的请求 直接对应一个方法
                        Method[] methods = cla.getDeclaredMethods();
                        for (Method method : methods) {
                            //每次循环得到一个方法对象  获取方法对象上面的注解里面的内容
                            RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                            //保证这个注解存在
                            if (methodAnnotation != null) {
                                String name = methodAnnotation.value();//方法名字拿过来目的是为了找到方法的
                                methodRealClassNameMap.put(name, fullName);
                            }else {
                                //抛出异常，告诉用户没有找到方法
                                throw new NoSuchMethodException("没有这个方法");
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //1号小弟  负责解析请求名urlName
    String parseURI(String uri) {
        //拆分
        uri = uri.substring(uri.lastIndexOf("/") + 1);
        return uri;
    }

    //2号小弟  负责通过类名  找到obj对象
    Object findObject(String requestContent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //找对象
        Object obj = objectMap.get(requestContent);
        //如果obj没有，证明从来没有创建使用过
        if (obj == null) {
            //从类-类全名map集合中找类全名
            String fullClassName = realClassName.get(requestContent);
            if (fullClassName == null) {
                //如果第一个集合没有读取到类名，发送的请求可能是方法名.do形式
                //去第四个集合里读取     方法名+类名
                fullClassName = methodRealClassNameMap.get(requestContent);
                if(fullClassName == null){
                    //请求就有问题了   类不存在
                    //自定义异常
                    throw new ControllerNotFoundException("类不存在");
                }
            }
            Class cla = Class.forName(fullClassName);
            obj = cla.newInstance();
            objectMap.put(requestContent, obj);
            //------------------->>>>>>>>>对象懒加载之后   马上解析对象中的全部方法
            //  methodName,method------>>>>>>>>>>Map<String,Method>------>>>>>>ATMController
            //  methodName,method------>>>>>>>>>>Map<String,Method>------>>>>>>ShoppingController
            //  Map<atmController,Map<methodName,method>>------>>>>>>Map<Object,Map<String,Method>>
            Method[] methods = cla.getMethods();
            Map<String, Method> methodMap = new HashMap<>();//用来存储当前对象中的所有方法
            for (Method method : methods) {
                //将一个方法名字和方法对象存入methodMap集合中
                methodMap.put(method.getName(), method);
            }
            objectMethodMap.put(obj, methodMap);
        }

        return obj;
    }

    //3号小弟  负责通过obj对象   找到某个方法
    Method findMethod(Object obj, String methodName) {
        Map<String, Method> methodMap = objectMethodMap.get(obj);
        Method method = methodMap.get(methodName);  //重载情况没有考虑
        return method;
    }

    //4号小弟  负责分析找到的那个method 做参数的自动注入
    //  先通过request接收参数    String value = request.getParameter("key");
    //  将这个接到的value交给method让method执行
    //  参数----------->>>>>>  method  request
    //  返回值 -------->>>>>>  方法执行的时候需要的具体参数值
    Object[] injectionParameters(Method method,HttpServletRequest request,HttpServletResponse response) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, java.lang.NoSuchMethodException {
        //解析Method    拿到方法参数列表中的那些key
        //  参数类型
        //  基础类型String  int    float    double
        //  对象类型domain
        //  集合类型map
        //1.获取method方法中所有参数对象
        Parameter[] parameters = method.getParameters();
        //2.严谨判断
        if (parameters == null || parameters.length == 0) {
            return null;
        }
        //3.做一个返回值准备好
        Object[] finalParamValue = new Object[parameters.length];
        //4.解析每一个参数-----获取key-----request取值-----存入返回值数组中
        for (int i = 0; i < parameters.length; i++) {
            //每次获取一个参数对象
            Parameter parameter = parameters[i];
            //先获取当前参数前面的注解
            RequestParam paramAnnotation = parameter.getAnnotation(RequestParam.class);
            //判断是否含有注解
            if (paramAnnotation != null) {//有注解 散装的值
                //获取注解中的value值
                String key = paramAnnotation.value();
                String value = request.getParameter(key);
                //判断参数类型
                if(value != null){
                    //获取当前参数的数据类型
                    Class paramClass = parameter.getType();
                    //分析参数类型做分支判断
                    if (paramClass == int.class || paramClass == Integer.class) {
                        finalParamValue[i] = new Integer(value);
                    } else if (paramClass == double.class || paramClass == Double.class) {
                        finalParamValue[i] = new Double(value);
                    } else if (paramClass == float.class || paramClass == Float.class) {
                        finalParamValue[i] = new Float(value);
                    } else if (paramClass == String.class) {
                        finalParamValue[i] = value;
                    }
                }
            }else{//对象  map集合   原生request
                //获取参数类型
                Class paramClass = parameter.getType();
                if (paramClass.isArray()) {
                    //数组    不进行处理   异常
                    throw new ParameterTypeException("对不起，参数为数组不进行处理");
                }else {
                    if (paramClass == HttpServletRequest.class) {
                        finalParamValue[i] = request;   continue;
                    }
                    if (paramClass == HttpServletResponse.class) {
                        finalParamValue[i] = response;  continue;
                    }
                    if (paramClass == Map.class || paramClass == List.class) {
                        //传递的是接口  处理不了
                        throw new ParameterTypeException("对不起 参数为接口不进行处理");
                    }
                    //普通具体对象
                    Object paramObj = paramClass.newInstance();
                    if (paramObj instanceof Map) {
                        //造型成Map    存值
                        Map<String,Object> paramMap = (Map<String, Object>) paramObj;//paramObj是具体的  多态效果
                        //获取全部请求    用请求的key作为最终map的key
                        Enumeration en = request.getParameterNames();
                        while (en.hasMoreElements()) {
                            String key = (String) en.nextElement();
                            String value = request.getParameter(key);
                            paramMap.put(key, value);
                        }
                        finalParamValue[i] = paramMap;
                    } else if (paramObj instanceof Object) {
                        //解析对象中的全部属性    属性名key
                        Field[] fields = paramClass.getDeclaredFields();//获取参数中所有属性，包括私有
                        for (Field field : fields) {
                            field.setAccessible(true);//操作私有属性
                            String key = field.getName();//获取属性名字
                            String value = request.getParameter(key);//通过属性名字获取属性的值
                            //将这个value存入属性对象里
                            //找对象中属性类型的构造方法 比如Integer   String
                            //对象中处理不了Character类型    对象中也处理不了对象属性（递归）
                            Class fieldType =field.getType();
                            Constructor fieldConstructor = fieldType.getConstructor(String.class);//除了Character 其他都能通过String构建
                            field.set(paramObj,fieldConstructor.newInstance(value));
                        }
                        finalParamValue[i] = paramObj;
                    }else {
                        throw new ParameterTypeException("未知类型，处理不了");
                    }
                }
            }
        }
        return finalParamValue;
    }

    //5.1号小小弟  负责处理方法的返回值   为5号小弟服务
    //参数    方法的返回值  转发    重定向 request response
    //返回值   void
    private void parseResponseContent(String path, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"".equals(path) && !"null".equals(path)) {
            //redirect:xxx.jsp
            String[] value = path.split(":");
            if (value.length == 1) {
                //应该是一个正常的转发
                request.getRequestDispatcher(path).forward(request,response);
            } else {
                //认为是一个重定向
                if("redirect".equals(value[0])){
                    response.sendRedirect(value[1]);
                }
            }
        }else {
            throw new PathNotFoundException("非正常路径，我也处理不了");
        }
    }

    //5.2号小小弟
    private void parseModelAndView(Object obj,ModelAndView mv, HttpServletRequest request) {
        //从mv对象中把那个map集合获取出来
        HashMap<String,Object> mvMap = mv.getAttributeMap();
        //遍历集合中的元素  拿出来 存入request作用域
        Set<String> keys = mvMap.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = mvMap.get(key);
            //存入request作用域中
            request.setAttribute(key,value);
        }
        //再分析一下注解看是否需要存入session
        SessionAttributes sessionAttributes= obj.getClass().getAnnotation(SessionAttributes.class);
        if (sessionAttributes != null) {
            String[] attributeNames = sessionAttributes.value();
            if (attributeNames.length != 0) {
                HttpSession session = request.getSession();
                for (String attributeName : attributeNames) {
                    session.setAttribute(attributeName,mvMap.get(attributeName));
                }
            }
        }
    }

    //5号小弟  负责处理方法返回值  不一定是String  也有可能是ModelAndView
    //参数    方法执行的返回值    Object类型
    //返回值   void
    void finalParse(Object obj,Method method,Object methodResult,HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        if (methodResult == null) {
            return;//证明这个方法不需要框架帮我们做响应处理
        }
        if (methodResult instanceof ModelAndView) {
            ModelAndView mv = (ModelAndView)methodResult;
            //找小小弟解析mv对象
            this.parseModelAndView(obj,mv, request);
            //再找另一个小小弟解析字符串
            this.parseResponseContent(mv.getPathName(),request,response);
        } else if (methodResult instanceof String) {
            //返回字符串  可能表示一个路径   也可能表示一个数据
            //在返回值表示数据的方法上面加上@ResponseBody注解来进行区分
            //获取方法上面的注解说明
            ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
            if (responseBody != null) {//有注解  证明返回值是一个数据
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write((String)methodResult);
            }else {
                this.parseResponseContent((String) methodResult,request,response);
            }
        }else {
            //返回值可能是一些对象  domain  List<domain>
            //AJAX+JSON
            ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
            if (responseBody != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("jsonObject", methodResult);
                response.getWriter().write(jsonObject.toJSONString());
            }else {
                //抛出一个自定义异常
                //返回值不认识  需要添加注解

            }
        }

    }
}
