package com.mclaudt.moneytransfer.account

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.mclaudt.moneytransfer.DefaultJsonFormats

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class AccountService(dao:AccountDAO)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  import com.mclaudt.moneytransfer.account.AccountJsonProtocol._

  implicit val timeout: Timeout = Timeout(2.seconds)

  val route: Route = pathPrefix("accounts") {
    echo ~
      create ~
      update ~
      deleteAccount~
      getByName ~
      getById ~
      transferFromTo
  }

  def echo: Route =
    path("echo") {
      post {
        entity(as[Account]) { account =>
          complete { dao.echo(account) }
        }
      }
    }

  def create: Route =
    pathEnd {
      post {
        entity(as[Account]) { account =>
          complete { dao.create(account) }
        }
      }
    }

  def update: Route =
    path(Segment) {
      accountId =>
        put {
          Try(accountId.toLong) match {
            case Success(id) =>
              entity(as[UpdateAccountPatchDTO]) { patch =>
                complete { dao.update(UpdateAccountDTO(id,patch)).map(o=>o.toSeq) }              }
            case Failure(exception) =>
              complete(HttpResponse(BadRequest, entity = s"Incorrect account id: $accountId. Should be parsable to Long"))
          }
      }
    }

  def deleteAccount: Route =
    path(Segment) {
      accountId =>
        delete {
          Try(accountId.toLong) match {
            case Success(id) =>
                complete { dao.delete(id).map(o=>
                  if (o == 1)
                    HttpResponse(NoContent)
                  else
                    HttpResponse(BadRequest, entity = s"No Accounts were deleted"))}
            case Failure(exception) =>
              complete(HttpResponse(BadRequest, entity = s"Incorrect account id: $accountId. Should be parsable to Long"))
          }
        }
    }

  def getByName: Route =
  parameter("name"){accountName =>
      get {
        complete {
          dao.getByName(accountName)
        }
      }
  }

  def getById: Route =
    path(  Segment) {
      accountId =>
      get {
          Try(accountId.toLong) match {
            case Success(id) =>
              complete {dao.getById(id).map(o=>o.toSeq)}
            case Failure(exception) =>
              complete(HttpResponse(BadRequest, entity = s"Incorrect account id: $accountId. Should be parsable to Long"))
          }

      }
    }

  def transferFromTo: Route =
    path("transfer") {
      post {
        entity(as[TransferDTO]) { transferDTO =>
              complete {dao.transferFromTo(transferDTO).map(o=>o.toSeq)}
        }
      }
    }
}
