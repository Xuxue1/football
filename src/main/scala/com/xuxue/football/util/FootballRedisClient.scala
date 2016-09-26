package com.xuxue.football.util

import com.xuxue.football.servlet.Odds
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
  * Created by xuxue on 2016/9/25.
  */
class FootballRedisClient(val config:Config) {
  
  var redis={
    val redis=new Jedis(config.redisHost,config.redisPort)
    redis.connect()
    redis
  }
  

  final def filter(odds: Odds):Boolean={
      val status=getOddsStatus(odds)
      if(status.equals("1")) true else false
  }
  
  def updateMap(odds: Odds,value:String):Boolean={
    redis.hset(config.filterMap,getOddsKey(odds),value)
    true
  }
  
  def deleteMap(odds: Odds):Boolean={
    redis.hdel(config.filterMap,getOddsKey(odds))
    true
  }

  private def getOddsStatus(odds: Odds):String={
    redis.hget(config.filterMap,getOddsKey(odds))
  }
  
  private def getOddsKey(odds: Odds):String={
    odds.source+"_"+odds.id+"_"+odds.company+"_"+odds.oddsType
  }

  /**
    *
    * @param num
    * @return
    */
  @tailrec
  final def reconnectRedis(num:Int):Try[Jedis]={
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
