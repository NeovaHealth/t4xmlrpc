package com.tactix4.xmlrpc.openerp

import com.tactix4.simpleXmlRpc._
import com.tactix4.simpleXmlRpc.XmlRpcPreamble._
import com.tactix4.simpleXmlRpc.XmlRpcResponseNormal

import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

/**
 * Created with IntelliJ IDEA.
 * User: les
 * Date: 04/06/13
 * Time: 21:29
 * To change this template use File | Settings | File Templates.
 */

class AdtToOpenERP {

  val config = XmlRpcConfig("http", "localhost", 8069, "/xmlrpc/common")

  def login() : Unit = {

    val result = XmlRpcClient.request(config, "login", "ww_test3", "admin", "admin")

      result.onComplete {
        x => x match {
          case Success(s: XmlRpcResponseNormal) => println(s)
          case Success(s: XmlRpcResponseFault) => println(s)
          case Failure(f) => println(f.getMessage)
          case _ => println("result could not be matched!")
        }

    }


  }

}
