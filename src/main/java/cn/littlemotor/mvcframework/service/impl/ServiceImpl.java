package cn.littlemotor.mvcframework.service.impl;

import cn.littlemotor.mvcframework.annotation.Service;
import cn.littlemotor.mvcframework.service.BaseService;

@Service
public class ServiceImpl implements BaseService {
  @Override
  public String get(String name) {
    return "name is " + name;
  }
}
