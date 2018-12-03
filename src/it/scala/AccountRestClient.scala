import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import com.mclaudt.moneytransfer.account.AccountJsonProtocol._
import com.mclaudt.moneytransfer.account._
import com.mclaudt.moneytransfer.DefaultJsonFormats

import scala.concurrent.{ExecutionContext, Future}

trait AccountRestClient extends DefaultJsonFormats {

  def postWithAccountBody(uri: String, account: Account)(implicit system: ActorSystem, executionContext: ExecutionContext, uriPrefix: String): Future[HttpResponse] =
    Marshal(account).to[RequestEntity] flatMap { entity => {
      val request = new HttpRequest(
        method = HttpMethods.POST,
        uri = uriPrefix + uri,
        headers = List(Accept(MediaTypes.`application/json`)),
        entity = entity.withContentType(MediaTypes.`application/json`),
        protocol = HttpProtocols.`HTTP/1.1`)
      Http()(system).singleRequest(request)
    }
    }

  def updateAccount(updateDTO: UpdateAccountDTO)(implicit system: ActorSystem, executionContext: ExecutionContext, uriPrefix: String): Future[HttpResponse] =
    Marshal(updateDTO.patch).to[RequestEntity] flatMap { entity => {

      val request = new HttpRequest(
        method = HttpMethods.PUT,
        uri = uriPrefix + "/" + updateDTO.id,
        headers = List(Accept(MediaTypes.`application/json`)),
        entity = entity.withContentType(MediaTypes.`application/json`),
        protocol = HttpProtocols.`HTTP/1.1`)
      Http()(system).singleRequest(request)
    }
    }

  def deleteAccount(id: Long)(implicit system: ActorSystem, executionContext: ExecutionContext, uriPrefix: String): Future[HttpResponse] = {
    //TODO отрефакторить в простой Get

    val request = new HttpRequest(
      method = HttpMethods.DELETE,
      uri = uriPrefix + "/" + id,
      headers = List(Accept(MediaTypes.`application/json`)),
      entity = HttpEntity.Empty,
      protocol = HttpProtocols.`HTTP/1.1`)
    Http()(system).singleRequest(request)
  }

  def get(uriAndParams: String)(implicit system: ActorSystem, executionContext: ExecutionContext, uriPrefix: String): Future[HttpResponse] = {
    val request = new HttpRequest(
      method = HttpMethods.GET,
      uri = uriPrefix + uriAndParams,
      headers = List(Accept(MediaTypes.`application/json`)),
      entity = HttpEntity.Empty,
      protocol = HttpProtocols.`HTTP/1.1`)
    Http()(system).singleRequest(request)
  }

  def transfer(t: TransferDTO)(implicit system: ActorSystem, executionContext: ExecutionContext, uriPrefix: String): Future[HttpResponse] =
    Marshal(t).to[RequestEntity] flatMap { entity => {
      val request = new HttpRequest(
        method = HttpMethods.POST,
        uri = uriPrefix + "/transfer",
        headers = List(Accept(MediaTypes.`application/json`)),
        entity = entity.withContentType(MediaTypes.`application/json`),
        protocol = HttpProtocols.`HTTP/1.1`)
      Http()(system).singleRequest(request)
    }
    }

}
