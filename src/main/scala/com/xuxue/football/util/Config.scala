package com.xuxue.football.util

import java.util.Properties

import com.xuxue.football.util.Loan.use

/**
  * Created by liuwei on 2016/9/24.
  */
class Config(val redisPort:Int,val redisHost:String,
             val mysqlURL:String,val mysqlUser:String
            ,val mysqlPassword:String) {
  override def toString = s"Config($redisPort, $redisHost, $mysqlURL, $mysqlUser, $mysqlPassword)"
}

object Config{
  implicit def stringToInt(s:String):Int={
    Integer.parseInt(s)
  }
  def apply(fileName:String): Config = {
    use(getClass.getClassLoader.getResourceAsStream(fileName)){
      stream=>
        val properties=new Properties()
        properties.load(stream)
        new Config(properties.getProperty("redis_port"),
          properties.getProperty("redis_host"),
          properties.getProperty("mysql_url"),
          properties.getProperty("mysql_user"),
          properties.getProperty("mysql_password"))
    }
  }
  def main(args: Array[String]): Unit = {
    val config=Config("conf.properties")
    println(config)
  }
}
