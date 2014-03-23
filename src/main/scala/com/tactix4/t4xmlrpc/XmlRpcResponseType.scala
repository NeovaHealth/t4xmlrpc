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
import com.typesafe.scalalogging.slf4j.Logging
import java.util.Date


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

trait XmlRpcResponses extends Logging {

  /**
   * Result type that accumulates errors and results as it flatMaps
   *
   * @param errors the errors
   * @param result the result
   * @tparam E the error type
   * @tparam A the result type
   */
  case class ParseResult[E, A](errors: List[E] = Nil, result: Option[A] = None) {

    def map[B](f: A => B): ParseResult[E, B] = {
      ParseResult(errors, result.map(f))
    }

    def flatMap[B](f: A => ParseResult[E, B]): ParseResult[E, B] = {
      result.map(f).map(r => ParseResult(r.errors ++ errors, r.result)) | ParseResult(errors, None)
    }

    def errorMap[F](f: E => F): ParseResult[F, A] = {
      ParseResult(errors.map(f), result)
    }

    def bimap[B, F](g: E => F, f: A => B): ParseResult[F, B] = {
      ParseResult(errors.map(g), result.map(f))
    }

    def ap[EE >: E, B](x: => ParseResult[EE, A => B])(implicit E: Semigroup[EE]): ParseResult[EE, B] = (this, x) match {
      case (ParseResult(e, a), ParseResult(b, f)) => ParseResult(e ++ b, f.flatMap(z => a.map(y => z(y))))
    }

  }

  object ParseResult {

    def ok[E,A](a:A) : ParseResult[E,A] = ParseResult(result = Some(a))
    def fail[E,A](e:E) : ParseResult[E,A] = ParseResult(errors = List(e))


    implicit def ParseResultBiTraverse: Bitraverse[ParseResult] = new Bitraverse[ParseResult] {
      override def bimap[A, B, C, D](fab: ParseResult[A, B])(f: A => C, g: B => D) = fab.bimap(f, g)

      def bitraverseImpl[G[+ _] : Applicative, A, B, C, D](fab: ParseResult[A, B])(f: A => G[C], g: B => G[D]) = fab.bitraverse(f, g)
    }

    implicit def ParseResultApplicative[L: Semigroup]: Applicative[({type l[a] = ParseResult[L, a]})#l] = new Applicative[({type l[a] = ParseResult[L, a]})#l] {
      def point[A](a: => A) = ParseResult(Nil, Some(a))
      def ap[A, B](fa: => ParseResult[L, A])(f: => ParseResult[L, A => B]) = fa ap f
    }

    implicit def ParseResultMonad[E: Semigroup]: Monad[({type l[a] = ParseResult[E, a]})#l] = new Monad[({type l[a] = ParseResult[E, a]})#l] {
      override def point[A](a: => A) = ParseResult(Nil, Some(a))
      def bind[A, B](pr: ParseResult[E, A])(f: A => ParseResult[E, B]) = pr flatMap f
    }
  }

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

   //return the first child as an element or the current element
    def getSubElementIfExists(e: Element): Element = {
      e.toCursor.firstChild.flatMap(_.elem) | e
    }

  sealed abstract class XmlRpcResponseType extends XmlRpcResponses {
    val errors: Seq[ErrorMessage]
  }

  final case class XmlRpcResponseFault(faultCode: Option[FaultCode], faultString: Option[String], override val errors: List[ErrorMessage] = Nil) extends XmlRpcResponseType {
    override def toString: String =
      s"FaultCode: ${faultCode.map(_.toString) | "[ ]"}\nFaultString: ${faultString.map(_.toString) | "[ ]"}"
  }

  final case class XmlRpcResponseNormal(params: Seq[XmlRpcDataType], override val errors: Seq[ErrorMessage] = Nil) extends XmlRpcResponseType {
    override def toString: String = params.map(_.toString).mkString
  }


  object XmlRpcResponseFault {

    def apply(c: Option[Content]): XmlRpcResponseFault = {
      val fc = getFaultCode(c)
      val fs = getFaultString(c)
      new XmlRpcResponseFault(fc.result, fs.result, fc.errors ++ fs.errors)
    }


