package com.mclaudt.moneytransfer

object Run extends App  {

  val s = new HttpCompleteService()

  scala.io.StdIn.readLine()

  s.close()
}


