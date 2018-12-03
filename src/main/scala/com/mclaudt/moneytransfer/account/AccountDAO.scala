package com.mclaudt.moneytransfer.account

import scala.concurrent.Future


trait AccountDAO {

  def create(chain: Account): Future[Account]

  def delete(id: Long): Future[Int]

  def update(chain: UpdateAccountDTO): Future[Option[Account]]

  def getByName(name: String): Future[Seq[Account]]

  def getById(id: Long): Future[Option[Account]]

  def transferFromTo(t:TransferDTO):Future[Option[(Account,Account)]]

  def echo(account: Account):Future[Account] = Future.successful(account)

}

case class TransferDTO(fromId:Long, toId:Long, money:Long,idempotencyKey:Option[Long] = None)

case class UpdateAccountDTO(id: Long,
                            patch:UpdateAccountPatchDTO)

case class UpdateAccountPatchDTO(name: Option[String],
                                 money: Option[Long]) {

}







