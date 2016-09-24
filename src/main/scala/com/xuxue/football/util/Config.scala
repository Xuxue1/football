package com.xuxue.football.util

import java.util.Properties

import com.xuxue.football.util.Loan.use

/**
  * Created by liuwei on 2016/9/24.
  */
class Config(val redisPort:Int,val redisHost:String) {
  override def toString = s"Config($redisPort, $redisHost)"
}

object Config{
  implicit def stringToInt(s:String):Int={
    Integer.parseInt(s)
  }
  def apply(fileName:String): Config = {
    val stream = getClass.getClassLoader.getResourceAsStream(fileName);
    use(getClass.getClassLoader.getResourceAsStream(fileName)){
      stream=>
        val properties=new Properties()
        properties.load(stream)
        new Config(properties.getProperty("redis_port"),properties.getProperty("redis_host"))
    }
  }
  def main(args: Array[String]): Unit = {
    val config=Config("conf.properties")
    println(config)
  }
}
