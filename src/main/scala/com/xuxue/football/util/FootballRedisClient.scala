package com.xuxue.football.util

import com.xuxue.football.servlet.Odds
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}
import collection.JavaConversions._

/**
  * Created by xuxue on 2016/9/25.
  */
class FootballRedisClient(val config: Config) {

    var redis = {
        val redis = new Jedis(config.redisHost, config.redisPort)
        redis.connect()
        redis
    }


    final def filter(odds: Odds): Boolean = {
        val status = getOddsStatus(odds)
        if (status!=null&&status.equals("1")) true else false
    }


    def updateMap(odds: Odds, value: String): Boolean = {
        val result = Try {
            redis.hset(config.filterMap, getOddsKey(odds), value)
        }
        result match {
            case Success(v) => {
                true
            }
            case Failure(ex) => {
                redis = reconnectRedis(0).get
                updateMap(odds, value)
            }
        }
    }

    def deleteMap(odds: Odds): Boolean = {
        val result = Try {
            redis.hdel(config.filterMap, getOddsKey(odds))
        }
        result match {
            case Success(v) => {
                true
            }
            case Failure(ex) => {
                redis = reconnectRedis(0).get
                deleteMap(odds)
            }
        }
    }

    private def getOddsStatus(odds: Odds): String = {
        redis.hget(config.filterMap, getOddsKey(odds))

        val result = Try {
            redis.hget(config.filterMap, getOddsKey(odds))
        }
        result match {
            case Success(v) => {
                v
            }
            case Failure(ex) => {
                redis = reconnectRedis(0).get
                getOddsStatus(odds)
            }
        }
    }

    private def getOddsKey(odds: Odds): String = {
        odds.source + "_" + odds.id + "_" + odds.company + "_" + odds.oddsType
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
