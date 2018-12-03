package com.mclaudt.moneytransfer.swagger

import akka.http.scaladsl.server.{Directives, Route}

class SwaggerUIService extends Directives {
  val route: Route =
    path("swagger") { getFromResource("swagger/index.html") } ~
      getFromResourceDirectory("swagger")
}
