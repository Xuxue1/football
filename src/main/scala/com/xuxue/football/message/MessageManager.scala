package com.xuxue.football.message

import com.xuxue.football.servlet.Game

/**
  * Created by xuxue on 2016/9/28.
  */



trait MessageManager {

    def push(game:Game):Boolean

    def pop():Game

}
