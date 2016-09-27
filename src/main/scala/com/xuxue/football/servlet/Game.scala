package com.xuxue.football.servlet

import java.util
import java.util.Date

/**
  * Created by liuwei on 2016/9/23.
  */
class Game {
  var id=0;   //这条数据的id
  var source=0;  //这条数据的来源网站
  var game:String=null //联赛名
  var gameTime:Date=null //开赛事件
  var status:String=null; //状态  正在进行  或者已经结束
  var morder:String=null; //主队排名
  var mteam:String=null;  //主队
  var score:String=null;  //比分
  var oteam:String=null;  //客队
  var oorder:String=null;  //客队排名
  var jingcai:Int=0;        //是否是竞猜
  var danchang:Int=0;       //是否单场
  var zucai:Int=0;          //是否是足彩
  var turn:String=null;      //轮次
  var odds:util.HashSet[Odds]=new util.HashSet[Odds](); //比分
  def addOdds(odds:Odds):Game={
    this.odds.add(odds)
    this
  }

  override def toString = s"Game(id=$id, source=$source, game=$game, gameTime=$gameTime, status=$status, morder=$morder, mteam=$mteam, score=$score, oteam=$oteam, oorder=$oorder, jingcai=$jingcai, danchang=$danchang, zucai=$zucai, turn=$turn, odds=$odds)"
}
