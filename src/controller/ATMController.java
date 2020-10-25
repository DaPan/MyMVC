package controller;

import com.sun.jmx.snmp.SnmpUnknownAccContrModelException;
import domain.User;
import mvc.ModelAndView;
import mvc.RequestMapping;
import mvc.RequestParam;
import mvc.SessionAttributes;
import service.ATMService;

//不用继承  松耦合
//继承父类                              现在不用继承
//方法重写                              现在不用重写
//方法必须有两个参数                     方法参数可以随意    原生String,int,float需要加注解 数组不行 接口不行
//方法必须有两个异常                     方法没有异常
//方法没有返回值                         现在方法有返回值  String  表示路径（转发、重定向redirect)
//Server对象的生命周期问题---管理机制
//单例    延迟加载                       底层Controller对象单例 延迟加载机制还保留
@SessionAttributes("name")
@RequestMapping("ATMController.do")
public class ATMController {

    private ATMService service = new ATMService();

    @RequestMapping("login.do")
    public ModelAndView login(User user){

        ModelAndView mv = new ModelAndView();
        //1.接收请求参数---以前通过request来接的 现在有参数列表
        //2.找寻业务层做登录逻辑判断
        //String result = service.login(user);
        String result = "success";
        //3.根据result做响应转发
        if ("success".equals(result)) {
            //我想要参数值  需要放一个空的容器（参数）  让框架去接收值  注入到参数容器里
            //我想将值存入request作用域  写一个容器  值存入容器里  让框架去容器里拿值  存入request里
            //容器的类型？------->>>>>Map<String,Object>
            //Map如何交给框架？------->>>>>>>返回值
            //现在返回值有路径+Map------->>>>>>包装成对象

            //request.setAttribute("name",name);
            mv.addAttribute("name", user.getName());
            //return "welcome.jsp";
            mv.setPathName("welcome.jsp");
        }else {
            mv.addAttribute("result",result);
            //return "index.jsp";
            mv.setPathName("index.jsp");
        }
        return mv;









        // System.out.println("这是login的controller");
        //System.out.println(name+"====="+password);
        //1.接收请求传递的参数？？？
        //2.调用业务层的方法
        //String result = service.login(name);
        //3.给予响应
        //  自己给响应
        //response.getWriter().write("<html>");
        //request.getRequestDispatcher("welcome.jsp").forward(request,response);
        //response.sendRedirect("xxx.jsp");
        //返回路径给大总管
        //return "welcome.jsp";   //响应信息  转发  重定向
    }


    @RequestMapping("query.do")
    public String  query(User user){
        System.out.println("这是query的controller");
        System.out.println(user);
        return "redirect:query.jsp";
    }



}
