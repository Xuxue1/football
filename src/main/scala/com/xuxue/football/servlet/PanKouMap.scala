package com.xuxue.football.servlet

import java.sql.DriverManager
import java.util

import com.xuxue.football.util.Config
import com.xuxue.football.util.Loan.use

import scala.util.{Failure, Success, Try}

/**
  * Created by liuwei on 2016/9/29.
  */
object PanKouMap {

    val config=Config("conf/aoke.conf")

    val map={
        val map=new util.HashMap[String,Integer]();
        Class.forName("com.mysql.jdbc.Driver")
        val connection=DriverManager.getConnection(config.mysqlURL,config.mysqlUser,config.mysqlPassword)
        use(connection.prepareStatement("select * from aoke_pankou")){
            sta=>
                use(sta.executeQuery()){
                    set=>
                        while(set.next()){
                            map.put(set.getString("name"),set.getInt("value"))
                        }
                }
        }
        map
    }

    def getPankouNumber(name:String):Int={
        val value=map.get(name)
        if(value==null){
            updeteMap()
            if(!map.containsKey(name)){
                val x=insert(name)
                x match {
                    case Success(v) =>println("success insert  "+v)
                    case Failure(ex)=>ex.printStackTrace()
                }
                return getPankouNumber(name)
            }else{
                return map.get(name)
            }
        }
        return value
    }


    private def updeteMap():Unit={
        map.clear()
        Class.forName("com.mysql.jdbc.Driver")
        val connection=DriverManager.getConnection(config.mysqlURL,config.mysqlUser,config.mysqlPassword)
        use(connection.prepareStatement("select * from aoke_pankou")){
            sta=>
                use(sta.executeQuery()){
                    set=>
                        while(set.next()){
                            map.put(set.getString("name"),set.getInt("value"))
                        }
                }
        }
    }

    private def insert(name:String): Try[Int] = {
        Try {
            Class.forName("com.mysql.jdbc.Driver")
            val connection=DriverManager.getConnection(config.mysqlURL,config.mysqlUser,config.mysqlPassword)
            val maxSize=use(connection.prepareStatement("select max(value) as value from aoke_pankou")){
                sta=>
                    use(sta.executeQuery()){
                        set=>
                            if(set.next()) set.getInt("value") else -1
                    }
            }
            if(maxSize== -1) throw new IllegalArgumentException
            use(connection.prepareStatement("insert into aoke_pankou values(?,?)")){
                sta=>
                    sta.setString(1,name)
                    sta.setInt(2,maxSize+1)
                    sta.executeUpdate()
            }
            map.put(name,maxSize+1)
            maxSize+1
        }
    }
}
