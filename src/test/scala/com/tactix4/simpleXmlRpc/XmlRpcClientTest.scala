package com.tactix4.simpleXmlRpc

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import XmlRpcPreamble._

 import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import org.scalatest.concurrent.Futures
import java.util.concurrent.TimeUnit
import org.scalatest.time.{Seconds, Span, Millis}
import java.util.Date

/**
 * @author max@tactix4.com
 *         5/22/13
 */
class XmlRpcClientTest extends FunSuite with Futures with BeforeAndAfterAll{

  implicit def scalaFutureToFutureConcept[T](future: Future[T]): FutureConcept[T] = new FutureConcept[T] {
    def eitherValue: Option[Either[Throwable, T]] = {
      if(!future.isCompleted)
        None
      else
        future.value match {
          case None => None
          case Some(t) => t match {
            case Success(v) => Some(Right(v))
            case Failure(e) => Some(Left(e))
          }
        }
    }
    def isExpired: Boolean = false   // Scala futures cant expire
    def isCanceled: Boolean = false  // Scala futures cannot be cancelled
    override def futureValue(implicit config: PatienceConfig): T = {
      Await.result(future, Duration(10, TimeUnit.SECONDS))

    }

  }



  val protocol = "http"
  val host = "192.168.2.110"
  val port = 8069
  val db = "ww_test3"
  val userId = "admin"
  val password = "admin"

  val commonApi = "/xmlrpc/common"
  val objectApi = "/xmlrpc/object"


  test("try connect to local openerp server") {
    val config = XmlRpcConfig(protocol, host, port, commonApi)
    val result = XmlRpcClient.request(config, "login", db+"082734", userId, password)

      whenReady(result){
          case s: XmlRpcResponseNormal => println("Gor a normal response: " + s)
          case s: XmlRpcResponseFault => println("Got a fault: " + s)
          case x => fail("result could not be matched!: " + x)
        }


  }


  test("try to read the list of patients") {

    val uid = 1 // Admin User - bypass login
    val config = XmlRpcConfig(protocol, host, port, objectApi)
    val result2 = XmlRpcClient.request(config, "execute", db, uid, password, "wardware.patient", "search", XmlRpcArray(List()))
    implicit  def patienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))
    whenReady(result2){ case response: XmlRpcResponseNormal => {
      val result3 = XmlRpcClient.request(config, "execute", db, uid, password, "wardware.patient", "read",response.params.head, List(
        "id" ->  XmlRpcString("name")
      ))
      whenReady(result3){ case response: XmlRpcResponseNormal => {
        println(response)
      }}
    }}


  }


  test("try to insert a new patient") {

    val uid = 1 // Admin User - bypass login
    val config = XmlRpcConfig("http", host, 8069, "/xmlrpc/object")
    val result = XmlRpcClient.request(config, "execute", db, uid, password, "res.partner", "create", XmlRpcStruct(List(
      ("name","myNEWname"))))

    whenReady(result){
        case s: XmlRpcResponseNormal => println(s)
        case s: XmlRpcResponseFault => {println(s.faultCode.fold(_.value,_.value)); fail(s.toString)}
        case x => fail(x.toString)
      }
    }

}
