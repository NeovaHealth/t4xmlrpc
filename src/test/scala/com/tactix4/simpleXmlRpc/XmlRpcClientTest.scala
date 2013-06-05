package com.tactix4.simpleXmlRpc

import org.scalatest.FunSuite
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import XmlRpcPreamble._
import scalaz.NonEmptyList
import java.net.URL



/**
 * @author max@tactix4.com
 *         5/22/13
 */
class XmlRpcClientTest extends FunSuite {

  val protocol = "http"
  val host = "localhost"
  val port = 8069
  val db = "ww_test3"
  val userId = "admin"
  val password = "admin"

  val commonApi = "/xmlrpc/common"
  val objectApi = "/xmlrpc/object"

  test("try connect to local openerp server") {

    val config = XmlRpcConfig(protocol, host, port, commonApi)
    val result = XmlRpcClient.request(config, "login", db, userId, password)

      result.onComplete {
        x => x match {
          case Success(s: XmlRpcResponseNormal) => println(s)
          case Success(s: XmlRpcResponseFault) => println(s)
          case Failure(f) => println(f.getMessage)
          case _ => fail("result could not be matched!")
        }
      }

    Thread.sleep(2000)

  }

  test("try to read the list of partners") {

    val uid = 1 // Admin User - bypass login
    val config = XmlRpcConfig("http", "localhost", 8069, "/xmlrpc/object")
    println("wtf?")
    val result2 = XmlRpcClient.request(config, "execute", db, uid, password, "res.partner", "read", ("1"))

    result2.onComplete {
      println("wtfnow?")
      x => x match {
        case Success(s: XmlRpcResponseNormal) => println(s)
          println("tiredwtf?")
          val response = x.map(z => println(z))
        case Success(s: XmlRpcResponseFault) => println("Got a fault")
        case Failure(f) => println(f.getMessage + "- FAILED")
        case _ => println("wtfelse?")
          fail("result could not be matched!")
      }
    }
  }

  test("try to insert a new partner") {

    val uid = 1 // Admin User - bypass login
    val config = XmlRpcConfig("http", "localhost", 8069, "/xmlrpc/object")
    val result = XmlRpcClient.request(config, "execute", db, uid, password, "res.partner", "create", List("name",new XmlRpcString("myname")))

    result.onComplete {
      println("wtfnow?")
      x => x match {
        case Success(s: XmlRpcResponseNormal) => println(s)
          println("tiredwtf?")
          val response = x.map(z => println(z))
        case Success(s: XmlRpcResponseFault) => println("Got a fault")
        case Failure(f) => println(f.getMessage + "- FAILED")
        case _ => println("wtfelse?")
          fail("result could not be matched!")
      }
    }



  }

}