    def getValue(oc: Option[Content], s: String): ParseResult[ErrorMessage, Element] = {
      val value = for {
        c <- oc
        fc <- c.toCursor.findRec(_.current.elem exists (_.strContent.mkString == s))
        p <- fc.parent
        v <- p findChildElementName ("value" == _)
        e <- v.elem
      } yield e

      value match {
        case Some(_: Element) => ParseResult(Nil, value)
        case None => ParseResult(List(s"Unable to find value element for $s"), None)
      }
    }



    /**
     *
     * grab the value from the faultCode <value> element and parse as an int if labeled as such
     * otherwise parse as a string
     * @param c
     * @return
     */
    def getFaultCode(c: Option[Content]): ParseResult[ErrorMessage, FaultCode] = {
      getValue(c, "faultCode").flatMap(element2XmlDataType)
    }

    /**
     * grab the value from the faultCode <value> element and parse as an int if labeled as such
     * otherwise parse as a string
     */
    def getFaultString(c: Option[Content]): ParseResult[ErrorMessage, String] = {
      getValue(c, "faultString").map(e => getSubElementIfExists(e).strContent.mkString)
    }
  }


  object XmlRpcResponseNormal {

    def apply(c: Option[Content]): XmlRpcResponseNormal = {
      val params = getParams(c).flatMap(_.map(element2XmlDataType).sequenceU)
      new XmlRpcResponseNormal(~(params.result), params.errors)
    }
  }


  def getParams(co: Option[Content]): ParseResult[ErrorMessage, List[Element]] = {
    val ps = for {
      content <- co
      params <- content.toCursor.findChildElementName("params" == _)
      parame <- params.current.elem
    } yield parame.children.map(_.children.headOption).flatten.toList

    ParseResult(ps.fold(List(s"Could not parse params:${co.map(_ sxprints pretty)}"))(x => Nil), ps)
  }


  def element2XmlDataType(e: Element): ParseResult[ErrorMessage, XmlRpcDataType] = {

    def parseBoolean(b: String): ParseResult[ErrorMessage, Boolean] = {
      val trimmed = b.trim.toLowerCase
      val trues = List("true", "1")
      val falses = List("false", "0")
      if (trues.exists(_ == trimmed)) ParseResult(result = true.some)
      else if (falses.exists(_ == trimmed)) ParseResult(result = false.some)
      else ParseResult(errors = List(s"Could not parse Boolean $b"))
    }

    def memberToTuple(e: Element): ParseResult[ErrorMessage, (String, XmlRpcDataType)] = {
      val tuple = for {
        n <- e.toCursor.findChildElementName("name" == _)
        v <- e.toCursor.findChildElementName("value" == _)
        ne <- n.elem
        ve <- v.elem
      } yield ne.strContent.mkString -> ve

      val t = tuple.map(s => element2XmlDataType(s._2).map(x => s._1 -> x))

      t | ParseResult(errors = List(s"Member: ${e.content.map(_ sxprints pretty).mkString(" ")} \n could not be parsed"))
    }

    val value: Element = getSubElementIfExists(e)

    val content = value.strContent.mkString
    value.sname match {
      case "value" | "string" => ParseResult(result = XmlRpcString(content).some)
      case "int" | "i4" => content.parseInt.fold(
        exception => ParseResult(errors = List("Unable to parse int: " + content)),
        (i: Int) => ParseResult(result = XmlRpcInt(i).some))
      case "boolean" => parseBoolean(content).map(XmlRpcBoolean)
      case "double" => content.parseDouble.fold(
        exception => ParseResult(errors = List("Unable to parse Double " + exception.getMessage)),
        (d: Double) => ParseResult(result = XmlRpcDouble(d).some))
      case "date" => getDateFromISO8601String(content).fold(
        error => ParseResult(errors = List(error)),
        (d: Date) => ParseResult(result = XmlRpcDate(d).some))
      case "array" => {
        val a = value.children.headOption.map(_.children.map(element2XmlDataType)) | Nil
        a.sequenceU.map(XmlRpcArray)
      }
      case "struct" =>  value.children.map(memberToTuple).sequenceU.map(l => XmlRpcStruct(l.toMap))
      case "base64" => ParseResult(result = XmlRpcBase64(content.getBytes("UTF-8")).some)
      case unknown => ParseResult(errors = List(s"Unsupported element type: $unknown"))
    }

  }

}
