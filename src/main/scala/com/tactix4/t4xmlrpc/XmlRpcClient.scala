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

package com.tactix4.t4xmlrpc

import scala.concurrent.{Future, ExecutionContext, Promise}
import com.typesafe.scalalogging.slf4j.Logging
import java.util.concurrent.Executors
import scala.util.{Try, Success, Failure}
import com.tactix4.t4xmlrpc.Exceptions.XmlRpcClientException
import dispatch._
import org.xml.sax.SAXParseException

/**
 * Main object of the library
 *
 * Created by max@tactix4.com
 * 5/21/13
 */
object XmlRpcClient extends Logging {


  implicit val ec : ExecutionContext = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  def request(config: XmlRpcConfig, methodName: String, params: XmlRpcDataValue*): Future[XmlRpcResponse] = request(config, methodName, params.toList)

  /**
   * Send an XML-RPC request
   * @param config the configuration to use
   * @param methodName the remote method to class
   * @param params the list of parameters to supply to the method
   * @return a Future[XmlRpcResponse]
   */
  def request(config: XmlRpcConfig, methodName: String, params: List[XmlRpcDataValue]): Future[XmlRpcResponse] = {

    val request = new XmlRpcRequest(methodName, params)
    val builder = url(config.getUrl) <:< Map("Content-Type" -> "text/xml") << config.headers setBody(request.toString)

    val result = Promise[XmlRpcResponse]()

    logger.info("sending message headers: " + config.headers)
    logger.info("sending message body: " + request.toString)

    Http(builder OK as.String).onComplete {

      x => x match {
        case Failure(e) => {
          logger.error("Failed to send message: " + builder.toString)
          result.failure(new XmlRpcClientException("Error connecting to XMLRPC Server: " + e.getMessage, e))
        }
        case Success(r) => {
          try{
            val xmlResult = scala.xml.XML.loadString(r)
            if ((xmlResult \ "fault").isEmpty) {
              result.complete(Try(XmlRpcResponseNormal(xmlResult)))
            } else {
              result.complete(Try(XmlRpcResponseFault(xmlResult)))
            }
          }
          catch {
            case e:Throwable => result.failure(new XmlRpcClientException("Error reading server response: " + e.getMessage, e))
          }
      }
    }
    }
    result.future
  }
}
