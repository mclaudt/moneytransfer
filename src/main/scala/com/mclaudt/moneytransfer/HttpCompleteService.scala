package com.mclaudt.moneytransfer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.mclaudt.moneytransfer.account._
import com.mclaudt.moneytransfer.swagger.{SwaggerDocManuallyGeneratedService, SwaggerEditorService, SwaggerUIService}
import org.slf4j.{Logger, LoggerFactory}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.language.postfixOps

class HttpCompleteService extends RouteConcatenation {

  val logger: Logger = LoggerFactory.getLogger(classOf[HttpCompleteService])

  implicit val actorSystem: ActorSystem = ActorSystem("akka-http-sample")

  sys.addShutdownHook {

    logger.info("Terminating from shutdown hook...")
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 30 seconds)
    logger.info("Terminated from shutdown hook. Bye!")
  }

  val settings = Settings(actorSystem)

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("h2mem")

  val dao = new AccountDAOSlick(dbConfig, executionContext, settings.idempotencyKeySupportConfig)

  val routes: Route =
    cors(CorsSettings.defaultSettings.withAllowedMethods(List(
      HttpMethods.GET,
      HttpMethods.POST,
      HttpMethods.HEAD,
      HttpMethods.OPTIONS,
      HttpMethods.PUT,
      HttpMethods.DELETE
    ))) {
        new AccountService(dao).route ~
        new SwaggerUIService().route ~
        new SwaggerEditorService().route ~
        new SwaggerDocManuallyGeneratedService().route
    }

  val tableCreation: Unit = Await.result(dbConfig.db.run(DBIO.seq(dao.Accounts.schema.create)), 2 seconds)

  val binding: Future[Http.ServerBinding] = Http()(actorSystem).bindAndHandle(routes, settings.host, settings.port)


  def close(): Unit = {
    val onceAllConnectionsTerminated: Future[Http.HttpTerminated] =
      Await.result(binding, 10.seconds).terminate(hardDeadline = 3.seconds)

    val fTerminated = onceAllConnectionsTerminated.flatMap { _ ⇒ {
      logger.info("Terminating from close method...")
      actorSystem.terminate()
    }
    }

    Await.ready(fTerminated, 5 seconds)
    logger.info("Terminated from close method. Bye!")
  }


}
