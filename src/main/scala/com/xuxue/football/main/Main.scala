package com.xuxue.football.main

import com.xuxue.football.util.Loan.use
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

/**
  * Created by liuwei on 2016/9/28.
  */
object Main {

    def main(args: Array[String]): Unit = {
        val get=new HttpGet("https://www.taobao.com/")
        use(HttpClients.createDefault()){
            client=>
                use(client.execute(get)){
                    res=>
                        println(EntityUtils.toString(res.getEntity))
                }
        }
    }

}
