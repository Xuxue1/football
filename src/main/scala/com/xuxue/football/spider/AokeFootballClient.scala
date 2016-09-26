package com.xuxue.football.spider

import java.io.IOException
import java.util.{ArrayList, Calendar, Date, GregorianCalendar}

import com.xuxue.football.servlet.Game
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import com.xuxue.football.util.Loan.use
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpGet, HttpUriRequest}
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory

import collection.JavaConversions._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  * Created by liuwei on 2016/9/23.
  */
class AokeFootballClient {

    val LOG=LoggerFactory.getLogger(classOf[AokeFootballClient])

    val client=HttpClients.custom().build()

    def requestGamePage(date:Date):ArrayList[Game]={

      val url=AokeFootballClient.getHomePageURLByDate(date)
      val get=new HttpGet(url)
      get.setConfig(AokeFootballClient.defaultHttpConfig)
      val document=AokeFootballClient.getPage(client,get,"gb2312",0)
      val elementContent=document.select("#livescore_table").select("tr");
      LOG.info("size {}",elementContent.size())
      elementContent.map{
        data=>
          val tds=data.select("td");
          val game=new Game()
          game.id=data.attr("matchid").toInt

      }
      null;
    }

    def requestOddsPage(game:Game):Game={
      null;
    }

    def requestGameDanChang():Array[Game]={
      null;
    }

    def requestGameZuCai():Array[Game]={
      null;
    }

    def requestGameDanChang(month:Int)={
      null;
    }

    def requestGameZuCai(year:Int)={

    }


    def requestPanKouPage(game:Game)={

    }

}


object AokeFootballClient{

  val LOG=LoggerFactory.getLogger(getClass)

  val homePageBaseURL="http://www.okooo.com/livecenter/football/";
  val jingCaiBaseURL="http://www.okooo.com/livecenter/jingcai/";
  val danChangBaseURL="http://www.okooo.com/livecenter/danchang/";
  val zuCaiBaseURL="http://www.okooo.com/livecenter/zucai/";

  def getHomePageURLByDate(date:Date):String={
    val calendar=new GregorianCalendar()
    calendar.setTime(date)
    homePageBaseURL+"?date="+calendar.get(Calendar.YEAR)+"-"+
      fillNumber(calendar.get(Calendar.MONTH)+1)+"-"+
      fillNumber(calendar.get(Calendar.DAY_OF_MONTH))
  }


  def getPage(client:CloseableHttpClient, httpUriRequest: HttpUriRequest, charset:String, times:Int):Document={
    val result=
      Try{
        use(client.execute(httpUriRequest)){
          res=>
            val content=EntityUtils.toString(res.getEntity,charset)
            Jsoup.parse(content,httpUriRequest.getURI.toString)
        }
      }
    result match {
      case Success(v)=> v
      case Failure(ex)=>{
        Thread.sleep(Math.pow(2,times).toInt*1000)
        getPage(client,httpUriRequest,charset,times+1)
      }
    }

  }



  def getJingCaiPageURLByDate(date:Date):String={
    val calendar=new GregorianCalendar()
    calendar.setTime(date)
    jingCaiBaseURL+"?date="+calendar.get(Calendar.YEAR)+"-"+
      fillNumber(calendar.get(Calendar.MONTH)+1)+
      fillNumber(calendar.get(Calendar.DAY_OF_MONTH))
  }


  def defaultHttpConfig=RequestConfig.custom().setConnectTimeout(5000)
                        .setSocketTimeout(10000)
                        .setConnectionRequestTimeout(5000)
                        .build()

  def getDanChangPageURLs():mutable.Buffer[String]={
    use(HttpClients.createDefault()){
      client=>
        val get=new HttpGet(danChangBaseURL)
        get.setConfig(defaultHttpConfig)
        use(client.execute(get)){
          res=>
            val html=Jsoup.parse(EntityUtils.toString(res.getEntity,"gb2312"),get.getURI.toString)
            val href=html.select("#show_qihao_list").select("a")
            for(value <- href)yield{
              val url=danChangBaseURL+"?date="+(value.text().replaceAll("期",""))
              url
            }
        }
    }
  }

  def getZuCaiPageURLs():mutable.Buffer[String]={
    use(HttpClients.createDefault()){
      client=>
        val get=new HttpGet(zuCaiBaseURL)
        get.setConfig(defaultHttpConfig)
        use(client.execute(get)){
          res=>
            val html=Jsoup.parse(EntityUtils.toString(res.getEntity,"gb2312"),get.getURI.toString)
            val href=html.select("#show_qihao_list").select("a")

            for(value <- href)yield{
              val url=zuCaiBaseURL+"?date="+(value.text().replaceAll("期",""))
              url
            }
        }
    }
  }

  def fillNumber(number:Int):String={
    if(number<=9) "0"+number else number.toString
  }


  def main(args: Array[String]): Unit = {
    val x=new AokeFootballClient
    x.requestGamePage(new Date())
  }
}
