package com.xuxue.football.data

import java.sql.{Date, DriverManager, Timestamp}
import java.util.concurrent.Executors

import com.xuxue.football.servlet.{Game, Odds}
import com.xuxue.football.util.{Config, FootballRedisClient}

import collection.JavaConversions._
import com.xuxue.football.util.Loan.use
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * Created by liuwei on 2016/9/23.
  */
class MySQLGameInsert(val config:Config) {


    val LOG=LoggerFactory.getLogger(classOf[MySQLGameInsert])

    val threadPool=Executors.newFixedThreadPool(50)

    val filter=new FootballRedisClient(config)

    val connection={
        Class.forName("com.mysql.jdbc.Driver")
        DriverManager.getConnection(config.mysqlURL,config.mysqlUser,config.mysqlPassword)
    }

    def pipeline(game:Game):Unit={
        val odds=game.odds
        saveGame(game)
        for(odd <- odds ){
            if(!filter.filter(odd)){
                saveOdds(game,odd)
                filter.updateMap(odd,"1")
            }
        }
    }


    private def saveGame(game: Game):Unit={

        val result=Try{
            use(connection.prepareStatement("insert into game value(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")){
                pre=>
                    pre.setInt(1,game.source)
                    pre.setInt(2,game.id)
                    pre.setString(3,game.game)
                    pre.setTimestamp(4,new Timestamp(game.gameTime.getTime))
                    pre.setString(5,game.status)
                    pre.setString(6,game.morder)
                    pre.setString(7,game.mteam)
                    pre.setString(8,game.score)
                    pre.setString(9,game.oteam)
                    pre.setString(10,game.oorder)
                    pre.setInt(11,game.jingcai)
                    pre.setInt(12,game.danchang)
                    pre.setInt(13,game.zucai)
                    pre.setString(14,game.turn)
                    pre.executeUpdate()
            }
        }

        result match {
            case Success(v) =>LOG.info("Success insert a game")
            case Failure(ex)=>LOG.warn("Failure insert a game",ex)
        }

    }

    private def saveOdds(game: Game,odds: Odds): Unit ={

        val result=Try{
            use(connection.prepareStatement("insert into odds values(?,?,?,?,?,?,?,?)")){
                pre=>
                    pre.setInt(1,game.source)
                    pre.setInt(2,game.id)
                    pre.setString(3,odds.company)
                    pre.setInt(4,odds.oddsType)
                    pre.setTimestamp(5,new Timestamp(odds.time.getTime))
                    pre.setString(6,odds.data1)
                    pre.setString(7,odds.data2)
                    pre.setString(8,odds.data3)
                    pre.executeUpdate()
            }
        }
        result match {
            case Success(v) =>LOG.info("Success insert a odds")
            case Failure(ex)=>LOG.warn("Failure insert a odds",ex)
        }
    }

}

object MySQLGameInsert{
    def main(args: Array[String]): Unit = {
        println("Hello")
    }
}
