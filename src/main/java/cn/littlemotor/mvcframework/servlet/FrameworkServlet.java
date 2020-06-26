package cn.littlemotor.mvcframework.servlet;

import cn.littlemotor.mvcframework.annotation.Autowired;
import cn.littlemotor.mvcframework.annotation.Controller;
import cn.littlemotor.mvcframework.annotation.RequestMapping;
import cn.littlemotor.mvcframework.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 这一层主要实现初始化逻辑
 */
public class FrameworkServlet extends HttpServlet {

  protected Map<String, Object> ioc = new HashMap<>();
  protected Map<String, Method> handlerMapping = new HashMap<>();

  private Properties contextConfig = new Properties();
  private List<String> classNames = new ArrayList<>();


  @Override
  public void init(ServletConfig config) throws ServletException {
    doLoadConfig(config.getInitParameter("contextConfigLocation"));

    doScanner(this.contextConfig.getProperty("scanPackage"));

    doIcoInitialization();

    doAutoWired();

    initHandlerMapping();

    System.out.println("MVC framework has inited");
  }


  private void doLoadConfig(String contextConfigLocation) {
    InputStream inputStream = null;
    try {
      inputStream = getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
      this.contextConfig.load(inputStream);
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
        this.classNames.add(className);
      }
    }
  }

  private void doIcoInitialization() {
    if (classNames.isEmpty()) return;
    try {
      for (String className : classNames) {
        Class<?> clazz = Class.forName(className);
        //注册Controller
        if (clazz.isAnnotationPresent(Controller.class)) {
          Object bean = clazz.newInstance();
          String beanName = firstLetterLowerCase(clazz.getSimpleName());
          ioc.put(beanName, bean);

        }
        //注册Service
        else if (clazz.isAnnotationPresent(Service.class)) {
          String beanName = clazz.getAnnotation(Service.class).value();
          if (beanName.equals("")) {
            beanName = firstLetterLowerCase(clazz.getSimpleName());
          }
          Object bean = clazz.newInstance();
          ioc.put(beanName, bean);
          for (Class<?> interfaze : clazz.getInterfaces()) {
            ioc.put(firstLetterLowerCase(interfaze.getSimpleName()), bean);
          }
        }
      }
    } catch (Exception e) {
    }
  }

  private void doAutoWired() {
    //给mapping中的value对象注入@autowire
    for (Object object : ioc.values()) {
      if (Objects.isNull(object)) continue;
      Field[] fields = object.getClass().getDeclaredFields();
      for (Field field : fields) {
        if (field.isAnnotationPresent(Autowired.class)) {
          Autowired autowired = field.getAnnotation(Autowired.class);
          String beanName = autowired.value();
          if ("".equals(beanName)) {
            beanName = firstLetterLowerCase(field.getType().getSimpleName());
          }
          field.setAccessible(true);
          try {
            field.set(object, ioc.get(beanName));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private void initHandlerMapping() {
    if (ioc.isEmpty()) return;
    for (Object instance : ioc.values()) {
      Class<?> clazz = instance.getClass();
      if (clazz.isAnnotationPresent(Controller.class)) {
        String baseUrl = "";
        if (clazz.isAnnotationPresent(RequestMapping.class)) {
          baseUrl = clazz.getAnnotation(RequestMapping.class).value();
        }
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
          if (!method.isAnnotationPresent(RequestMapping.class)) continue;
          String url = (baseUrl + "/" + method.getAnnotation(RequestMapping.class).value()).replaceAll("/+", "/");
          handlerMapping.put(url, method);
          System.out.println("Mapped " + url + "," + method);
        }
      }
    }
  }

  protected String firstLetterLowerCase(String name) {
    char[] chars = name.toCharArray();
    if (Objects.nonNull(chars) && (chars[0] >= 65 && chars[0] <= 90)) {
      chars[0] += 32;
      return new String(chars);
    }
    return name;
  }
}
