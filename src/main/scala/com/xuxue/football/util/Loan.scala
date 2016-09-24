package com.xuxue.football.util
/**
  *
  *
  *
  * @ time 2016-9-11
  *
  * @author xuxue
  *
  */
object Loan {
  def use[T<:{def close();},R](resource:T)(func:T=>R)={
    try{
      func(resource)
    }finally {
      resource.close()
    }
  }

}

