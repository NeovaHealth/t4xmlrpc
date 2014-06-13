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

import scala.language.postfixOps
import scalaz._
import Scalaz._
import scalaz.xml.{Element, Content}
import scalaz.xml.Xml._
import com.typesafe.scalalogging.slf4j.LazyLogging


/**
 * @author max@tactix4.com
 *         5/21/13
 */


/**
 * Represents a fault response from the server
 * it contains the error code and error string returned by the server
 * the error code should be an Int as per http://t4xmlrpc.scripting.com/spec
 * however pythons XML-RPC library decided to use a string instead :/
 * http://docs.python.org/2/library/xmlrpclib.html#fault-objects
 * Hence why faultCode has type XmlRpcDataType
 * <methodResponse>
 * <fault>
 * <value>
 * <struct>
 * <member>
 * <name>faultCode</name>
 * <value><int>4</int></value>
 * </member>
 * <member>
 * <name>faultString</name>
 * <value><string>Too many parameters.</string></value>
 * </member>
 * </struct>
 * </value>
 * </fault>
 * </ methodResponse>
 */

trait XmlRpcResponses extends LazyLogging {

      val trues = List("true", "1")
      val falses = List("false", "0")
  /**
   * return the content that contains the methodResponse
   * @param l the list of contents
   * @return the content containing the methodResponse
   */
  def getMethodResponse(l: List[Content]): Option[Content] = {
    l.find(_.elem exists (_.sname == "methodResponse"))
  }

  /**
   * is the first child element called fault?
   * @return a boolean
   */
  def isFault(o: Option[Content]): Boolean = o.exists(+_ findChildElementName ("fault" == _) isDefined)

  def createXmlRpcResponse(c: List[Content]): XmlRpcResponse = {
    val methodResponse = getMethodResponse(c)
    if (isFault(methodResponse))
      XmlRpcResponseFault(methodResponse).left
    else
      XmlRpcResponseNormal(methodResponse).right
  }

   //return the first child as an element or the current element
    def getSubElementIfExists(e: Element): Element = {
      e.toCursor.firstChild.flatMap(_.elem) | e
    }



  object XmlRpcResponseFault {

    def apply(c: Option[Content]): XmlRpcResponseFault = {
      new XmlRpcResponseFault(getFaultCode(c), getFaultString(c))
    }

    def getValue(oc: Option[Content], s: String): ResultType[Element] = {
      val value = for {
        c <- oc
        fc <- c.toCursor.findRec(_.current.elem exists (_.strContent.mkString == s))
        p <- fc.parent
        v <- p findChildElementName ("value" == _)
        e <- v.elem
      } yield e

      value \/> s"Unable to find value element for $s"
    }

    /**
     *
     * grab the value from the faultCode <value> element and parse as an int if labeled as such
     * otherwise parse as a string
     * @param c
     * @return
     */
    def getFaultCode(c: Option[Content]): ResultType[FaultCode] = {
      getValue(c, "faultCode").flatMap(element2XmlDataType)
    }

    /**
     * grab the value from the faultCode <value> element and parse as an int if labeled as such
     * otherwise parse as a string
     */
    def getFaultString(c: Option[Content]): ResultType[String] = {
      getValue(c, "faultString").map(e => getSubElementIfExists(e).strContent.mkString)
    }
  }


  object XmlRpcResponseNormal {

    def apply(c: Option[Content]): XmlRpcResponseNormal = {
      val params = getParams(c).flatMap(_.map(element2XmlDataType).sequence[ResultType,XmlRpcDataType])
      new XmlRpcResponseNormal(params)
    }
  }


  def getParams(co: Option[Content]): ResultType[List[Element]] = {
    val ps = for {
      content <- co
      params <- content.toCursor.findChildElementName("params" == _)
      parame <- params.current.elem
    } yield parame.children.map(_.children.headOption).flatten.toList

    ps \/> s"Could not parse params:${co.map(_ sxprints pretty)}"

  }


  def element2XmlDataType(e: Element): ResultType[XmlRpcDataType] = {

    def parseBoolean(b: String): ResultType[Boolean] = {
      val trimmed = b.trim.toLowerCase
      if (trues.exists(_ == trimmed)) \/-(true)
      else if (falses.exists(_ == trimmed)) \/-(false)
      else -\/(s"Could not parse Boolean $b")
    }

    def memberToTuple(e: Element): ResultType[(String, XmlRpcDataType)] = {
      val tuple = for {
        n <- e.toCursor.findChildElementName("name" == _)
        v <- e.toCursor.findChildElementName("value" == _)
        ne <- n.elem
        ve <- v.elem
      } yield ne.strContent.mkString -> ve

      val t = tuple.map(s => element2XmlDataType(s._2).map(x => s._1 -> x))

      t | -\/(s"Member: ${e.content.map(_ sxprints pretty).mkString(" ")} \n could not be parsed")
    }

    val value: Element = getSubElementIfExists(e)

    val content = value.strContent.mkString
    value.sname match {
      case "value" | "string" =>  \/-(XmlRpcString(content))
      case "int" | "i4" => content.parseInt.disjunction.bimap(_.getMessage,XmlRpcInt)
      case "boolean" => parseBoolean(content).map(XmlRpcBoolean)
      case "double" => content.parseDouble.disjunction.bimap(_.getMessage,XmlRpcDouble)
      case "date" => getDateFromISO8601String(content).map(XmlRpcDate)
      case "array" => {
        val a = value.children.headOption.map(_.children.map(element2XmlDataType)) | Nil
        a.sequence[ResultType,XmlRpcDataType].map(XmlRpcArray)
      }
      case "struct" =>  value.children.map(memberToTuple).sequence[ResultType,(String,XmlRpcDataType)].map(l => XmlRpcStruct(l.toMap))
      case "base64" => \/-(XmlRpcBase64(content.getBytes("UTF-8")))
      case unknown => -\/(s"Unsupported element type: $unknown")
    }

  }

}
