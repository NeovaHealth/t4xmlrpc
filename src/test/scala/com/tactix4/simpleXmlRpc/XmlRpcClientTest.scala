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

  test("try connect to local openerp server") {


    val config = XmlRpcConfig("http", "192.168.1.95", 8069, "/xmlrpc/common")
      val result = XmlRpcClient.request(config, "login", "tactix4test", "admin", "password")


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
}
