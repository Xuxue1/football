package com.xuxue.football.test

import java.util.Date

import com.xuxue.football.spider.AokeFootballClient

import scala.util.{Failure, Success}

/**
  * Created by liuwei on 2016/10/12.
  */
object TestJingCaiRequest {
    def main(args: Array[String]): Unit = {

        val client=new AokeFootballClient
        val r=client.requestGameJingcai(new Date)

        r.map{
            res=>
                res match {
                    case Success(v) => println(v)
                    case Failure(ex) => ex.printStackTrace();
                }

        }


    }
}
