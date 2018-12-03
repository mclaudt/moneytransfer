import com.mclaudt.moneytransfer.account.Account
import com.mclaudt.moneytransfer.account.AccountJsonProtocol._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import spray.json._

class UpdateGatlingSpec extends Simulation with CommonGatling {

  val updatedAccountWithNoIdFixture: Account = accountWithNoIdFixture.copy(name="Very Rich", money = 100500000)

  val scn: ScenarioBuilder = scenario("simple update scenario")
    .exec(http("simple create request to update in next step")
    .post("/accounts")
    .body(StringBody(accountWithNoIdFixture.toJson.prettyPrint)).check(jsonPath("$.id").find.saveAs("createdId")))
    .pause(pause)
    .exec(
      http("simple update request")
      .put("/accounts/${createdId}")
        //найти другой способ достать переменную из сессии
      .body(StringBody("""{
                       |  "name": "Very Rich",
                       |  "money": 100500000
                       |}""".stripMargin))
        .check(jsonPath("$[0].name").find.is(updatedAccountWithNoIdFixture.name))
        .check(jsonPath("$[0].money").find.is(updatedAccountWithNoIdFixture.money.toString()))
        .check(status.is(200))
    )

  setUp(scn.inject(atOnceUsers(users)).protocols(httpProtocol)).assertions(
    global.successfulRequests.percent.gt(threshold)
  )

}
