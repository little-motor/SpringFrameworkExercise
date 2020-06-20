package cn.littlemotor.mvcframework.controller;

import cn.littlemotor.mvcframework.annotation.Autowired;
import cn.littlemotor.mvcframework.annotation.Controller;
import cn.littlemotor.mvcframework.annotation.RequestMapping;
import cn.littlemotor.mvcframework.annotation.RequestParam;
import cn.littlemotor.mvcframework.service.BaseService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping
public class BaseController {

  @Autowired
  private BaseService service;

  @RequestMapping("/query")
  public void query(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                    @RequestParam("name") String name) {
    String result = service.get(name);
    try {
      httpServletResponse.getWriter().write(result);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @RequestMapping("/add")
  public void add(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                  @RequestParam("a") Integer a, @RequestParam("b") Integer b) {
    try {
      httpServletResponse.getWriter().write(a + " + " + b + " = " + (a + b));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @RequestMapping("/remove")
  public void remove(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                     @RequestParam("id") Integer id) {

  }


}
