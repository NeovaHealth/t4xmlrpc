package com.tactix4.t4xmlrpc

import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by max on 19/06/14.
 */
class XmlRpcClientTest extends FunSuite {

  test("test exception catching"){
    try {
      val config = XmlRpcConfig("http","doesntexithost", 8069, "/xmlrpc/db")
      val client = XmlRpcClient()
      val x = Await.result(client.request(config, "doesnt matter"), 2 seconds)
    }catch{
      case e:RuntimeException => println("I'm here")
      case e:Exception => println("now I'm here")
      case e:Throwable => println("and now i'm here")
    }
  }

}
