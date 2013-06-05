package com.tactix4.simpleXmlRpc

import org.scalatest.FunSuite
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import XmlRpcPreamble._

 import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import org.scalatest.concurrent.Futures
import java.util.concurrent.TimeUnit
import org.scalatest.time.{Seconds, Span, Millis}

/**
 * @author max@tactix4.com
 *         5/22/13
 */
class XmlRpcClientTest extends FunSuite with Futures{

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
      Await.result(future, Duration(config.timeout.totalNanos, TimeUnit.NANOSECONDS))

    }

  }

  val protocol = "http"
val host = "192.168.1.95"
  val port = 8069
  val db = "ww_test3"
  val userId = "admin"
  val password = "admin"

  val commonApi = "/xmlrpc/common"
  val objectApi = "/xmlrpc/object"

  test("try connect to local openerp server") {

    val config = XmlRpcConfig(protocol, host, port, commonApi)
    val result = XmlRpcClient.request(config, "login", db, userId, password)

      whenReady(result){
          case s: XmlRpcResponseNormal => println(s)
          case s: XmlRpcResponseFault => println(s)
          case x => fail("result could not be matched!: " + x)
        }


  }


  test("try to read the list of partners") {

    val uid = 1 // Admin User - bypass login
    val config = XmlRpcConfig("http", "192.168.1.95", 8069, "/xmlrpc/object")
    val result2 = XmlRpcClient.request(config, "execute", db, uid, password, "wardware.patient", "read", ("1"))
 implicit  def patienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))
    whenReady(result2){ case response: XmlRpcResponse => println(response)}


  }


  test("try to insert a new partner") {

    val uid = 1 // Admin User - bypass login
    val config = XmlRpcConfig("http", host, 8069, "/xmlrpc/object")
    val result = XmlRpcClient.request(config, "execute", db, uid, password, "res.partner", "create", XmlRpcStruct(List(("name",XmlRpcString("myname")))))

    whenReady(result){
        case s: XmlRpcResponseNormal => println(s)
        case s: XmlRpcResponseFault => println("Got a fault")
        case x => println(x)
      }
    }

}
