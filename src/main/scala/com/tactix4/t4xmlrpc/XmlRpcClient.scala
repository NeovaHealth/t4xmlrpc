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
import com.typesafe.scalalogging.slf4j.LazyLogging
import java.util.concurrent.{Executor, Executors}
import dispatch._
import scalaz.xml.Xml._
import scalaz.syntax.std.option._
import scalaz.syntax.std.either._
import scalaz.syntax.either._
import scala.util.control.Exception._
import scalaz.{\/, EitherT}

/**
 * Main object of the library
 *
 * Created by max@tactix4.com
 * 5/21/13
 */
class XmlRpcClient(e: Option[Executor] = None) extends LazyLogging with XmlRpcResponses {

  implicit val ec : ExecutionContext = e.map(ExecutionContext.fromExecutor) | ExecutionContext.fromExecutor(Executors.newCachedThreadPool())


  def outputParams(ps: List[XmlRpcDataType]): String = ps.map(d => s"<param>${XmlWriter.write(d)}</param>").mkString

  def toRequestString(name:String, params:List[XmlRpcDataType]) : String =
    s"<?xml version='1.0'?><methodCall>" +
    s"<methodName>$name</methodName>" +
    s"<params>${outputParams(params)}</params></methodCall>"

  /**
   * Send an XML-RPC request
   * @param config the configuration to use
   * @param methodName the remote method to class
   * @param params the list of parameters to supply to the method
   * @return a Future[XmlRpcResponse]
   */
  def request(config: XmlRpcConfig, methodName: String, params: XmlRpcDataType*): EitherT[Future,XmlRpcResponseFault,XmlRpcResponseNormal] = request(config, methodName, params.toList)
  def request(config: XmlRpcConfig, methodName: String, params: List[XmlRpcDataType]): EitherT[Future,XmlRpcResponseFault,XmlRpcResponseNormal] = {

    val builder = url(config.toString) <:< Map("Content-Type" -> "text/xml") << config.headers setBody toRequestString(methodName, params)

    logger.debug("sending message headers: " + config.headers)
    logger.debug("sending message body: " + builder.toString)

    EitherT {
      Http(builder OK as.String).either.flatMap(_.fold(
        Future.failed,
        (s: String) => {
          val xmlResult = s.parseXml
          logger.debug("received message" + xmlResult.map(_ sxprints pretty).mkString)
          Future.successful(createXmlRpcResponse(xmlResult))
        }))
    }
  }
}

object XmlRpcClient {
  def apply()  = new XmlRpcClient(None)
  def apply(e:Executor) = new XmlRpcClient(Some(e))
}
