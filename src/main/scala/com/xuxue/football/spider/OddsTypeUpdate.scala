package com.xuxue.football.spider

import java.util.{Calendar, GregorianCalendar, TimerTask}

import com.xuxue.football.data.MySQLGameInsert
import com.xuxue.football.util.Config
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

/**
  * Created by Administrator on 2016/10/3.
  */
class OddsTypeUpdate extends TimerTask {

    val LOG=LoggerFactory.getLogger(classOf[OddsTypeUpdate])

    override def run(): Unit = {

        val client=new AokeFootballClient
        val install=new MySQLGameInsert(Config("conf.properties"));
        client.requestGameZuCai().map {
            _ match {
                case Success(v) => install.pipeline(v)
                case Failure(ex) => LOG.warn("a game request error", ex)
            }
        }
        client.requestGameDanChang().map{
            _ match {
                case Success(v) => install.pipeline(v)
                case Failure(ex) => LOG.warn("a game request error",ex)
            }
        }
        val calendar=new GregorianCalendar()

       def requestJingCai(): Unit ={
           val games=client.requestGameJingcai(calendar.getTime)
            if(games.size !=0 ){
                games.map{
                    _ match {
                        case Success(v) => install.pipeline(v)
                        case Failure(ex) => LOG.warn("a game request error",ex)
                    }
                }
                calendar.add(Calendar.DAY_OF_MONTH,1)
                requestJingCai()
            }
       }

        requestJingCai()
    }

}
