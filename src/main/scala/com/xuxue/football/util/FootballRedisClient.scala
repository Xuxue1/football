package com.xuxue.football.util

import java.util.{Calendar, Date, GregorianCalendar}

import com.xuxue.football.servlet.Odds
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
  * Created by xuxue on 2016/9/25.
  */
class FootballRedisClient(val config: Config) {

    val LOG=LoggerFactory.getLogger(classOf[FootballRedisClient])

    var redis = {
        val redis = new Jedis(config.redisHost, config.redisPort)
        redis.connect()
        redis
    }


    final def filter(odds: Odds): Boolean = {
        val status = getOddsStatus(odds)
        if (status!=null&&status.equals("1")) true else false
    }

    def mapKey():String={
        val date=new GregorianCalendar()
        val key=date.get(Calendar.YEAR)+"-"+(date.get(Calendar.MONTH)+1)+
          "-"+date.get(Calendar.DAY_OF_MONTH)
        config.filterMap+"_"+key
    }

    def updateMap(odds: Odds, value: String): Boolean = {
        val result = Try {
            redis.hset(mapKey(), getOddsKey(odds), value)
        }
        result match {
            case Success(v) => {
                true
            }
            case Failure(ex) => {
                redis = reconnectRedis(0).get
                LOG.warn("Update status map failure will try next",ex)
                updateMap(odds, value)
            }
        }
    }

    def deleteMap(odds: Odds): Boolean = {
        val result = Try {
            redis.hdel(mapKey(), getOddsKey(odds))
        }
        result match {
            case Success(v) => {
                true
            }
            case Failure(ex) => {
                redis = reconnectRedis(0).get
                LOG.warn("delete status map failure will try later",ex)
                deleteMap(odds)
            }
        }
    }

    private def getOddsStatus(odds: Odds): String = {
        redis.hget(mapKey(), getOddsKey(odds))

        val result = Try {
            redis.hget(config.filterMap, getOddsKey(odds))
        }
        result match {
            case Success(v) => {
                v
            }
            case Failure(ex) => {
                LOG.warn("Get odds status failure will try later",ex)
                redis = reconnectRedis(0).get
                getOddsStatus(odds)
            }
        }
    }

    private def getOddsKey(odds: Odds): String = {
        odds.source + "_" + odds.id + "_" + odds.company + "_" + odds.oddsType
    }

    /**
      * redis连接重试
      * @param num 重试的次数
      * @return redis连接
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

    def close(): Unit ={
        this.redis.close()
    }
}