package com.xuxue.football.spider

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, GregorianCalendar}

import com.xuxue.football.servlet.{CompanyMap, Game, Odds}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import com.xuxue.football.util.Loan.use
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpGet, HttpUriRequest}
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.slf4j.LoggerFactory

import collection.JavaConversions._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  *
  * @author xuxue
  */
class AokeFootballClient {

    val LOG = LoggerFactory.getLogger(classOf[AokeFootballClient])

    val client = HttpClients.custom().build()

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")

    /**
      * 请求这个日期对应的球探网足球赔率所对应的页面 返回这个页面的所有
      * 比赛的列表
      *
      * @param date 这个页面的日期
      * @return 这个页面的比赛列表
      */
    def requestGamePage(date: Date): mutable.Buffer[Try[Game]] = {
        val url = AokeFootballClient.getHomePageURLByDate(date)
        val get = new HttpGet(url)
        get.setConfig(AokeFootballClient.defaultHttpConfig)
        val document = AokeFootballClient.getPage(client, get, "gb2312", 0)
        val elementContent = document.select("#livescore_table").select("tr")
        LOG.info("size {}", elementContent.size())
        for (e <- elementContent if e.attr("matchid").nonEmpty) yield {
            val value = Try {
                val tds = e.select("td")
                val game = new Game()
                game.id = e.attr("matchid").toInt
                game.game = tds.get(1).text()
                game.source = 1
                game.gameTime = dateFormat.parse((date.getYear + 1900) + "-" + tds.get(2).text())
                game.status = tds.get(3).text().replaceAll("\"", "")
                game.morder = tds.get(4).select(".rank.float_l").text()
                game.mteam = tds.get(4).select(".ctrl_homename").text()
                game.score = tds.get(5).text()
                game.oteam = tds.get(6).select(".ctrl_awayname").text()
                game.oorder = tds.get(6).select(".rank.float_r").text()
                game
            }
            value match {
                case Failure(ex) => {
                    LOG.info("A game failed parse because " + ex.getMessage, ex)
                }
                case Success(v) => {
                    //do nothing
                }
            }
            value
        }
    }

    /**
      *
      * 请求这个比赛的赔率页面
      *
      * @param game 这个赔率页面对应的比赛
      * @return 携带赔率的比赛
      */
    def requestOddsPage(game: Game): Try[Game] = {
        val value = Try {
            val url = AokeFootballClient.getOddsPageURL(game)
            val get = new HttpGet(url)
            get.setConfig(AokeFootballClient.defaultHttpConfig)
            val document = AokeFootballClient.getPage(client, get, "gb2312", 0)
            val element = document.select("#lunci > div.qk_two").select("span")
            game.turn = element.text()
            val ajaxURL = AokeFootballClient.getOddsAjaxURL(game, 0)
            val ajaxGet = new HttpGet(ajaxURL)
            val ajaxDocument = AokeFootballClient.getAjaxPage(client, game, 3)
            val elements = ajaxDocument.select("tr")
            analyzeOddsTable(elements, game, 1)
        }

        value match {
            case Failure(ex) => {
                LOG.info("A game failed request odds because " + ex.getMessage, ex)
            }
            case Success(v) => {
                //do nothing
            }
        }
        value
    }

    /**
      * 请求盘口页面
      *
      * @param game 与这个盘口页面对应的比赛
      * @return 携带盘口信息的比赛
      */
    def requestPankouPage(game: Game): Try[Game] = {
        val value = Try {
            val get = new HttpGet(AokeFootballClient.getPanKouPageURL(game))
            get.setConfig(AokeFootballClient.defaultHttpConfig)
            val document = AokeFootballClient.getPage(client, get, "gbk", 0)
            val elements = document.select("#datatable1 > table").select("tr")
            analyzeOddsTable(elements, game, 2)
        }
        value match {
            case Failure(ex) => {
                LOG.info("A game failed request panKou because " + ex.getMessage, ex)
            }
            case Success(v) => {
                //do nothing
            }
        }
        value
    }

    /**
      * 请求单场页面
      *
      * @return 网站默认的所有单场比赛
      */
    def requestGameDanChang(): mutable.Buffer[Try[Game]] = {
        val qichi = AokeFootballClient.getDanChangPageURLs()
        val all = qichi.map {
            q =>
                val request = new HttpGet("http://www.okooo.com/livecenter/danchang/?date=" + q)
                request.setConfig(AokeFootballClient.defaultHttpConfig)
                val document = AokeFootballClient.getPage(client, request, "gb2312", 0)
                val elements = document.select("#livescore_table > table").select("tr")
                for (e <- elements if e.attr("matchid").nonEmpty) yield {
                    Try {
                        val game = new Game
                        val tds=e.select("td")
                        game.source = 1
                        game.id = e.attr("matchid").toInt
                        game.danchang = q
                        game
                    }
                }
        }
        all.flatMap(_.toList)
    }

