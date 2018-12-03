import com.mclaudt.moneytransfer.account._

import scala.concurrent.{ExecutionContext, Future}

class AccountDAOMock(ec: ExecutionContext) extends AccountDAO{

  val mockAccount = Account(Some(1L),"mock",0)

  val mockTransferResult = Some(mockAccount,mockAccount)

  val mockDeleted = 1

  override def create(account: Account): Future[Account] = Future{mockAccount}(ec)

  override def delete(id: Long): Future[Int] = Future{mockDeleted}(ec)

  override def update(account: UpdateAccountDTO): Future[Option[Account]] = Future{Some(mockAccount)}(ec)

  override def getByName(name: String): Future[Seq[Account]] = Future{Seq(mockAccount)}(ec)

  override def getById(id: Long): Future[Option[Account]] = Future{Some(mockAccount)}(ec)

  override def transferFromTo(t:TransferDTO): Future[Option[(Account, Account)]] = Future{mockTransferResult}(ec)
}
