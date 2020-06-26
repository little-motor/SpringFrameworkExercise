package cn.littlemotor.mvcframework.servlet;

import cn.littlemotor.mvcframework.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
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
    if (!this.handlerMapping.containsKey(url)){
      resp.getWriter().write("404 Not Found!");
      return;
    }
    Method method = handlerMapping.get(url);
    Map<String, String[]> requestParamMap = req.getParameterMap();
    Class<?>[] methodParameterTypes = method.getParameterTypes();
    Object[] reflectParamValues = new Object[methodParameterTypes.length];
    for (int i = 0; i < methodParameterTypes.length; i++){
      Class parameterType = methodParameterTypes[i];
      if (parameterType == HttpServletRequest.class) {
        reflectParamValues[i] = req;
        continue;
      } else if (parameterType == HttpServletResponse.class) {
        reflectParamValues[i] = resp;
        continue;
      } else if (parameterType == String.class) {
        Annotation[][] annotations = method.getParameterAnnotations();
        for (Annotation annotation : annotations[i]) {
          if (annotation instanceof RequestParam){
            String parameterName = ((RequestParam) annotation).value();
            reflectParamValues[i] = Arrays.toString(requestParamMap.get(parameterName)).replaceAll("\\[|\\]", "");
          }
        }
      }
    }
    Object object = this.ioc.get(firstLetterLowerCase(method.getDeclaringClass().getSimpleName()));
    method.invoke(object, reflectParamValues);
  }
}
