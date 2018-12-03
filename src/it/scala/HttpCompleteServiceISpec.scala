import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpMethods, HttpResponse, StatusCodes, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.mclaudt.moneytransfer.account.AccountJsonProtocol._
import com.mclaudt.moneytransfer.{DefaultJsonFormats, HttpCompleteService}
import com.mclaudt.moneytransfer.account._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.implicitConversions

class HttpCompleteServiceISpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with DefaultJsonFormats with AccountRestClient {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit var uriPrefix:String = _

  implicit def accountToUpdateAccountDTO(a:Account): UpdateAccountDTO = UpdateAccountDTO(a.id.get, UpdateAccountPatchDTO(Some(a.name),Some(a.money)))

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(10, Seconds)),
    interval = scaled(Span(20, Millis))
  )

  var s: HttpCompleteService = _

  val account = Account(Some(10L),"Rich",100500)
  val nonExistingAccount = Account(Some(6060606L),"Rich",100500)

  "The service" should "echo requested account with Some id" in {

    val responseFuture: Future[HttpResponse] =  postWithAccountBody("/echo",account)

    whenReady(responseFuture){res =>
      val returnedAccount = Unmarshal(res).to[Account]
      returnedAccount.futureValue shouldBe account
      }
    }

    it should "return 404 for non-existing uri" in {

      val responseFuture =  postWithAccountBody("/ccreate",account)

      whenReady(responseFuture){res =>
        res.status shouldBe StatusCodes.MethodNotAllowed
      }
    }

  it should "create requested account" in {

    val responseFuture: Future[HttpResponse] =  postWithAccountBody("",account)

    whenReady(responseFuture){res =>

      val returnedAccount = Unmarshal(res).to[Account]
      val created = returnedAccount.futureValue
      account.copy(id=created.id) shouldBe created
    }
  }

  it should "update requested account" in {

    whenReady(for {
      resCreate       <- postWithAccountBody("",account)
      createdAccount  <- Unmarshal(resCreate).to[Account]
      resUpdate       <- updateAccount(createdAccount.copy(name=createdAccount.name+"_updated"))
      updatedAccount  <- Unmarshal(resUpdate).to[Seq[Account]]
    } yield (createdAccount,updatedAccount)){

      case (createdAccount,updatedAccount)=>
        updatedAccount shouldBe Seq(createdAccount.copy(name=createdAccount.name+"_updated")) }
  }

  it should "not update not existing account and return empty array" in {

    whenReady(for {
      resUpdate       <- updateAccount(nonExistingAccount)
      updatedAccount  <- {Unmarshal(resUpdate).to[Seq[Account]]}
    } yield updatedAccount){
      updatedAccount => updatedAccount shouldBe Seq.empty }
  }


  it should "delete created account" in {

    whenReady(for {
      resCreate       <- postWithAccountBody("",account)
      createdAccount  <- Unmarshal(resCreate).to[Account]
      resDelete       <- deleteAccount(createdAccount.id.get)
    } yield resDelete){
      httpResponse =>
        httpResponse.status shouldBe StatusCodes.NoContent}
  }

  it should "getByName requested account" in {

    val name = "accountToGetByName"

    val accountToInsert  = account.copy(name = name)

    whenReady(for {
      resCreate       <- postWithAccountBody("",accountToInsert)
      createdAccount  <- Unmarshal(resCreate).to[Account]
      resGottenByName       <- get(s"?name=${accountToInsert.name}")
      gotAccounts  <- Unmarshal(resGottenByName).to[Seq[Account]]
    } yield (createdAccount,gotAccounts)){

      case (createdAccount,updatedAccount)=>
        updatedAccount.forall(a=>a.name == accountToInsert.name && a.money == accountToInsert.money) shouldBe true
         }
  }

  it should "getById created account" in {

    val name = "accountToGetById"

    val accountToInsert  = account.copy(name = name)

    whenReady(for {
      resCreate       <- postWithAccountBody("",accountToInsert)
      createdAccount  <- Unmarshal(resCreate).to[Account]
      resGotById       <- get(s"/${createdAccount.id.get.toString}")
      gotAccounts  <- Unmarshal(resGotById).to[Seq[Account]]
    } yield (createdAccount,gotAccounts)){

      case (createdAccount,gotAccounts)=>
        gotAccounts shouldBe Seq(createdAccount)
    }
  }

  it should "transfer money from one account to another" in {

    val transferAmount = 1000

    whenReady(for {
      resCreateFrom       <- postWithAccountBody("",account.copy(name="accountForTransferFrom"))
      createdFromAccount  <- Unmarshal(resCreateFrom).to[Account]
      resCreateTo       <- postWithAccountBody("",account.copy(name="accountForTransferTo"))
      createdToAccount  <- Unmarshal(resCreateTo).to[Account]
      resTransfer       <- transfer(TransferDTO(createdFromAccount.id.get,createdToAccount.id.get, transferAmount))
      finalAccountsState  <- Unmarshal(resTransfer).to[Seq[(Account,Account)]]
    } yield (createdFromAccount,createdToAccount,finalAccountsState)){

      case (createdFromAccount,createdToAccount,finalAccountsState)=>
        finalAccountsState shouldBe Seq((createdFromAccount.copy(money = createdFromAccount.money-transferAmount),createdToAccount.copy(money = createdToAccount.money+transferAmount)))
    }
  }


  it should "transfer money from one account to another only once in the presence of idempotency keys" in {

    val transferAmount = 1000

    val idempotencyKey = Some(10101L)

    whenReady(for {
      resCreateFrom       <- postWithAccountBody("",account.copy(name="accountForTransferFrom"))
      createdFromAccount  <- Unmarshal(resCreateFrom).to[Account]
      resCreateTo       <- postWithAccountBody("",account.copy(name="accountForTransferTo"))
      createdToAccount  <- Unmarshal(resCreateTo).to[Account]
      resTransfer1       <- transfer(TransferDTO(createdFromAccount.id.get,createdToAccount.id.get, transferAmount,idempotencyKey))
      finalAccountsState1  <- Unmarshal(resTransfer1).to[Seq[(Account,Account)]]
      resTransfer2       <- transfer(TransferDTO(createdFromAccount.id.get,createdToAccount.id.get, transferAmount,idempotencyKey))
      finalAccountsState2  <- Unmarshal(resTransfer2).to[Seq[(Account,Account)]]
    } yield (createdFromAccount,createdToAccount,finalAccountsState2)){

      case (createdFromAccount,createdToAccount,finalAccountsState2)=>
        finalAccountsState2 shouldBe Seq((createdFromAccount.copy(money = createdFromAccount.money-transferAmount),createdToAccount.copy(money = createdToAccount.money+transferAmount)))
    }
  }

  override def beforeAll: Unit = {

    s = new HttpCompleteService()
    uriPrefix = s"http://${s.settings.host}:${s.settings.port}/accounts"

  }

  override def afterAll: Unit = {
    s.close()
  }

}
