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
  * 澳客网赔率爬虫的线程
  */
class AoKeSpider(init: Boolean, var startTime: Date, var endTime: Date) extends Runnable {

    /**
      * 日志
      */
    val LOG=LoggerFactory.getLogger(classOf[AoKeSpider])

    /**
      * 配置文件
      */
    val conf = Config("conf/aoke.conf")

    /**
      * 连续5个页面没有赔率认为爬取到结尾了
      */
    val judgmentEnd = 5

    /**
      * 访问澳客网的客户端
      */
    val aokeFootballClient = new AokeFootballClient

    /**
      * redis连接
      */
    val messageManager = new RedisMessageManager

    /**
      * 存储赔率
      */
    val pipleLine=new MySQLGameInsert(conf)

    /**
      * 过滤
      */
    val redisClient = new FootballRedisClient(conf)

    /**
      * 已经连续没有赔率的页面的数目
      */
    var noElmentPage = 0;

    /**
      * 接收到这种类型的game 就会停止抓取
      */
    val endGame={
        val game=new Game
        game.processStatus = -1
        game
    }

    override def run(): Unit = {
        /**
          * 如果开始和结束时间都没有指定 会一直循环抓取
          */
        if(startTime==null && endTime==null){
            while(true){
                spiderInit()
                processFootballMassage()
                new OddsTypeUpdate().run()
            }
            //如果指定了其中一个 就抓取一次
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
            case 0 =>{
                LOG.info("game = {}" ,game)
                System.exit(0)
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
        LOG.info("nex date call")
        val calendar = new GregorianCalendar()
        calendar.setTime(data)
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        LOG.info("return "+ calendar.getTime.toString)
        if(endTime!=null&&calendar.getTime.getTime > endTime.getTime) Failure(new IllegalArgumentException)
        else if(noElmentPage>judgmentEnd) Failure(new IllegalArgumentException)
        else Success(calendar.getTime)
    }


    final def requestNexDate(date: Date):Try[mutable.Buffer[Game]]={
        val nextDate=nexDate(date) match {
            case Success(v)=> v
            case Failure(ex)=> return Failure(ex)
        }
        val games = aokeFootballClient.requestGamePage(nextDate)
        val result = for (game <- games if game.isSuccess) yield {
            val g=game.get
            g.processStatus=1
            g
        }
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
            LOG.info("init spider")
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
        else if(args.length == 1){
            if(args(0).equals("?")){
                println("usag + isMaster startTime endTime");
                return
            }
            new AoKeSpider(args(0).toBoolean,null,null);
        }
        else if(args.length==2) new AoKeSpider(args(0).toBoolean,dateformate.parse(args(1)),null)
        else if(args.length==3) new AoKeSpider(args(0).toBoolean,dateformate.parse(args(1)),dateformate.parse(args(2)));
        else throw new Error("illeaglue Argument");
        new Thread(spider).start()
    }

}

