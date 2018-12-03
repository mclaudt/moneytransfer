package com.mclaudt.moneytransfer.account

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


class AccountDAOSlick(val dbConfig: DatabaseConfig[JdbcProfile], executionContextForBlockingDBOperations: ExecutionContext, val config: IdempotencyKeySupportConfig) extends AccountDAO with IdempotencyKeySupport[Long, Option[(Account, Account)]] with Tables {

  // https://stackoverflow.com/questions/35510070/best-practice-for-slick-2-1-3-execution-context-usage
  // Slick’s API is fully asynchronous and runs database call in a separate thread pool.
  implicit val ec: ExecutionContext = executionContextForBlockingDBOperations

  override val profile = dbConfig.profile

  import dbConfig.profile.api._

  override def create(a: Account): Future[Account] = dbConfig.db.run(
    (for {
      chainId <- Accounts.returning(Accounts.map(s => s.id)) += AccountRow(id = None, name = a.name, money = a.money)
      newAccount <- Accounts.filter(_.id === chainId).result.head
    } yield toAccount(newAccount)).transactionally
  )

  override def delete(id: Long): Future[Int] = dbConfig.db.run(
    (for {
      chainId <- Accounts.filter(_.id === id)
    } yield chainId).delete
  )

  override def update(a: UpdateAccountDTO): Future[Option[Account]] = {
    dbConfig.db.run(
      for {
        existingAccount <- Accounts.filter(_.id === a.id).result.headOption
      } yield existingAccount
    ).map(_.map(toAccount)).flatMap {
      oa =>
        if (oa.isDefined) {
          dbConfig.db.run(
            for {
              updatedCount <- (for {c <- Accounts if c.id === a.id} yield (c.name, c.money)).update((a.patch.name.getOrElse(oa.get.name), a.patch.money.getOrElse(oa.get.money)))
              optAccount <- Accounts.filter(_.id === a.id).result.headOption
            } yield optAccount.map(toAccount)
          )
        } else {
          Future {
            None
          }
        }
    }
  }

  override def getByName(name: String): Future[Seq[Account]] = dbConfig.db.run(
    Accounts.filter(a => a.name === name).result
  ).map(_.map(toAccount))(ec)

  override def getById(id: Long): Future[Option[Account]] = dbConfig.db.run(
    Accounts.filter(a => a.id === id).result.headOption
  ).map(_.map(toAccount))(ec)

  override def transferFromTo(t: TransferDTO): Future[Option[(Account, Account)]] = {
    tick()

    if (t.idempotencyKey.isEmpty) {
      dbConfig.db.run {
        directTransactionDBIO(t)
      }
    } else {
      if (hasNotOutdatedKey(t.idempotencyKey.get)) {
        Future {
          getResult(t.idempotencyKey.get)
        }
      } else {
        dbConfig.db.run {
          directTransactionDBIO(t)
        }.map(newRes => {
          writeResult(t.idempotencyKey.get, newRes)
          newRes
        })
      }
    }
  }

  private def directTransactionDBIO(t: TransferDTO): DBIO[Option[(Account, Account)]] = {
    (for {
      from <- Accounts.filter(a => a.id === t.fromId).result.headOption //if from.exists(_.money >= t.money)
      to <- Accounts.filter(a => a.id === t.toId).result.headOption if from.nonEmpty
      _ <- Accounts.filter(a => a.id === t.fromId).map(_.money).update(from.get.money - t.money)
      _ <- Accounts.filter(a => a.id === t.toId).map(_.money).update(to.get.money + t.money)
      newFrom <- Accounts.filter(a => a.id === t.fromId).result.headOption
      newTo <- Accounts.filter(a => a.id === t.toId).result.headOption
    } yield newFrom.flatMap(nf => newTo.map(nt => (toAccount(nf), toAccount(nt))))).transactionally
  }

  def toAccount(x: AccountRow): Account = {
    Account(x.id, x.name, x.money)
  }
}


