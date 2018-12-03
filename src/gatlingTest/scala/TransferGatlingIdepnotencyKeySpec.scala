import com.mclaudt.moneytransfer.account.Account
import com.mclaudt.moneytransfer.account.AccountJsonProtocol._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import spray.json._

import scala.util.Random

class TransferGatlingIdepnotencyKeySpec extends Simulation with CommonGatling {

  val transferAmount = 100

  val accountToTransferFrom: Account = accountWithNoIdFixture.copy(name = "Poor", money = 100)

  val accountToTransferTo: Account = accountWithNoIdFixture.copy(name = "Rich", money = 1000000)

  val scn: ScenarioBuilder = scenario("simple transfer scenario with idempotency key support")
    .exec(_.set("randomIdempotencyKeyForSession", Random.nextLong()))
    .exec(http("simple create account to transfer from")
      .post("/accounts")
      .body(StringBody(accountToTransferFrom.toJson.prettyPrint)).check(jsonPath("$.id").find.saveAs("createdFromId")))
    .exec(http("simple create account to transfer to")
      .post("/accounts")
      .body(StringBody(accountToTransferTo.toJson.prettyPrint)).check(jsonPath("$.id").find.saveAs("createdToId")))
    .repeat(5) {
      exec(
        http("simple transfer request which makes the poor poorer and the rich richer")
          .post("/accounts/transfer")
          //найти другой способ достать переменную из сессии
          .body(StringBody(
          """{ "fromId":${createdFromId},
            |  "toId": ${createdToId},
            |  "money": 100,
            |  "idempotencyKey":${randomIdempotencyKeyForSession}
            |}""".stripMargin))
          .check(jsonPath("$[0][0].name").find.is(accountToTransferFrom.name))
          .check(jsonPath("$[0][0].money").find.is((accountToTransferFrom.money - transferAmount).toString()))
          .check(jsonPath("$[0][1].name").find.is(accountToTransferTo.name))
          .check(jsonPath("$[0][1].money").find.is((accountToTransferTo.money + transferAmount).toString()))
          .check(status.is(200))

      )
    }

  setUp(scn.inject(atOnceUsers(10)).protocols(httpProtocol)).assertions(
    global.successfulRequests.percent.gt(threshold)
  )

}
