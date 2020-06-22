package cn.littlemotor.mvcframework;

import java.io.*;
import java.util.Properties;

public class test {
  public static void main(String[] args) throws IOException {
    Properties pro = new Properties();
    pro.setProperty("driver", "com.mysql.jdbc.Driver");
    pro.setProperty("url", "jdbc:mysql///user");
    pro.setProperty("user", "root");
    pro.setProperty("password", "451535");
    pro.setProperty("name", "中文");
    //1.通过字节流的形式
    //store(OutputStream out, String comments)
    //outputStream:字节输出流   comments：配置文件说明
    pro.store(new OutputStreamWriter(new FileOutputStream("/Users/littlemotor/jdbc.properties"), "utf-8"), "");


  }
}
