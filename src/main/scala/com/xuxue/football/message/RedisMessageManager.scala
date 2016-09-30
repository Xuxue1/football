package com.xuxue.football.message
import com.google.gson.Gson
import com.xuxue.football.servlet.Game
import com.xuxue.football.util.{Config}
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}
import collection.JavaConversions._

/**
  * @author xuxue
  */
class RedisMessageManager extends MessageManager{

    val conf=Config("conf.properties")
    val G=new Gson()
    var redis={
        val redis = new Jedis(conf.redisHost, conf.redisPort)
        redis.connect()
        redis
    }

    override def push(game: Game): Boolean ={
        val value=Try{
            redis.lpush(conf.gameTaskKey,G.toJson(game))
        }
        value match {
            case Success(v) => true
            case Failure(ex) => {
                reconnectRedis(0)
                push(game)
            }
        }
    }

    override def pop(): Game = {
        val value=Try{
            redis.brpop(60,conf.gameTaskKey)
        }
        value match {
            case Success(v)=>{
                if(v==null) pop()
                else G.fromJson(v.get(1),classOf[Game])
            }
            case Failure(ex)=>{
                redis=reconnectRedis(0).get
                pop()
            }
        }
    }

    /**
      *
      * @param num
      * @return
      */
    @tailrec
    final def reconnectRedis(num: Int): Try[Jedis] = {
        val result = Try {
            redis.close();
            redis.connect()
            redis
        }
        result match {
            case Success(v) => Success(v)
            case Failure(e) => {
                if (num > 3) Failure(new InterruptedException)
                else reconnectRedis(num + 1)
            }
        }
    }
}



object RedisMessageManager{

    def main(args: Array[String]): Unit = {
        val conf=Config("conf.properties")

        val client=new Jedis(conf.redisHost,conf.redisPort)
        client.connect()

        val x=Try{
            client.brpop(20,"haha")
        }

        x match {
            case Success(v)=>{
                if(v==null) println("value is null")
                else v.map(println(_))
            }
            case Failure(ex)=>{
                ex.printStackTrace()
            }
        }
    }

}