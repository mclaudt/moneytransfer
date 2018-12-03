package com.mclaudt.moneytransfer.swagger

import akka.http.scaladsl.server.{Directives, Route}

class SwaggerEditorService extends Directives {
  val route: Route =
    path("swagger-editor") { getFromResource("swagger-editor/index.html") } ~
      getFromResourceDirectory("swagger-editor")
}
