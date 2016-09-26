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
  
  @tailrec
  final def filter(odds: Odds):Boolean={
    val result=Try{
      redis.hget(config.filterMap,odds.source+"_"+odds.id+"_"+odds.company+"_"+odds.oddsType)
    }
    result match {
      case Success(v)=>{
       true
      }
      case Failure(e)=>{
        redis=reconnectRedis(0).get
        filter(odds)
      }
    }
  }
  
  private def updeateMap(odds: Odds,value:String):Boolean={
    redis.hset(config.filterMap,getOddsKey(odds),value)
    true
  }
  
  private def deleteMap(odds: Odds):Boolean={
    true
  }
  
  private def getOddsKey(odds: Odds):String={
    odds.source+"_"+odds.id+"_"+odds.company+"_"+odds.oddsType
  }
  
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
