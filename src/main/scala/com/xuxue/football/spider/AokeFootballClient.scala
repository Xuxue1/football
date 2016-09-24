package com.xuxue.football.spider

import java.util.{ArrayList, Date}

import com.xuxue.football.servlet.Game
import org.apache.http.impl.client.HttpClients

/**
  * Created by liuwei on 2016/9/23.
  */
class AokeFootballClient(connectTimeout:Int,readTimeout:Int,requestTimeout:Int) {

    val client=HttpClients.custom().build()

    def requestGamePage(date:Date):ArrayList[Game]={

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

}
