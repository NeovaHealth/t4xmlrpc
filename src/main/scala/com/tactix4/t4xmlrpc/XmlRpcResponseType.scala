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
import scalaz.ValidationNel
import com.typesafe.scalalogging.slf4j.Logging
import scalaz.xml.cursor.Cursor
import java.io.Serializable
import scala.collection.immutable

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
 * for this reason we allow a string too, hence why faultCode has type Either[XmlRpcString.XmlRpcInt]
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

trait XmlRpcResponses extends Logging {




  type XmlRpcResponse = XmlRpcResponseFault \/ XmlRpcResponseNormal

  type FaultCode = XmlRpcDataType

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

  sealed abstract class XmlRpcResponseParent extends XmlRpcResponses{
    val errors: Option[NonEmptyList[ErrorMessage]]
  }
  case class XmlRpcResponseFault(faultCode: Option[FaultCode], faultString: Option[String], override val errors:Option[NonEmptyList[ErrorMessage]] = None) extends XmlRpcResponseParent {
    override def toString: String =
      s"FaultCode: ${faultCode.map(_.toString) | "[ ]"}\nFaultString: ${faultString.map(_.toString) | "[ ]"}"
  }

  case class XmlRpcResponseNormal(params: List[XmlRpcDataType], override val errors:Option[NonEmptyList[ErrorMessage]] = None) extends XmlRpcResponseParent {
    override def toString: String = params.map(_.toString).mkString
  }

  def appendOptions[G:Semigroup](o1:Option[G], o2: Option[G]): Option[G] = (o1, o2) match {
    case (Some(l1), Some(l2)) => Some(implicitly[Semigroup[G]].append(l1,l2))
    case (Some(l), _:None.type) => Some(l)
    case (_:None.type, Some(l)) => Some(l)
    case _ => None
  }

  object XmlRpcResponseFault {

    def apply(c: Option[Content]): XmlRpcResponseFault = {
      val fc = getFaultCode(c).disjunction
      val fs = getFaultString(c).disjunction

      new XmlRpcResponseFault(fc.toOption,fs.toOption,appendOptions(~fs toOption, ~fc toOption))
    }


    def getValue(oc: Option[Content], s: String): ValidationNel[ErrorMessage, Element] = {
      val value = for {
        c <- oc
        fc <- c.toCursor.findRec(_.current.elem exists (_.strContent.mkString == s))
        p <- fc.parent
        v <- p findChildElementName ("value" == _)
        e <- v.elem
      } yield e.successNel[String]

      value | s"Unable to find value element for $s".failNel[Element]

    }

    //return the first child as an element or the current element
    def getSubElementIfExists(e: Element): Element = {
      e.toCursor.firstChild.flatMap(_.elem) | e
    }

    /**
     *
     * grab the value from the faultCode <value> element and parse as an int if labeled as such
     * otherwise parse as a string
     * @param c
     * @return
     */
    def getFaultCode(c: Option[Content]): ValidationNel[ErrorMessage, FaultCode] = {
      getValue(c, "faultCode").flatMap(elem => elementToXmlDataType(elem))
    }

    /**
     * grab the value from the faultCode <value> element and parse as an int if labeled as such
     * otherwise parse as a string
     */
    def getFaultString(c: Option[Content]): ValidationNel[ErrorMessage, String] = {
      getValue(c, "faultString").map(elem => getSubElementIfExists(elem).strContent.mkString)
    }
  }


  object XmlRpcResponseNormal {

    def apply(c: Option[Content]): XmlRpcResponseNormal = {
      val params: \/[ErrorMessage, List[Element]] =  getParams(c).disjunction
      val xmlparams: List[ValidationNel[ErrorMessage, XmlRpcDataType]] = params.getOrElse(Nil).map(elementToXmlDataType)

     val s: Option[NonEmptyList[ErrorMessage]] =  {
       val l = xmlparams.map(_.swap.toOption).flatten
       if(l.isEmpty) None
       else l.sequenceU.map(_.mkString).some
     }

//        p <- getParams(c)
//        e <- p.flatMap(elementToXmlDataType).success
//      } yield e
      new XmlRpcResponseNormal(xmlparams.map(_.toOption).flatten, s)
    }
  }

  def getParams(co: Option[Content]): Validation[ErrorMessage, List[Element]] = {
    val ps = for {
      c <- co
      params <- (+c) findChildElementName ("params" == _)
      parame <- params.current.elem
    } yield parame.children.map(_.children.headOption).flatten
    ps.map(_.success[ErrorMessage]) | s"Could not parse params:${co.map(_ sxprints pretty)} ".fail[List[Element]]
  }

  def memberToTuple(e: Element): ValidationNel[ErrorMessage, (String, XmlRpcDataType)] = {
    val tuple = for {
      n <- e.toCursor.findChildElementName("name" == _)
      v <- e.toCursor.findChildElementName("value" == _)
      ne <- n.elem
      ve <- v.elem
    } yield ne.strContent.mkString -> ve

    tuple.map(s => elementToXmlDataType(s._2).map(d => (s._1, d))) | s"Member: ${e.content.map(_ sxprints pretty).mkString(" ")} \n could not be parsed".failNel[(String, XmlRpcDataType)]

  }

  def parseBoolean(b:String) : ValidationNel[ErrorMessage, Boolean] = {
    val trimmed = b.trim.toLowerCase
    val trues = List("true","1")
    val falses = List("false","0")
    if(trues.exists(_ == trimmed)) true.success
    else if(falses.exists(_ == trimmed)) false.success
    else s"Could not parse Boolean $b".failNel
  }

  def elementToXmlDataType(e: Element): ValidationNel[ErrorMessage, XmlRpcDataType] = {

    val v = for {
      c:Cursor <- e.toCursor.findChildOr(_.current.elem exists(_ => true), e.toCursor).some
      elem <- c.current.elem
    } yield (elem.sname.toLowerCase, elem)

    v.map(t => t._1 match {
      case "value" | "string" => XmlRpcString(t._2.strContent.mkString).successNel[ErrorMessage]
      case "base64" => XmlRpcBase64(t._2.strContent.mkString.getBytes).successNel[ErrorMessage]
      case "int" | "i4" => t._2.strContent.mkString.parseInt.map(i => XmlRpcInt(i)).leftMap("Unable to parse Int " + _.getMessage).toValidationNel
      case "boolean" => parseBoolean(t._2.strContent.mkString).map(b => XmlRpcBoolean(b))
      case "double" => t._2.strContent.mkString.parseDouble.map(d => XmlRpcDouble(d)).leftMap("Unable to parse Double " + _.getMessage).toValidationNel
      case "date" => getDateFromISO8601String(t._2.strContent.mkString).map(d => XmlRpcDate(d)).toValidationNel
      case "array" => {
        val s = t._2.children.headOption.map(_.children.map(elementToXmlDataType)) getOrElse List("Empty array".failNel[XmlRpcDataType])
        s.sequenceU.map(XmlRpcArray)
      }
      case "struct" => t._2.children.map(memberToTuple).sequenceU.map(s => XmlRpcStruct(s.toMap))
      case f => s"Unsupported element type: $f".failNel[XmlRpcDataType]
    }) getOrElse s"unable to parse element: ${e.content.map(_ sxprints pretty).mkString(" ")}".failNel[XmlRpcDataType]
  }


}
