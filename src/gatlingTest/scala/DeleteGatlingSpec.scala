import com.mclaudt.moneytransfer.account.AccountJsonProtocol._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import spray.json._

class DeleteGatlingSpec extends Simulation with CommonGatling {

  val scn: ScenarioBuilder = scenario("simple delete scenario")
    .exec(http("simple create request to delete it in next step")
    .post("/accounts")
    .body(StringBody(accountWithNoIdFixture.toJson.prettyPrint)).check(jsonPath("$.id").find.saveAs("createdId")))
    .pause(pause)
    .exec(
      http("simple delete request")
      .delete("/accounts/${createdId}")
        .check(status.is(204))
    )

  setUp(scn.inject(atOnceUsers(users)).protocols(httpProtocol)).assertions(
    global.successfulRequests.percent.gt(threshold)
  )

}
