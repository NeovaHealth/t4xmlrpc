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

import scala.concurrent.{Future, ExecutionContext}
import com.typesafe.scalalogging.slf4j.Logging
import java.util.concurrent.Executors
import dispatch._
import scalaz.xml.Xml._
import scala.util.control.Exception.allCatch

/**
 * Main object of the library
 *
 * Created by max@tactix4.com
 * 5/21/13
 */
object XmlRpcClient extends Logging with XmlRpcResponses {

  implicit val ec : ExecutionContext = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  /**
   * Send an XML-RPC request
   * @param config the configuration to use
   * @param methodName the remote method to class
   * @param params the list of parameters to supply to the method
   * @return a Future[XmlRpcResponse]
   */
  def request(config: XmlRpcConfig, methodName: String, params: XmlRpcDataType*): Future[XmlRpcResponse] = request(config, methodName, params.toList)
  def request(config: XmlRpcConfig, methodName: String, params: List[XmlRpcDataType]): dispatch.Future[XmlRpcResponse] = {

    val request = new XmlRpcRequest(methodName, params)
    val builder = url(config.getUrl) <:< Map("Content-Type" -> "text/xml") << config.headers setBody request.toXmlString

    logger.debug("sending message headers: " + config.headers)
    logger.debug("sending message body: " +request.toXmlString)

    try{
      Http(builder OK as.String).map((success: String) => {
          val xmlResult = success.parseXml
          logger.debug("received message" + xmlResult.map(_ sxprints pretty).mkString)
          createXmlRpcResponse(xmlResult)
        })
    } catch {
      case e: Throwable => Future.failed(e)
    }
  }
}
