import com.mclaudt.moneytransfer.account.AccountJsonProtocol._
import com.mclaudt.moneytransfer.account.Account
import org.scalatest.{FlatSpec, Matchers}
import spray.json._


class AccountJsonProtocolSpec extends FlatSpec with Matchers with AccountFixtures with DefaultJsonProtocol {

  "Account Json serde" should "serialize Account without id to Json without id " in {

    accountWithNoIdFixture.toJson.prettyPrint should equal( """{
                                         |  "name": "Rich",
                                         |  "money": 100500
                                         |}""".stripMargin)
  }

  it should "serialize Account with id to Json with id " in {

   accountFixture.toJson.prettyPrint should equal( """{
                                         |  "id": 123,
                                         |  "name": "Rich",
                                         |  "money": 100500
                                         |}""".stripMargin)
  }

  it should "serialize Seq[Account] " in {

    Seq(accountFixture).toJson.prettyPrint should equal( """[{
                                         |  "id": 123,
                                         |  "name": "Rich",
                                         |  "money": 100500
                                         |}]""".stripMargin)

    Seq.empty[Account].toJson.prettyPrint should equal( "[]")
  }

  "TransferDTO Json serde" should "serialize TransferDTO" in {

    transferDTOFixture.toJson.prettyPrint should equal( """{
                                                              |  "fromId": 123,
                                                              |  "toId": 456,
                                                              |  "money": 100500
                                                              |}""".stripMargin)
  }

  "Seq((accountFixture, accountFixture))" should "be serialized" in {
    Seq((accountFixture, accountFixture)).toJson.prettyPrint should equal(
      """[[{
        |  "id": 123,
        |  "name": "Rich",
        |  "money": 100500
        |}, {
        |  "id": 123,
        |  "name": "Rich",
        |  "money": 100500
        |}]]""".stripMargin)
  }


  "UpdateAccountPatch Json serde" should "serialize UpdateAccountDTO" in {
    updateAccountDTOFixture.toJson.prettyPrint should equal("""{
                                                              |  "id": 123,
                                                              |  "patch": {
                                                              |    "name": "Rich",
                                                              |    "money": 100500
                                                              |  }
                                                              |}""".stripMargin)
  }

}
