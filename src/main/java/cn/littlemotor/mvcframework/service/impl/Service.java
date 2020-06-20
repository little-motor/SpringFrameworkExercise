package cn.littlemotor.mvcframework.service.impl;

import cn.littlemotor.mvcframework.service.BaseService;

public class Service implements BaseService {
  @Override
  public String get(String name) {
    return "name is " + name;
  }
}
