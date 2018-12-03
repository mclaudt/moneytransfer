import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class GetByNameGatlingSpec extends Simulation with CommonGatling {

  val scn: ScenarioBuilder = scenario("simple get by name scenario")
    .exec(_.set("randomNameForSession", "gatling_getByName_" + genRandomString(32)))
    .repeat(3) {
      exec {
        http("simple create request to get by name in next step")
          .post("/accounts")
          .body(StringBody(
            """{
              |  "name": "${randomNameForSession}",
              |  "money": 100500000
              |}""".stripMargin))
          .check(status.is(200))
      }.pause(1)
    }
    .exec(
      http("simple getByName request")
        .get("/accounts?name=${randomNameForSession}")
        .check(status.is(200))
        .check(jsonPath("$[0].name").find.is("${randomNameForSession}"))
        .check(jsonPath("$[1].name").find.is("${randomNameForSession}"))
        .check(jsonPath("$[2].name").find.is("${randomNameForSession}"))
    )

  setUp(scn.inject(atOnceUsers(users)).protocols(httpProtocol)).assertions(
    global.successfulRequests.percent.gt(threshold)
  )

}

