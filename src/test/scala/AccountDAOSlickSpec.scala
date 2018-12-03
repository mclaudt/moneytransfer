import com.mclaudt.moneytransfer.account._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.language.implicitConversions


class AccountDAOSlickSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with AccountFixtures{

  import scala.concurrent.ExecutionContext.Implicits.global

  var databaseConfig: DatabaseConfig[JdbcProfile] = _

  val timeToLiveInMillis: Int = 1000*20

  var dao: AccountDAOSlick = _

  implicit def accountToUpdateAccountDTO(a:Account): UpdateAccountDTO = UpdateAccountDTO(a.id.get, UpdateAccountPatchDTO(Some(a.name),Some(a.money)))

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(10, Seconds)),
    interval = scaled(Span(20, Millis))
  )

  "AccountDao's create" should "create an account" in {

    val createdAccount = dao.create(accountFixture).futureValue

    val created: Option[Account] = dao.getById(createdAccount.id.get).futureValue
    created.size should equal (1)
    created.head should equal (accountFixture.copy(id = createdAccount.id))

  }


  "AccountDao's update" should "update the existing account" in  {

    val createdAccount = dao.create(accountFixture).futureValue

    val created: Option[Account] = dao.getById(createdAccount.id.get).futureValue


    val toUpdate = createdAccount.copy(name = createdAccount.name+"_updated")

    val updated: Option[Account] = dao.update(toUpdate).futureValue


    updated should equal (Some(toUpdate))



  }

  "AccountDao's update" should "not update the non-existing account" in  {


    val updated: Option[Account] = dao.update(accountNonExistingFixture).futureValue

    updated should equal (None)

  }


  "AccountDao's delete" should "delete the existing account" in  {

    val createdAccount = dao.create(accountFixture).futureValue

    val deleted: Int = dao.delete(createdAccount.id.get).futureValue

    deleted should equal (1)

  }

  "AccountDao's getByName" should "find by name all created accounts with same name" in  {

    val count = 10

    val name = "John"

    val accountToInsert = accountFixture.copy(name=name)

    val accountsToInsert = (1 to count).map(i=>accountToInsert)

    val insertedAccounts = accountsToInsert.map(a=>dao.create(accountToInsert).futureValue)

    val updated: Seq[Account] = dao.getByName(name).futureValue



    updated.size shouldBe count

    updated.forall(a=> a.name == accountToInsert.name && a.money == accountToInsert.money) shouldBe true


  }

  "AccountDao's getById" should "get created account by id " in {

    val createdAccount = dao.create(accountFixture).futureValue

    val awaitedResult =  Some(accountFixture.copy(id = createdAccount.id))

    val getByIdResultOpt: Option[Account] = dao.getById(createdAccount.id.get).futureValue

    getByIdResultOpt should equal (awaitedResult)

  }

  "AccountDao's getById" should "not find non existing account" in  {

    val nonExisting = dao.getById(60606).futureValue

    nonExisting.size should equal (0)

  }

  "AccountDao's transfer" should "transfer money from one account to another" in {

    val transferAmount = 1000

    val createdFromAccount = dao.create(accountFixture).futureValue

    val createdToAccount = dao.create(accountFixture).futureValue

    val awaitedResult = Some((
      accountFixture.copy(money = accountFixture.money-transferAmount).copy(id=createdFromAccount.id),
      accountFixture.copy(money = accountFixture.money+transferAmount).copy(id=createdToAccount.id)
    ))

    val transferResult: Option[(Account, Account)] = dao.transferFromTo(TransferDTO(createdFromAccount.id.get,createdToAccount.id.get,transferAmount)).futureValue

    transferResult should equal (awaitedResult)

  }

  "AccountDao's transfer" should "not transfer money more than once in case of idempotency key usage" in {

    val transferAmount = 10

    val count = 100

    val createdFromAccount = dao.create(accountFixture).futureValue

    val createdToAccount = dao.create(accountFixture).futureValue

    val awaitedResultAfterTransfers = Some((
      accountFixture.copy(money = accountFixture.money-transferAmount*count).copy(id=createdFromAccount.id),
      accountFixture.copy(money = accountFixture.money+transferAmount*count).copy(id=createdToAccount.id)
    ))

    val awaitedResultAfterTransfersAndPause = Some((
      accountFixture.copy(money = accountFixture.money-transferAmount*count*2).copy(id=createdFromAccount.id),
      accountFixture.copy(money = accountFixture.money+transferAmount*count*2).copy(id=createdToAccount.id)
    ))
//Они все должны исполниться
    val transfers = (1 to count).map(i=>dao.transferFromTo(TransferDTO(createdFromAccount.id.get,createdToAccount.id.get,transferAmount,Some(i))).futureValue).last should equal (awaitedResultAfterTransfers)

    //Потом надо их повторить и обнаружить тот же результат

    val repeatedTransfers = (1 to count).map(i=>dao.transferFromTo(TransferDTO(createdFromAccount.id.get,createdToAccount.id.get,transferAmount,Some(i))).futureValue).last should equal (awaitedResultAfterTransfers)

    //Потом надо сделать паузу, за которую они все протухнут

    Thread.sleep(timeToLiveInMillis+100)

    val repeatedTransfersAfterInvalidationByTime = (1 to count).map(i=>dao.transferFromTo(TransferDTO(createdFromAccount.id.get,createdToAccount.id.get,transferAmount,Some(i))).futureValue).last should equal (awaitedResultAfterTransfersAndPause)



   }

  override def beforeAll: Unit = {

    databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("h2mem")

    // https://stackoverflow.com/questions/35510070/best-practice-for-slick-2-1-3-execution-context-usage
//    Slick’s API is fully asynchronous and runs database call in a separate thread pool.
    dao = new AccountDAOSlick(databaseConfig,global,IdempotencyKeySupportConfig(timeToLiveInMillis ,1000))



    val createTablesDBIO: DBIO[Unit] = DBIO.seq(dao.Accounts.schema.create)

    databaseConfig.db.run(createTablesDBIO).futureValue

  }

  override def afterAll: Unit = {

    databaseConfig.db.close()

  }

}
