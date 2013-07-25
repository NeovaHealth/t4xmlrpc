package com.tactix4.simpleXmlRpc

import dispatch._
import Defaults._
import scala.concurrent.Promise
import scala.util.{Try, Success, Failure}
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Created by max@tactix4.com
 * 5/21/13
 */
object XmlRpcClient extends Logging {


  def request(config: XmlRpcConfig, methodName: String, params: XmlRpcDataType*): Future[XmlRpcResponse] = request(config, methodName, params.toList)

  def request(config: XmlRpcConfig, methodName: String, params: List[XmlRpcDataType]): Future[XmlRpcResponse] = {

    val request = new XmlRpcRequest(methodName, params)
    val builder = url(config.getUrl)

    builder <:< Map("Content-Type" -> "text/xml")
    builder << config.headers
    builder.setBody(request.toString)

    val result = Promise[XmlRpcResponse]()

    logger.info("sending message headers: " + config.headers)
    logger.info("sending message body: " + request.toString)

    Http(builder OK as.xml.Elem).onComplete {

      // match on the result - failure means dispatch failed at some point
      // success can still be fault or normal response
      // hence the ghetto fault detection
      x => x match {
        case Failure(e) => {
          logger.error("Failed to send message: " + builder.toString)
          result.failure(new XmlRpcClientException("Error connecting to XMLRPC Server: " + e.getMessage, e))
        }
        case Success(r) => {
          if ((r \ "fault").isEmpty) {
            result.complete(Try(XmlRpcResponseNormal(r)))
          } else {
            result.complete(Try(XmlRpcResponseFault(r)))
          }
      }
    }
    }
    result.future
  }

}