    /**
      *
      * @return 默认的所有足彩页面
      */
    def requestGameZuCai(): mutable.Buffer[Try[Game]] = {
        val qichi = AokeFootballClient.getZuCaiPageURLs()
        val all = qichi.map {
            q =>
                val request = new HttpGet("http://www.okooo.com/livecenter/danchang/?date=" + q)
                request.setConfig(AokeFootballClient.defaultHttpConfig)
                val document = AokeFootballClient.getPage(client, request, "gb2312", 0)
                val elements = document.select("#livescore_table > table").select("tr")
                for (e <- elements if (e.attr("matchid") != null)) yield {
                    Try {
                        val game = new Game
                        game.source = 1
                        game.id = e.attr("matchid").toInt
                        game.danchang = q
                        game
                    }
                }
        }
        all.flatMap(_.toList)
    }


    def requestGameJingcai(date:Date):mutable.Buffer[Try[Game]]={
        val url=AokeFootballClient.getJingCaiPageURLByDate(date)
        val get=new HttpGet(url)
        get.setConfig(AokeFootballClient.defaultHttpConfig)
        val page=AokeFootballClient.getPage(client,get,"gb2312",0)
        val trs=page.select("#livescore_table > table").select("tr")
        for (e <- trs if (e.attr("matchid") != null)) yield {
            Try {
                val game = new Game
                game.source = 1
                game.id = e.attr("matchid").toInt
                game.jingcai=1
                game
            }
        }
    }

    /**
      * 请求这个月份的所有单场页面
      *
      * @param month
      * @return
      */
    def requestGameDanChang(month: Int) = {
        null;
    }

    /**
      * 请求这个年份的说有足彩页面
      *
      * @param year
      */
    def requestGameZuCai(year: Int) = {

    }

    def close(): Unit = {
        client.close()
    }


    //*************************私有方法********************************************

    /**
      *
      * 解析赔率的时间
      *
      * @param time
      * @param date
      * @return
      */
    private def parseOddsTime(time: String, date: Date): Try[Date] = {
        //*****************方法里面的方法
        def hour(): Try[Int] = {
            val regex = "赛前[0-9]*?时".r
            val m = regex.pattern.matcher(time)
            Try {
                if (m.find()) m.group().replaceAll("赛前", "").replaceAll("时", "").toInt
                else throw new IllegalArgumentException()
            }
        }
        def minute(): Try[Int] = {
            val regex = "时[0-9]*?分".r
            val m = regex.pattern.matcher(time)
            Try {
                if (m.find()) m.group().replaceAll("时", "").replaceAll("分", "").toInt
                else throw new IllegalArgumentException()
            }
        }

        Try {
            val calendar = new GregorianCalendar()
            calendar.setTime(date)
            calendar.add(Calendar.HOUR, -(hour.get))
            calendar.add(Calendar.MINUTE, -(minute.get))
            calendar.getTime
        }
    }


    /**
      *
      * 解析足球赔率数据
      *
      * @param elements
      * @param game
      * @param oddsType
      * @return
      */
    private def analyzeOddsTable(elements: Elements, game: Game, oddsType: Int): Game = {
        for (element <- elements) {
            val tds = element.select("td")
            val companyName = tds.get(1).text()
            if (CompanyMap.map.get(companyName) != null) {
                val odds = new Odds()
                odds.source = game.source
                odds.id = game.id
                odds.company = companyName
                val date = parseOddsTime(tds.get(2).attr("title"), game.gameTime)
                LOG.warn(tds.get(2).attr("title") + "    " + dateFormat.format(date.get))
                odds.time = date match {
                    case Success(v) => v
                    case Failure(ex) => {
                        LOG.warn("" + game.id + " " + odds.company + "  not set update time", ex)
                        null
                    }
                }
                odds.oddsType = oddsType
                odds.data1 = tds.get(1).text()
                odds.data2 = tds.get(2).text()
                odds.data3 = tds.get(3).text()
                game.addOdds(odds)
            }
        }
        game
    }

}


object AokeFootballClient {

    val LOG = LoggerFactory.getLogger(classOf[AokeFootballClient])
    val homePageBaseURL = "http://www.okooo.com/livecenter/football/"
    val jingCaiBaseURL = "http://www.okooo.com/livecenter/jingcai/"
    val danChangBaseURL = "http://www.okooo.com/livecenter/danchang/"
    val zuCaiBaseURL = "http://www.okooo.com/livecenter/zucai/"

