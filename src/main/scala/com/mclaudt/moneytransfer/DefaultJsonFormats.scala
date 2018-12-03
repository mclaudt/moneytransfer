package com.mclaudt.moneytransfer

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, StatusCode}
import spray.json._

import scala.reflect.ClassTag

case class ErrorResponseException(responseStatus: StatusCode, response: Option[HttpEntity]) extends Exception

trait DefaultJsonFormats extends DefaultJsonProtocol with SprayJsonSupport {


  def jsonObjectFormat[A : ClassTag]: RootJsonFormat[A] = new RootJsonFormat[A] {
    val ct: ClassTag[A] = implicitly[ClassTag[A]]
    def write(obj: A): JsValue = JsObject("value" -> JsString(ct.runtimeClass.getSimpleName))
    def read(json: JsValue): A = ct.runtimeClass.newInstance().asInstanceOf[A]
  }

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)
    def read(value: JsValue): UUID = value match {
      case JsString(x) => UUID.fromString(x)
      case x           => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }

}
