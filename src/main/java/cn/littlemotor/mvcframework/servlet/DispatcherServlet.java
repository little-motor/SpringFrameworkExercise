package cn.littlemotor.mvcframework.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class DispatcherServlet extends HttpServlet {

  private Map<String, Object> mapping = new HashMap<>();

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
      resp.getWriter().write("500 Exception " + e.getMessage());
    }
  }

  private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    String url = req.getRequestURL().toString();
    String contextPath = req.getContextPath();
    url = url.replace(contextPath, "").replaceAll("/+", "/");
    if (!this.mapping.containsKey(url)){
      resp.getWriter().write("404 Not Found!");
      return;
    }
    Method method = (Method) mapping.get(url);
    Map<String, String[]> params = req.getParameterMap();
    method.invoke(this.mapping.get(method.getDeclaringClass().getName()), new Object[]{req, resp, params.get("name")[0]});
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    InputStream inputStream = null;
    try {
      Properties configContext = new Properties();
      System.out.println(config.toString());
      inputStream = getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
      configContext.load(inputStream);
      String scanPackage = configContext.getProperty("scanPackage");
      doScanner(scanPackage);
      Thread.currentThread();





    } catch (Exception e) {

    } finally {
      if (Objects.nonNull(inputStream)) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void doScanner(String packageName) {
    URL url = this.getClass().getClassLoader().getResource("/" + packageName.replace(".", "/"));
    File classDir = new File(url.getFile());
    for (File file : classDir.listFiles()) {
      if (file.isDirectory()) {
        doScanner(packageName + "." + file.getName());
      } else {
        if (!file.getName().endsWith("class")) continue;
        String className = packageName + "." + file.getName().replaceAll(".class", "");
        mapping.put(className, null);
      }
    }
  }
}
