package com.xuxue.football.servlet

import java.util.Date

/**
  * Created by liuwei on 2016/9/23.
  */
class Odds {
  var id=0;
  var source=0;
  var company:String=null; //公司
  var time:Date=null;
  var oddsType=0;   //赔率类型  1 表是欧赔  2是 盘口
  var data1:String=null;
  var data2:String=null;
  var data3:String=null;
  override def toString = s"Odds($id, $source, $company, $time, $oddsType, $data1, $data2, $data3)"
}
