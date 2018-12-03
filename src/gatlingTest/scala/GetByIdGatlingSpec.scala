import com.mclaudt.moneytransfer.account.AccountJsonProtocol._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import spray.json._

class GetByIdGatlingSpec extends Simulation with CommonGatling{

  val scn: ScenarioBuilder = scenario("simple get by id scenario")
    .exec(http("simple create request to get by id in next step")
      .post("/accounts")
      .body(StringBody(accountWithNoIdFixture.toJson.prettyPrint)).check(jsonPath("$.id").find.saveAs("createdId")))
    .pause(pause)
    .exec(
      http("simple getById request")
        .get("/accounts/${createdId}")
        .check(jsonPath("$[0].name").find.is(accountWithNoIdFixture.name))
        .check(jsonPath("$[0].money").find.is(accountWithNoIdFixture.money.toString()))
        .check(jsonPath("$[0].id").find.is("${createdId}"))
        .check(status.is(200))

    )

  setUp(scn.inject(atOnceUsers(users)).protocols(httpProtocol)).assertions(
    global.successfulRequests.percent.gt(threshold)
  )

}

