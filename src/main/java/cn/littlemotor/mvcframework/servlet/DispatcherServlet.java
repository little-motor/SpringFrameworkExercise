package cn.littlemotor.mvcframework.servlet;

import cn.littlemotor.mvcframework.annotation.Autowired;
import cn.littlemotor.mvcframework.annotation.Controller;
import cn.littlemotor.mvcframework.annotation.RequestMapping;
import cn.littlemotor.mvcframework.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

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
      e.printStackTrace();
      resp.getWriter().write("500 Exception " + e.getMessage());
    }
  }

  private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    String url = req.getRequestURI();
    String contextPath = req.getContextPath();
    url = url.replace(contextPath, "").replaceAll("/+", "/");
    if (!this.mapping.containsKey(url)){
      resp.getWriter().write("404 Not Found!");
      return;
    }
    Method method = (Method) mapping.get(url);
    Map<String, String[]> params = req.getParameterMap();
    Object object = this.mapping.get(method.getDeclaringClass().getName());
    method.invoke(object, req, resp, params.get("name")[0]);
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
      //注意此处只所以新建set是因为后面mapping会有新增元素不能直接对mapping.keyset遍历
      Set<String> keys = new HashSet<>(mapping.keySet());
      for (String className : keys) {
        if (!className.contains(".")) return;
        Class<?> clazz = Class.forName(className);
        //注册Controller
        if (clazz.isAnnotationPresent(Controller.class)) {
          mapping.put(className, clazz.newInstance());
          String baseUrl = "";
          if (clazz.isAnnotationPresent(RequestMapping.class)) {
            baseUrl = clazz.getAnnotation(RequestMapping.class).value();
          }
          Method[] methods = clazz.getMethods();
          for (Method method : methods) {
            if (!method.isAnnotationPresent(RequestMapping.class)) continue;
            String url = (baseUrl + method.getAnnotation(RequestMapping.class).value()).replaceAll("/+", "/");
            mapping.put(url, method);
            System.out.println("Mapped " + url + "," + method);
          }
        }
        //注册Service
        else if (clazz.isAnnotationPresent(Service.class)) {
          String beanName = clazz.getAnnotation(Service.class).value();
          if (beanName.equals("")) {
            beanName = clazz.getName();
          }
          Object instance = clazz.newInstance();
          mapping.put(beanName, instance);
          for (Class<?> interfaze : clazz.getInterfaces()) {
            mapping.put(interfaze.getName(), instance);
          }
        }
      }
      //给mapping中的value对象注入@autowire
      for (Object object : mapping.values()) {
        if(Objects.isNull(object)) continue;
        Class clazz = object.getClass();
        if (clazz.isAnnotationPresent(Controller.class)) {
          Field[] fields = clazz.getDeclaredFields();
          for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
              Autowired autowired = field.getAnnotation(Autowired.class);
              String beanName = autowired.value();
              if ("".equals(beanName)) {
                beanName = field.getType().getName();
              }
              field.setAccessible(true);
              try {
                field.set(object, mapping.get(beanName));
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }

      }
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
    System.out.println("MVC framework has inited");
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
