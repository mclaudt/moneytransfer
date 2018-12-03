import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.mclaudt.moneytransfer.DefaultJsonFormats
import com.mclaudt.moneytransfer.account.{Account, AccountService}
import org.scalatest.{Matchers, WordSpec}
import com.mclaudt.moneytransfer.account.AccountJsonProtocol._

class ServiceRouteSpec extends WordSpec with Matchers with DefaultJsonFormats with ScalatestRouteTest with AccountFixtures{

  val dao = new  AccountDAOMock(scala.concurrent.ExecutionContext.Implicits.global)

  val service = new AccountService(dao)

  val myRoute: Route = service.route

  "Account service" should {

    "echo requested Account with no id" in {

      Post("/accounts/echo",accountWithNoIdFixture) ~> myRoute ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Account] shouldEqual accountWithNoIdFixture
      }
    }

    "echo requested Account with Some id" in {

      Post("/accounts/echo",accountFixture) ~> myRoute ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Account] shouldEqual accountFixture
      }
    }

    "mock-create Account" in {

      Post("/accounts",accountWithNoIdFixture) ~> myRoute ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Account] shouldBe dao.`mockAccount`
      }
    }

    "mock-update Account" in {

      Put(s"/accounts/${updateAccountDTOFixture.id}",updateAccountDTOFixture.patch) ~> myRoute ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[Account]] shouldBe Seq(dao.`mockAccount`)
      }
    }

    "mock-delete Account" in {

      Delete(s"/accounts/${updateAccountDTOFixture.id}") ~> myRoute ~> check {
        status shouldEqual StatusCodes.NoContent
      }
    }

    "mock-getByName Account" in {

      Get(s"/accounts?name=${accountFixture.name}") ~> myRoute ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[Account]] shouldBe Seq(dao.`mockAccount`)
      }
    }

    "mock-getById Account" in {

      Get(s"/accounts/${accountFixture.id.get.toString}") ~> myRoute ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[Account]] shouldBe Seq(dao.`mockAccount`)
      }
    }

    "mock-transferFromTo Account" in {

      Post("/accounts/transfer",transferDTOFixture) ~> myRoute ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Seq[(Account,Account)]] shouldBe dao.mockTransferResult.toSeq
      }
    }
  }
}
