package com.xuxue.football.spider

import com.xuxue.football.servlet.{Game, Odds}
import com.xuxue.football.util.Config
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
  * Created by liuwei on 2016/9/23.
  */
class RedisFilter {

}

object RedisFilter{
  
  
  val config=Config("conf.properties");
  var redis={
    val redis=new Jedis(config.redisHost,config.redisPort)
    redis.connect()
    redis
  }
  
  @tailrec
  def filter(odds: Odds):Boolean={
    val result=Try{
      redis.hget(config.filterMap,odds.source+"_"+odds.id+"_"+odds.company+"_"+odds.oddsType)
    }
    result match {
      case Success(v)=>v.equals("1")
      case Failure(e)=>{
        redis=reconnectRedis(0).get
        filter(odds)
      }
    }
  }
  
  @tailrec
  def reconnectRedis(num:Int):Try[Jedis]={
     val result=Try{
       redis.close();
       redis.connect()
       redis
     }
    result match {
      case Success(v)=>Success(v)
      case Failure(e)=>{
        if(num>3) Failure(new InterruptedException)
        else reconnectRedis(num+1)
      }
    }
  }

}
