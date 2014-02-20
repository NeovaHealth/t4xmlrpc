package com.tactix4.t4xmlrpc

import org.scalatest.FunSuite
import scala.util.{Success,Failure}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created with IntelliJ IDEA.
 * User: max
 * Date: 10/02/14
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
class XmlRpcClient$Test extends FunSuite {

  test("basic test") {
    val config = XmlRpcConfig(RPCProtocol.RPC_HTTP,"10.10.170.22", 8069,"/xmlrpc/db")
    val response = XmlRpcClient.request(config,"list")
    response.onComplete({
      case Success(s) => s.fold(
        (fault: XmlRpcClient.XmlRpcResponseFault) => println("FAULT: " +fault.toString),
        (normal: XmlRpcClient.XmlRpcResponseNormal) => println("NORMAL: " + normal.params.map(_.toString)))
      case Failure(f) => println(f.getMessage)
    })

    Await.result(response, 2 minutes)
    Thread.sleep(100)
  }

}
