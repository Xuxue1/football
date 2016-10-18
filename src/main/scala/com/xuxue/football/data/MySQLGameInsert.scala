package com.xuxue.football.data

import java.sql.{DriverManager, Timestamp}

import com.xuxue.football.servlet.{CompanyMap, Game, Odds, PanKouMap}
import com.xuxue.football.util.{Config, FootballRedisClient}

import collection.JavaConversions._
import com.xuxue.football.util.Loan.use
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * @author xuxue
  */
class MySQLGameInsert(val config: Config) {


    val LOG = LoggerFactory.getLogger(classOf[MySQLGameInsert])

    val filter = new FootballRedisClient(config)

    var connection = {
        Class.forName("com.mysql.jdbc.Driver")
        DriverManager.getConnection(config.mysqlURL, config.mysqlUser, config.mysqlPassword)
    }

    def reconnect():Unit={
        this.connection=DriverManager.getConnection(config.mysqlURL, config.mysqlUser, config.mysqlPassword)
    }

    def pipeline(game: Game): Unit = {
        val odds = game.odds
        saveGame(game)
        for (odd <- odds) {
            if (!filter.filter(odd)) {
                saveOdds(game, odd)
                filter.updateMap(odd, "1")
            }else{
                LOG.info("odds exist");
            }
        }
    }

    def close(): Unit = {
        filter.close()
        connection.close()
    }

     def updateGame(game:Game):Try[Int]={
         if(game.jingcai !=0 ){
             update("update game set jingcai=? where id=? and source=?",
                 Array(game.jingcai,game.id,game.source))
         }else if(game.zucai !=0 ){
             update("update game set zucai=? where id=? and source=?",
                 Array(game.zucai,game.id,game.source))
         }else if(game.danchang != 0){
             update("update game set danchang=? where id=? and source=?",
                 Array(game.danchang,game.id,game.source))
         }else{
             update("update game set gametime=?,score=? where id=? and source=?",
                 Array(new Timestamp(game.gameTime.getTime),game.score,game.id,game.source))
         }
    }

    def update(sql:String,data:Array[Any]):Try[Int]={
        Try{
            use(connection.prepareStatement("insert into game value(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")){
                pre=>
                    for(i <- 0 to data.length){
                        pre.setObject(i+1,data(i))
                    }
                    pre.executeUpdate()
            }
        }
    }

    private def saveGame(game: Game): Unit = {

        if(game.turn == null ) updateGame(game)
        val result = Try {
            use(connection.prepareStatement("insert into game value(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                pre =>
                    pre.setInt(1, game.source)
                    pre.setInt(2, game.id)
                    pre.setString(3, game.game)
                    pre.setTimestamp(4, new Timestamp(game.gameTime.getTime))
                    pre.setString(5, game.status)
                    pre.setString(6, game.morder)
                    pre.setString(7, game.mteam)
                    pre.setString(8, game.score)
                    pre.setString(9, game.oteam)
                    pre.setString(10, game.oorder)
                    pre.setInt(11, game.jingcai)
                    pre.setInt(12, game.danchang)
                    pre.setInt(13, game.zucai)
                    pre.setString(14, game.turn)
                    pre.executeUpdate()
            }
        }

        result match {
            case Success(v) => LOG.info("Success insert a game")
            case Failure(ex) => updateGame(game)
        }

    }

    private def saveOdds(game: Game, odds: Odds): Unit = {
        val result = Try {
            use(connection.prepareStatement("insert into odds values(?,?,?,?,?,?,?,?)")) {
                LOG.info("odds= {}"+odds.toString)
                pre =>
                    pre.setInt(1, game.source)
                    pre.setInt(2, game.id)
                    pre.setInt(3, CompanyMap.map.get(odds.company))
                    pre.setInt(4, odds.oddsType)
                    pre.setTimestamp(5, new Timestamp(odds.time.getTime))
                    pre.setDouble(6, odds.data1.toDouble)
                    if (odds.oddsType == 1) pre.setDouble(7, odds.data2.toDouble)
                    else pre.setDouble(7, PanKouMap.getPankouNumber(odds.data2))
                    pre.setDouble(8, odds.data3.toDouble)
                    pre.executeUpdate()
            }
        }
        result match {
            case Success(v) => LOG.info("Success insert a odds")
            case Failure(ex) => LOG.warn("Failure insert a odds", ex)
        }
    }
}


