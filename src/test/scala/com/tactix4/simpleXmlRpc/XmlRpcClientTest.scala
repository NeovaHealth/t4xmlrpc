package com.tactix4.simpleXmlRpc

import org.scalatest.FunSuite
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author max@tactix4.com
 *         5/22/13
 */
class XmlRpcClientTest extends FunSuite {

  test("try connect to local openerp server") {

    val client = new XmlRpcClient(new XmlRpcConfig("http://192.168.1.95:8069/xmlrpc/object"))
    val result = client.request("execute", XmlRpcString("tactix4test"), XmlRpcInt(1), XmlRpcString("password"),
      XmlRpcString("res.partner"), XmlRpcString("write"), XmlRpcInt(67), XmlRpcStruct(List(("website",XmlRpcString("09jasdf"))))
    )
    result.onComplete {
      x => x match {
        case Success(s: XmlRpcResponseNormal) => println(s)
        case Success(s: XmlRpcResponseFault) => println(s)
        case Failure(f) => println(f.getMessage)
      }
    }

    val result2 = client.request("execute", XmlRpcString("tactix4test"), XmlRpcInt(1), XmlRpcString("password"),
      XmlRpcString("res.partner"), XmlRpcString("write"), XmlRpcInt(67), XmlRpcStruct(List(("website",XmlRpcString("09jasdf"))))
    )
    result2.onComplete {
      x => x match {
        case Success(s: XmlRpcResponseNormal) => println(s)
        case Success(s: XmlRpcResponseFault) => println(s)
        case Failure(f) => println(f.getMessage)
      }
    }
    val result3 = client.request("execute", XmlRpcString("tactix4test"), XmlRpcInt(1), XmlRpcString("password"),
      XmlRpcString("res.partner"), XmlRpcString("write"), XmlRpcInt(67), XmlRpcStruct(List(("website",XmlRpcString("09jasdf"))))
    )
    result3.onComplete {
      x => x match {
        case Success(s: XmlRpcResponseNormal) => println(s)
        case Success(s: XmlRpcResponseFault) => println(s)
        case Failure(f) => println(f.getMessage)
      }
    }
    val result4 = client.request("execute", XmlRpcString("tactix4test"), XmlRpcInt(1), XmlRpcString("password"),
      XmlRpcString("res.partner"), XmlRpcString("write"), XmlRpcInt(25), XmlRpcStruct(List(("website",XmlRpcString("can I be found?"))))
    )
    result4.onComplete {
      x => x match {
        case Success(s: XmlRpcResponseNormal) => println(s)
        case Success(s: XmlRpcResponseFault) => println(s)
        case Failure(f) => println(f.getMessage)
      }
    }
    val result5 = client.request("execute", XmlRpcString("tactix4test"), XmlRpcInt(1), XmlRpcString("password"),
      XmlRpcString("res.partner"), XmlRpcString("write"), XmlRpcInt(67), XmlRpcStruct(List(("website",XmlRpcString("09jasdf"))))
    )
    result5.onComplete {
      x => x match {
        case Success(s: XmlRpcResponseNormal) => println(s)
        case Success(s: XmlRpcResponseFault) => println(s)
        case Failure(f) => println(f.getMessage)
      }
    }
    Thread.sleep(2000)

  }
}
