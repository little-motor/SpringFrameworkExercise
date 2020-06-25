package cn.littlemotor.mvcframework.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 这一层主要实现便业务方面的逻辑
 */
public class DispatcherServlet extends FrameworkServlet {


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      this.doPost(req, resp);
    } catch (Exception e) {
      resp.getWriter().write("500 Exception " + e.getMessage());
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      doDispatcher(req, resp);
    } catch (Exception e) {
      e.printStackTrace();
      resp.getWriter().write("500 Exception " + e.getMessage());
    }
  }

  private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    String url = req.getRequestURI();
    String contextPath = req.getContextPath();
    url = url.replace(contextPath, "").replaceAll("/+", "/");
    if (!this.ioc.containsKey(url)){
      resp.getWriter().write("404 Not Found!");
      return;
    }
    Method method = (Method) ioc.get(url);
    Map<String, String[]> params = req.getParameterMap();
    Object object = this.ioc.get(method.getDeclaringClass().getName());
    method.invoke(object, req, resp, params.get("name")[0]);
  }
}
