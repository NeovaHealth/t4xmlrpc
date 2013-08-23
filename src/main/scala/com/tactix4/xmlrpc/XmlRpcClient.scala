/*
 * Copyright (C) 2013 Tactix4
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tactix4.xmlrpc

import scala.concurrent.{Future, ExecutionContext, Promise}
import scala.util.{Try, Success, Failure}
import com.typesafe.scalalogging.slf4j.Logging
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import java.net.URL
import java.util.concurrent.Executors
import com.tactix4.xmlrpc.Exceptions.XmlRpcClientException

/**
 * Main object of the library
 *
 * Created by max@tactix4.com
 * 5/21/13
 */
object XmlRpcClient extends Logging {

  implicit val ec : ExecutionContext = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  implicit val httpClient = new ApacheHttpClient

  def request(config: XmlRpcConfig, methodName: String, params: XmlRpcData*): Future[XmlRpcResponse] = request(config, methodName, params.toList)

  /**
   * Send an XML-RPC request
   * @param config the configuration to use
   * @param methodName the remote method to class
   * @param params the list of parameters to supply to the method
   * @return a Future[XmlRpcResponse]
   */
  def request(config: XmlRpcConfig, methodName: String, params: List[XmlRpcData]): Future[XmlRpcResponse] = {
    val url = new URL(config.getUrl)
    val requestBody = new XmlRpcRequest(methodName, params)
    val httpRequest = POST(url).addBody(requestBody.toString).addHeaders(config.headers.toList)

    logger.debug("sending message headers: " + httpRequest.headers.toString)
    logger.debug("sending message body: " + requestBody.toString)

    val result = Promise[XmlRpcResponse]()

    httpRequest.executeAsyncUnsafe.toScalaFuture.onComplete {
      _ match {
        case Failure(e) => {
          logger.error("Failed to send message: " + httpRequest.body.toString + "\n" + e.getMessage)
          result.failure(new XmlRpcClientException("Error connecting to XML-RPC Server: " + e.getMessage, e))
        }
        case Success(r) => {
          val xmlAnswer = scala.xml.XML.loadString(r.bodyString)
          logger.debug("Got response: " + xmlAnswer)
          if ((xmlAnswer \ "fault").isEmpty) {
            result.complete(Try(XmlRpcResponseNormal(xmlAnswer)))
          } else {
            result.complete(Try(XmlRpcResponseFault(xmlAnswer)))
          }
        }
      }
    }
    result.future
  }
}
