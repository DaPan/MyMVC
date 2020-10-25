package controller;

import domain.User;
import mvc.RequestMapping;
import mvc.ResponseBody;
import mvc.SessionAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * 这个控制层是专门负责Shopping种类相关的
 */

public class ShoppingController {

    @RequestMapping("kindQuery.do")
    public String kindQuery(HashMap<String,Object> map) {
        System.out.println("这是shoppingController中的kindQuery方法");
        System.out.println(map);
        return "welcome.jsp";
    }
    @RequestMapping("kindAdd.do")
    public String  kindAdd(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        System.out.println("这是shoppingController中的kindAdd方法");
        System.out.println(request.getParameter("name"));
        System.out.println(request.getParameter("password"));
        return "welcome.jsp";
    }
}
