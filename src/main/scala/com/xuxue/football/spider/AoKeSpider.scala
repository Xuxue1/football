package com.xuxue.football.spider

import java.util.Date

import scala.util.{Failure, Success}

/**
  * Created by liuwei on 2016/9/26.
  */
class AoKeSpider {

}


object AoKeSpider {

    def main(args: Array[String]): Unit = {

        val client=new AokeFootballClient
        val games=client.requestGamePage(new Date())
        val result=for(game <- games if game.isSuccess)yield {
            client.requestOddsPage(game.get)
        }

        result.map{
            r=>
                if(r.isSuccess) println(r)
        }
    }

}
