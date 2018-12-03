package com.mclaudt.moneytransfer.swagger

import akka.http.scaladsl.server.{Directives, Route}

class SwaggerDocManuallyGeneratedService extends Directives {
  val route: Route =
    path("api-docs" /"swagger-manual.json" ) { getFromResource("swagger-manual.json") }
}
