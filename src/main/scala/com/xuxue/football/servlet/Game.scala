package com.xuxue.football.servlet

import java.util
import java.util.Date

/**
  * Created by liuwei on 2016/9/23.
  */
class Game {
  var id=0;
  var source=0;
  var game:String=null
  var gameTime:Date=null
  var status:Int=0;
  var morder:String=null;
  var mteam:String=null;
  var score:String=null;
  var oteam:String=null;
  var ordered:String=null;
  var jingcai:Int=0;
  var danchang:Int=0;
  var zucai:Int=0;
  var trun:String=null;
  var odds:util.HashSet[Odds]=new util.HashSet[Odds]();

  def addOdds(odds:Odds):Game={
    this.odds.add(odds)
    this
  }
}
