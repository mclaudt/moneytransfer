import com.mclaudt.moneytransfer.account.AccountJsonProtocol._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import spray.json._

class EchoGatlingSpec extends Simulation with CommonGatling {

  val scn: ScenarioBuilder = scenario("simple echo scenario")
    .exec(http("simple echo request")
    .post("/accounts/echo")
    .body(StringBody(accountFixture.toJson.prettyPrint)))
    .pause(pause)

  setUp(scn.inject(atOnceUsers(users)).protocols(httpProtocol)).assertions(
    global.successfulRequests.percent.gt(threshold)
  )

}
