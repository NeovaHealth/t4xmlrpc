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

import scala.language.postfixOps
import scala.xml.{Node, Elem}
import com.tactix4.xmlrpc.Exceptions.XmlRpcParseException


/**
 * @author max@tactix4.com
 *         5/21/13
 */

//TODO: Do away with scala.xml - and use scalaz.xml
//TODO: make strings per the spec i.e. not full unicode
//TODO: Use Validations rather than all this horrible exception throwing

/**
 * Provides the superclass to [[com.tactix4.xmlrpc.XmlRpcResponseNormal]] and [[com.tactix4.xmlrpc.XmlRpcResponseFault]]
 */
sealed trait XmlRpcResponse{
  def toElem: Elem
  val params : List[XmlRpcDataValue]
}

/**
 * Represents a fault response from the server
 * it contains the error code and error string returned by the server
 * the error code should be an Int as per http://xmlrpc.scripting.com/spec
 * however pythons XML-RPC library decided to use a string instead :/
 * http://docs.python.org/2/library/xmlrpclib.html#fault-objects
 * for this reason we allow a string too, hence why faultCode has type Either[XmlRpcString.XmlRpcInt]
 *
 * @param element the raw xml Elem from the server
 */

case class XmlRpcResponseFault(element: Node) extends XmlRpcResponse {

  private[XmlRpcResponseFault] val members =  scala.xml.Utility.trim(element) \ "fault" \ "value" \ "struct" \ "member"

  //grab the faultCode value
  private[XmlRpcResponseFault] val faultCodeValueElem = (members.filter { _ \\ "_" exists (_.text == "faultCode") } \ "value").headOption
  //parse the faultString
  val faultCode : Either[XmlRpcString, XmlRpcInt] = faultCodeValueElem.map(
    try{
      _ match{

        case <value><int>{contents}</int></value>       => Right(XmlRpcInt(contents.text.toInt))
        case <value><i4>{contents}</i4></value>         => Right(XmlRpcInt(contents.text.toInt))
        case <value><string>{contents}</string></value> => Left(XmlRpcString(contents.text))
        case <value>{string}</value> if string.isAtom   => Left(XmlRpcString(string.text, withTag = false))
        case x                                          => throw new XmlRpcParseException("Could not parse faultCode value element: " + x)
      }
    }
    catch {
      case e: NumberFormatException => throw new XmlRpcParseException("could not parse xml content",e)
    }
  ).getOrElse(throw new XmlRpcParseException("Could not find faultCode"))

  //grab the faultString value
  private[XmlRpcResponseFault] val faultStringValueElem = (members.filter { _ \\ "_" exists (_.text == "faultString") } \ "value" ).headOption
  //parse the faultString
  val faultString : XmlRpcString = faultStringValueElem.map( _ match {

    case <value><string>{contents}</string></value> => XmlRpcString(contents.text)
    case <value>{unnamedString}</value>             => XmlRpcString(unnamedString.text,withTag = false)

    case x => throw new XmlRpcParseException("Could not parse faultString value element: " + x)

  }).getOrElse(throw new XmlRpcParseException("Could not find faultString"))


  val params : List[XmlRpcDataValue] = List(faultCode.fold(s => s, i => i), faultString)

  override def toString : String  = "Fault Code: " + faultCode.fold(_.value,_.value) + "\nFault String: " + faultString.value

  /**
   *
   * @return an Elem representation of the fault message
   */
  def toElem: Elem = <methodResponse><fault><value><struct><member><name>faultCode</name>{faultCode.fold(s => XmlWriter.write(s),i => XmlWriter.write(i))}</member><member><name>faultString</name>{XmlWriter.write(faultString)}</member></struct></value></fault></methodResponse>
}

/**
 * Represents a normal (i.e. non-fault) response from the XML-RPC server
 * @param element the xml Node from the server
 */
case class XmlRpcResponseNormal(element: Node) extends XmlRpcResponse{

  /**
   * a list of XmlRpcDataValue object representing the response's arguments
   */
  val params: List[XmlRpcDataValue] = {
    val query = element \ "params" \ "param" \ "_"
    query.map(getParam).toList
  }

  /**
   * translate an xml node into a scala type
   * @param node the node to convert
   * @return a case class based on XmlRpcDataValue
   * @throws XmlRpcParseException if the node doesn't match one the expected types
   */
  private[XmlRpcResponseNormal] def getParam(node: Node) : XmlRpcDataValue =
  try{
    scala.xml.Utility.trim(node) match {
      case <value><string>{v}</string></value>        if v.isAtom =>  XmlRpcString(unescape(v.text))
      case <value><int>{v}</int></value>              if v.isAtom =>  XmlRpcInt(v.text.toInt)
      case <value><i4>{v}</i4></value>                if v.isAtom =>  XmlRpcInt(v.text.toInt)
      case <value><double>{v}</double></value>        if v.isAtom =>  XmlRpcDouble(v.text.toDouble)
      case <value><base64>{v}</base64></value>        if v.isAtom =>  XmlRpcBase64(v.text.getBytes)
      case <value><date>{v}</date></value>            if v.isAtom =>  XmlRpcDateTime(getDateFromISO8601String(v.text))
      case <value><boolean>{v}</boolean></value>      if v.isAtom =>  XmlRpcBoolean(!v.text.equals("0"))
      case <value><array><data>{v @_*}</data></array></value>     =>  createXmlRpcArray((node \ "array" \ "data" \"value" toList).reverse)
      case <value><struct>{v @_*}</struct></value> => {
        val names = v \ "name"
        if(names.exists(_.descendant.size != 1)) throw new XmlRpcParseException("Could not parse struct correctly: " + node)
        val namesText = names.map(_.text)
        val values = v \ "value"
        if(namesText.length != values.length) throw new XmlRpcParseException("Could not parse struct correctly: " + node)
        createXmlRpcStruct((namesText zip values).toList.reverse)
      }
      //if no type is specified assume a string
      case <value>{v}</value>                         if v.isAtom =>  XmlRpcString(unescape(v.text), withTag = false)
      case x                                                      => throw new XmlRpcParseException("Parse failed.  Unexpected Node: " + x)
    }
  }
  catch {
    case e: NumberFormatException => throw new XmlRpcParseException("could not parse xml content",e)
  }

  def createXmlRpcArray(nodes : List[Node]) : XmlRpcArray = {
    def loop(nodes:List[Node], acc:List[XmlRpcDataValue]) : XmlRpcArray = nodes match {
      case Nil => acc
      case x::xs => loop(xs, getParam(x)::acc)
     }
    loop(nodes,Nil)
  }

  def createXmlRpcStruct(nodes : List[(String,Node)]) : XmlRpcStruct =  {
    def loop(nodes: List[(String,Node)], acc:List[(String, XmlRpcDataValue)]) : XmlRpcStruct = nodes match {
      case Nil => acc
      case (x1,x2)::xs => loop(xs, x1 -> getParam(x2) :: acc)
    }
    loop(nodes,Nil)

  }

  def toElem : Elem =   <methodResponse><params>{params.map(d => <param>{XmlWriter.write(d)}</param>)}</params></methodResponse>

  override def toString : String = toElem.toString()
}