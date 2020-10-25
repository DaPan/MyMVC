<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2020/10/23
  Time: 10:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>$Title$</title>
  </head>
  <body>
  <%--发送请求的时候 携带两个信息  类名AtmController  方法名login--%>
  <%--约定优于配置：ATMController.do?method=login             --%>
  <%--发送请求的时候，遵循HTTP协议 . ? :只能有一个--%>
    <a href="ATMController.do?method=login&name=dp1&password=12">模拟ATM系统功能点1(登录)</a><br/>
    <a href="ATMController.do?method=query&name=dp2&password=34">模拟ATM系统功能点2(查询)</a><br/>
    <%--不同的功能--%>
    <a href="kindQuery.do?name=dp3&password=56">模拟Shopping系统功能点1(查询种类)</a><br/>
    <a href="kindAdd.do?name=dp4&password=78">模拟Shopping系统功能点2(种类添加)</a><br/>

    <hr>
<%--    ${requestScope.result}--%>
<%--    <form action="login.do" method="post">--%>
<%--      用户名：<input type="text" name="name" value=""><br/>--%>
<%--      密  码：<input type="password" name="password" value=""><br/>--%>
<%--      <input type="submit" value="登录">--%>
<%--    </form>--%>
  </body>
</html>
