package com.xuxue.football.main

import java.text.SimpleDateFormat
import com.xuxue.football.spider.AoKeSpider
/**
  * Created by liuwei on 2016/9/28.
  */
object Main {

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
