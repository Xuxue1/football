package com.xuxue.football.spider


import java.text.SimpleDateFormat
import java.util.{Calendar, Date, GregorianCalendar}

import com.xuxue.football.data.MySQLGameInsert
import com.xuxue.football.message.RedisMessageManager
import com.xuxue.football.servlet.Game
import com.xuxue.football.util.{Config, FootballRedisClient}
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  * Created by liuwei on 2016/9/26.
  */
class AoKeSpider(init: Boolean, var startTime: Date, var endTime: Date) extends Runnable {


    val LOG=LoggerFactory.getLogger(classOf[AoKeSpider])

    val conf = Config("conf.properties")

    val judgmentEnd = 5

    val aokeFootballClient = new AokeFootballClient

    val messageManager = new RedisMessageManager


    val pipleLine=new MySQLGameInsert(conf)

    val redisClient = new FootballRedisClient(conf)

    var noElmentPage = 0;

    val endGame={
        val game=new Game
        game.processStatus = -1
        game
    }

    override def run(): Unit = {
        if(startTime==null && endTime==null){
            while(true){
                spiderInit()
                processFootballMassage()
                new OddsTypeUpdate().run()
            }
        }else{
            spiderInit()
            processFootballMassage()
        }
    }


    @tailrec
    final def processFootballMassage(): Unit = {

        val game = messageManager.pop()

        game.processStatus match {
            case -1=>{
                //接收到位-1的信号 表示程序要退出
                messageManager.push(endGame)
                return
            }
            case 1 => {
                Try {
                    val r = aokeFootballClient.requestOddsPage(game)
                    aokeFootballClient.requestPankouPage(r.get).get
                } match {
                    case Success(v) => pipleLine.pipeline(v)
                    case Failure(ex) => LOG.info("Fail download a game",ex)

                }
                if (game.flage == 1) {
                    val games=requestNexDate(game.gameTime)
                    games match {
                        case Success(value) =>{
                            value.last.flage=1
                            value.foreach(messageManager.push(_))
                        }
                        case Failure(ex)=>messageManager.push(endGame)
                    }
                }
            }
        }
        processFootballMassage()
    }


    def nexDate(data:Date):Try[Date]={
        val calendar = new GregorianCalendar()
        calendar.setTime(data)
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        if(endTime!=null&&calendar.getTime.getTime > endTime.getTime) Failure(new IllegalArgumentException)
        else if(noElmentPage>judgmentEnd) Failure(new IllegalArgumentException)
        else Success(calendar.getTime)
    }


    final def requestNexDate(date: Date):Try[mutable.Buffer[Game]]={
        val games = aokeFootballClient.requestGamePage(date)
        val result = for (game <- games if game.isSuccess) yield game.get
        if(result.size>0){
            noElmentPage=0
            result.last.flage=1
            Success(result)
        }else{
            noElmentPage+=1
            if(noElmentPage>judgmentEnd){
                return Failure(new IllegalArgumentException)
            }
            Try{
                val next=nexDate(date)
                requestNexDate(next.get).get
            }
        }
    }


    def spiderInit(): Unit = {
        if (init) {
            if (startTime == null) startTime = new Date()
            val calendar = new GregorianCalendar()
            calendar.setTime(new Date())
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            val games = aokeFootballClient.requestGamePage(calendar.getTime)
            val result = for (game <- games if game.isSuccess) yield{
                val g=game.get
                g.processStatus=1;
                g
            }
            messageManager.init()
            result.last.flage = 1
            result.foreach(messageManager.push(_))
        }
    }

}

object AoKeSpider {

    def main(args: Array[String]): Unit = {
        val dateformate=new SimpleDateFormat("yyyy-MM-dd")
        val spider=if(args.length == 0) new AoKeSpider(true,null,null)
        else if(args.length == 1) new AoKeSpider(args(0).toBoolean,null,null)
        else if(args.length==2) new AoKeSpider(args(0).toBoolean,dateformate.parse(args(1)),null)
        else if(args.length==3) new AoKeSpider(args(0).toBoolean,dateformate.parse(args(1)),dateformate.parse(args(2)));
        else throw new Error("illeaglue Argument");
        new Thread(spider).start()
    }

}

