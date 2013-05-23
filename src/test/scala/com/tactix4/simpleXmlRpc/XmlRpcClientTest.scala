package com.tactix4.simpleXmlRpc

import org.scalatest.FunSuite
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import XmlRpcPreamble._
/**
 * @author max@tactix4.com
 *         5/22/13
 */
class XmlRpcClientTest extends FunSuite {

  test("try connect to local openerp server") {


    val config = XmlRpcConfig(RPCProtocol.RPC_HTTP, "192.168.1.95", 8069, "/xmlrpc/object")
    val result = XmlRpcClient.request(config, "execute", "tactix4test", 1, "password",
      "res.partner", "write", 67, List(("website",XmlRpcString("09jasdf")))
    )
    result.onComplete {
      x => x match {
        case Success(s: XmlRpcResponseNormal) => println(s)
        case Success(s: XmlRpcResponseFault) => println(s)
        case Failure(f) => println(f.getMessage)
        case _          => fail("result could not be matched!")
      }
    }

    Thread.sleep(2000)

  }
}
