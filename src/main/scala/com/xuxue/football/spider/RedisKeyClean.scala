package com.xuxue.football.spider

import java.util.{Calendar, Date, GregorianCalendar, TimerTask}

import com.xuxue.football.util.Config
import redis.clients.jedis.Jedis

/**
  * 定期清除redis里面的过期的key
  * @author xuxue
  */
class RedisKeyClean extends TimerTask{

  override def run(): Unit = {
    val config=Config("conf/aoke.conf")
    val redis=new Jedis(config.redisHost,config.redisPort)
    redis.connect()
    val calendar=new GregorianCalendar();
    calendar.add(Calendar.DAY_OF_MONTH,-3)
    val date=new GregorianCalendar()
    val key=date.get(Calendar.YEAR)+"-"+(date.get(Calendar.MONTH)+1)+
      "-"+date.get(Calendar.DAY_OF_MONTH)
    redis.del(config.filterMap+"_"+key)
  }

}
