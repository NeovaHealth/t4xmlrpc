package com.tactix4.simpleXmlRpc

import dispatch._
import Defaults._
import scala.concurrent.Promise
import scala.xml.{Source, XML, Unparsed, Elem}
import scala.util.{Try, Success, Failure}
import scala.xml.parsing.ConstructingParser

/**
 * Created by max@tactix4.com
 * 5/21/13
 */
object XmlRpcClient {


  def request(config: XmlRpcConfig, methodName: String, params: XmlRpcDataType*): Future[XmlRpcResponse] = request(config, methodName, params.toList)

  def request(config: XmlRpcConfig, methodName: String, params: List[XmlRpcDataType]): Future[XmlRpcResponse] = {

    val request = new XmlRpcRequest(methodName, params)
    val builder = url(config.getUrl)

    builder <:< Map("Content-Type" -> "text/xml")
    builder << config.headers
    builder.setBody(request.toString)

    val result = Promise[XmlRpcResponse]()

    Http(builder OK as.xml.Elem).onComplete {

      // match on the result - failure means dispatch failed at some point
      // success can still be fault or normal response
      // hence the ghetto fault detection
      x => println(x); x match {

        case Failure(e) => { println(e); result.failure(new XmlRpcClientException("Something went wrong: " + e.getMessage, e))}
        case Success(r) => {
          if ((r \\ "fault").length > 0) {
            result.complete(Try(XmlRpcResponseFault(r)))
          } else {
            result.complete(Try(XmlRpcResponseNormal(r)))
          }
        }
      }
    }
    result.future
  }

}