    def getHomePageURLByDate(date: Date): String = {
        val calendar = new GregorianCalendar()
        calendar.setTime(date)
        homePageBaseURL + "?date=" + calendar.get(Calendar.YEAR) + "-" +
                fillNumber(calendar.get(Calendar.MONTH) + 1) + "-" +
                fillNumber(calendar.get(Calendar.DAY_OF_MONTH))
    }

    def getOddsPageURL(game: Game): String = "http://www.okooo.com/soccer/match/" + game.id + "/odds/"


    def getPanKouPageURL(game: Game): String = "http://www.okooo.com/soccer/match/" + game.id + "/ah/"


    def getOddsAjaxURL(game: Game, page: Int): String = "http://www.okooo.com/soccer/match/" + game.id + "/odds/ajax/?page=" + page + "&companytype=BaijiaBooks&type=1"


    def getPage(client: CloseableHttpClient, httpUriRequest: HttpUriRequest, charset: String, times: Int): Document = {
        val result =
            Try {
                use(client.execute(httpUriRequest)) {
                    res =>
                        val content = EntityUtils.toString(res.getEntity, charset)
                        Jsoup.parse(content, httpUriRequest.getURI.toString)
                }
            }
        result match {
            case Success(v) => {
                LOG.info("Request " + httpUriRequest.getURI.toString + " Success")
                v
            }
            case Failure(ex) => {
                LOG.info("Request " + httpUriRequest.getURI.toString + " failed will sleep " + Math.pow(2, times) + "s", ex)
                Thread.sleep(Math.pow(2, times).toInt * 1000)
                getPage(client, httpUriRequest, charset, times + 1)
            }
        }
    }

    def getAjaxPage(client: CloseableHttpClient, game: Game, maxPage: Int): Document = {
        var continue = true;
        var content = ""
        for (page <- 0 to maxPage - 1 if continue) {
            val pageContent = requestAjaxPage(client, 0, page, game)
            continue = pageContent.length > 500
            if (continue) {
                content += pageContent
                Thread.sleep(200)
            }
        }
        content = "<table>" + content + "</table>"
        Jsoup.parse(content, getOddsAjaxURL(game, 0))
    }


    private def requestAjaxPage(client: CloseableHttpClient, times: Int, page: Int, game: Game): String = {
        val url = getOddsAjaxURL(game, page)
        val httpGet = new HttpGet(url)
        httpGet.setConfig(defaultHttpConfig)
        val result = Try {
            use(client.execute(httpGet)) {
                res =>
                    EntityUtils.toString(res.getEntity, "utf-8")
            }
        }
        result match {
            case Success(v) => {
                LOG.info("Request " + httpGet.getURI.toString + " Success")
                v
            }
            case Failure(ex) => {
                LOG.info("Request " + httpGet.getURI.toString + "  occur exception" + ex.getMessage + "  will sleep " + Math.pow(2, times) + " s", ex)
                Thread.sleep(Math.pow(2, times).toInt * 1000)
                requestAjaxPage(client, times, page, game)
            }
        }
    }


    def getJingCaiPageURLByDate(date: Date): String = {
        val calendar = new GregorianCalendar()
        calendar.setTime(date)
        jingCaiBaseURL + "?date=" + calendar.get(Calendar.YEAR) + "-" +
                fillNumber(calendar.get(Calendar.MONTH) + 1) +
                fillNumber(calendar.get(Calendar.DAY_OF_MONTH))
    }


    def defaultHttpConfig = RequestConfig.custom().setConnectTimeout(5000)
            .setSocketTimeout(10000)
            .setConnectionRequestTimeout(5000)
            .build()

    def getDanChangPageURLs(): mutable.Buffer[Int] = {
        use(HttpClients.createDefault()) {
            client =>
                val get = new HttpGet(danChangBaseURL)
                get.setConfig(defaultHttpConfig)
                use(client.execute(get)) {
                    res =>
                        val html = Jsoup.parse(EntityUtils.toString(res.getEntity, "gb2312"), get.getURI.toString)
                        val href = html.select("#show_qihao_list").select("a")
                        for (value <- href) yield {
                            (value.text().replaceAll("期", "")).toInt
                        }
                }
        }
    }

    def getZuCaiPageURLs(): mutable.Buffer[Int] = {
        use(HttpClients.createDefault()) {
            client =>
                val get = new HttpGet(zuCaiBaseURL)
                get.setConfig(defaultHttpConfig)
                use(client.execute(get)) {
                    res =>
                        val html = Jsoup.parse(EntityUtils.toString(res.getEntity, "gb2312"), get.getURI.toString)
                        val href = html.select("#show_qihao_list").select("a")

                        for (value <- href) yield {
                            (value.text().replaceAll("期", "")).toInt
                        }
                }
        }
    }

    def fillNumber(number: Int): String = {
        if (number <= 9) "0" + number else number.toString
    }
}
