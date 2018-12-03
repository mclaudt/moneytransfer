package com.mclaudt.moneytransfer.account

import slick.jdbc.JdbcProfile

trait Tables {

  val profile: JdbcProfile

  import profile.api._

  class Accounts(tag: Tag) extends Table[AccountRow](tag,"ACCOUNTS") {
    val id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    val name = column[String]("name")
    val money = column[Long]("money")
    def * = (id.?, name, money) <> (AccountRow.tupled, AccountRow.unapply)
  }

  lazy val Accounts = TableQuery[Accounts]

}

case class AccountRow(id: Option[Long], name: String, money: Long)







