import com.mclaudt.moneytransfer.account.AccountJsonProtocol._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import spray.json._

class CreateGatlingSpec extends Simulation with CommonGatling{

  val scn: ScenarioBuilder = scenario("simple create scenario")
    .exec(http("simple create request")
    .post("/accounts")
    .body(StringBody(accountWithNoIdFixture.toJson.prettyPrint))
    .check(status.is(200)))
    .pause(pause)

  setUp(scn.inject(atOnceUsers(users)).protocols(httpProtocol)).assertions(
    global.successfulRequests.percent.gt(threshold)
  )
}

