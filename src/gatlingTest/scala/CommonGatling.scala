import com.mclaudt.moneytransfer.account.Account
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.util.Random

trait CommonGatling {

  //pause between tests
  val pause = 5

  //Number of users
  val users = 50

  //Threshold for reporting success
  val threshold = 99

  val host = "0.0.0.0"
  val port = 8090

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(s"http://$host:$port")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val accountFixture = Account(Some(123L),"Rich",100500)

  val accountWithNoIdFixture: Account = accountFixture.copy(id=None)

  def genRandomString(length:Int): String = Random.alphanumeric.take(length).mkString

}
